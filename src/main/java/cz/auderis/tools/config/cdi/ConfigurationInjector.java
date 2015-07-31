/*
 * Copyright 2015 Boleslav Bobcik - Auderis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cz.auderis.tools.config.cdi;

import cz.auderis.tools.config.ConfigurationData;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.util.AnnotationLiteral;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class ConfigurationInjector implements Bean, Serializable {
	private static final long serialVersionUID = 7741337547728658327L;
	private static final Set<Annotation> DEFAULT_QUALIFIERS = createDefaultQualifiers();

	private static Set<Annotation> createDefaultQualifiers() {
		final Set<Annotation> result = new HashSet<Annotation>(2);
		final AnnotationLiteral<Default> defaultLiteral = new AnnotationLiteral<Default>() { };
		final AnnotationLiteral<Any> anyLiteral = new AnnotationLiteral<Any>() { };
		result.add(defaultLiteral);
		result.add(anyLiteral);
		return Collections.unmodifiableSet(result);
	}

	private final Class<?> targetType;
	private final Set<Annotation> qualifiers;
	private final InjectorDataProvider dataProvider;

	ConfigurationInjector(Class<?> type, Set<Annotation> qualifiers, InjectorDataProvider cdiDataProvider) {
		assert null != type;
		assert null != cdiDataProvider;
		this.targetType = type;
		this.dataProvider = cdiDataProvider;
		if (qualifiers == null || qualifiers.isEmpty()) {
			this.qualifiers = DEFAULT_QUALIFIERS;
		} else {
			final HashSet<Annotation> copyOfQualifiers = new HashSet<Annotation>(qualifiers);
			this.qualifiers = Collections.unmodifiableSet(copyOfQualifiers);
		}
	}

	@Override
	public Set<? extends Type> getTypes() {
		return Collections.singleton(targetType);
	}

	@Override
	public Set<Annotation> getQualifiers() {
		return qualifiers;
	}

	@Override
	public Class<?> getScope() {
		return Dependent.class;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public Set getStereotypes() {
		return Collections.emptySet();
	}

	@Override
	public Class getBeanClass() {
		return targetType;
	}

	@Override
	public boolean isAlternative() {
		return false;
	}

	@Override
	public boolean isNullable() {
		return false;
	}

	@Override
	public Set getInjectionPoints() {
		return Collections.emptySet();
	}

	/**
	 * Creates a concrete configuration accessor (proxy defined by
	 * {@link cz.auderis.tools.config.ConfigurationDataAccessProxyHandler})
	 * that is needed at the given injection point.
	 *
	 *
	 * @param creationalContext
	 * @return
	 */
	@Override
	public Object create(CreationalContext creationalContext) {
		final Object cfgAccessor = ConfigurationData.createConfigurationObject(dataProvider, targetType);
		return cfgAccessor;
	}

	@Override
	public void destroy(Object instance, CreationalContext creationalContext) {
		creationalContext.release();
	}

	@Override
	public String toString() {
		final StringBuilder str = new StringBuilder(32);
		str.append("CfgInjector[class=");
		str.append(targetType);
		if (!qualifiers.isEmpty()) {
			str.append(", qualifiers=");
			str.append(qualifiers);
		}
		str.append(']');
		return str.toString();
	}

}