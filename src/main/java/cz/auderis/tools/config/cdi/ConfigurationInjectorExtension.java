package cz.auderis.tools.config.cdi;

import cz.auderis.tools.config.ConfigurationDataProvider;
import cz.auderis.tools.config.annotation.ConfigurationObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.enterprise.util.AnnotationLiteral;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ConfigurationInjectorExtension implements Extension {

	final Logger LOG;
	int detectedInjectionPoints;
	final Set<ConfigurationObjectKey> cfgObjectInjections;
	final InjectorDataProvider cdiDataProvider;

	public ConfigurationInjectorExtension() {
		this.cfgObjectInjections = new HashSet<ConfigurationObjectKey>(32);
		this.cdiDataProvider = new InjectorDataProvider();
		this.LOG = LoggerFactory.getLogger(ConfigurationInjectorExtension.class);
	}

	/**
	 * Makes the {@link ConfigurationObject} behave as a qualifier despite not having
	 * the standard {@link javax.inject.Qualifier} annotation. This is to avoid a need
	 * for JavaEE dependency in core <b>auderis-config</b> module.
	 *
	 * @param event CDI event
	 */
	public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery event) {
		LOG.trace("Auderis Configuration CDI extension enabled");
		// Make the @ConfigurationObject behave as a qualifier despite not having
		// the standard @Qualifier annotation (this is to avoid dependency on JavaEE in
		// core auderis-config module)
		event.addQualifier(ConfigurationObject.class);
	}

	/**
	 * Records occurrences of {@code ConfigurationObject} annotations during CDI initialization.
	 *
	 * @param event CDI event
	 * @param <X> Class of the CDI bean that is the subject of this event (<i>not used</i>)
	 */
	public <X> void processInjectionTarget(@Observes ProcessInjectionTarget<X> event, BeanManager beanManager) {
		final boolean logInjectionPoints = LOG.isTraceEnabled();
		final InjectionTarget<X> injectionTarget = event.getInjectionTarget();
		final Set<InjectionPoint> injectionPoints = injectionTarget.getInjectionPoints();
		for (final InjectionPoint injectionPoint : injectionPoints) {
			final Annotated annotatedPoint = injectionPoint.getAnnotated();
			if (annotatedPoint.isAnnotationPresent(ConfigurationObject.class)) {
				registerCfgObjectInjectionPoint(injectionPoint, beanManager);
				if (logInjectionPoints) {
					LOG.trace("Registered " + injectionPoint + " as configuration injection point");
				}
			}
		}
	}

	private void registerCfgObjectInjectionPoint(InjectionPoint injectionPoint, BeanManager beanManager) {
		final Class<?> pointType = (Class<?>) injectionPoint.getAnnotated().getBaseType();
		final Set<Annotation> origAnnotations = injectionPoint.getAnnotated().getAnnotations();
		final Set<Annotation> pointAnnotations = new HashSet<Annotation>(origAnnotations);
		// Remove all non-qualifier annotations
		for (Iterator<Annotation> annotationIterator = pointAnnotations.iterator(); annotationIterator.hasNext(); ) {
			final Annotation annotation = annotationIterator.next();
			final Class<? extends Annotation> annotationType = annotation.annotationType();
			if (!beanManager.isQualifier(annotationType)) {
				annotationIterator.remove();
			}
		}
		final ConfigurationObjectKey injection = new ConfigurationObjectKey(pointType, pointAnnotations);
		cfgObjectInjections.add(injection);
		++detectedInjectionPoints;
	}

	public void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager manager) {
		if (cfgObjectInjections.isEmpty()) {
			LOG.debug("No configuration injection points detected");
			return;
		}
		final List<ConfigurationDataProvider> cfgDataProviders = getAvailableProviders(manager);
		LOG.trace("Detected " + cfgDataProviders.size() + " configuration data providers");
		if (cfgDataProviders.isEmpty()) {
			final int injectCount = cfgObjectInjections.size();
			LOG.error("Cannot inject " + injectCount + " cfg injection points, no data providers detected");
			throw new InjectionException("Cannot create configuration data provider, no @ConfigurationSource found");
		}
		for (final ConfigurationDataProvider provider : cfgDataProviders) {
			cdiDataProvider.addDelegate(provider);
		}
		LOG.trace("Preparing beans for " + cfgObjectInjections.size() + " requested configuration data types");
		for (ConfigurationObjectKey key : cfgObjectInjections) {
			final Class<?> cfgBeanType = key.getType();
			final Set<Annotation> qualifiers = key.getQualifiers();
			final ConfigurationInjector bean = new ConfigurationInjector(cfgBeanType, qualifiers, cdiDataProvider);
			event.addBean(bean);
		}
		LOG.debug("Prepared " + cfgObjectInjections.size() + " beans for "
				+ detectedInjectionPoints + " configuration injection points");
		detectedInjectionPoints = 0;
		cfgObjectInjections.clear();
	}

	@SuppressWarnings("unchecked")
	private List<ConfigurationDataProvider> getAvailableProviders(BeanManager beanManager) {
		assert beanManager.isQualifier(ConfigurationSource.class);
		// Find all beans and producers that provide ConfigurationDataProvider instances
		final Annotation anyAnnotation = new AnnotationLiteral<Any>() { };
		final Set<Bean<?>> cfgBeanCandidates = beanManager.getBeans(ConfigurationDataProvider.class, anyAnnotation);
		if (cfgBeanCandidates.isEmpty()) {
			return Collections.emptyList();
		}
		// Filter out beans and candidates that do not have qualifier @ConfigurationSource
		final List<Bean<ConfigurationDataProvider>> candidateList = new ArrayList<Bean<ConfigurationDataProvider>>(cfgBeanCandidates.size());
		for (final Bean<?> candidateBean : cfgBeanCandidates) {
			if (hasQualifierType(candidateBean, ConfigurationSource.class)) {
				assert hasBeanType(candidateBean, ConfigurationDataProvider.class);
				candidateList.add((Bean<ConfigurationDataProvider>) candidateBean);
			}
		}
		if (candidateList.isEmpty()) {
			return Collections.emptyList();
		}
		// Prepare a list of ConfigurationDataProvider instances sorted by their priorities
		Collections.sort(candidateList, ConfigurationSourceComparator.BY_PRIORITY);
		final List<ConfigurationDataProvider> result = new ArrayList<ConfigurationDataProvider>(candidateList.size());
		final CreationalContext<ConfigurationDataProvider> ctx = beanManager.createCreationalContext(null);
		for (final Bean<ConfigurationDataProvider> candidateBean : candidateList) {
			final ConfigurationDataProvider instance = candidateBean.create(ctx);
			result.add(instance);
		}
		return result;
	}


	private static boolean hasQualifierType(Bean<?> bean, Class<? extends Annotation> requiredType) {
		final Collection<Annotation> qualifiers = bean.getQualifiers();
		if (null == qualifiers) {
			return false;
		}
		for (final Annotation qualifier : qualifiers) {
			final Class<? extends Annotation> qualifierType = qualifier.annotationType();
			if (requiredType.equals(qualifierType)) {
				return true;
			}
		}
		return false;
	}

	private static boolean hasBeanType(Bean<?> bean, Class<?> requiredType) {
		final Set<Type> types = bean.getTypes();
		for (final Type type : types) {
			if ((type instanceof Class) && requiredType.isAssignableFrom((Class<?>) type)) {
				return true;
			}
		}
		return false;
	}

}
