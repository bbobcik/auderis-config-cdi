package cz.auderis.tools.config.cdi;

import javax.enterprise.inject.spi.Bean;
import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.Set;

enum ConfigurationSourceComparator implements Comparator<Bean> {

	BY_PRIORITY {
		@Override
		public int compare(Bean b1, Bean b2) {
			assert null != b1;
			assert null != b2;
			final ConfigurationSource src1 = getConfigurationSource(b1);
			final ConfigurationSource src2 = getConfigurationSource(b2);
			if (null == src1) {
				return (null == src2) ? 0 : 1;
			} else if (null == src2) {
				return -1;
			}
			// Both sources are non-null, use decreasing ordering
			final int p1 = src1.priority();
			final int p2 = src2.priority();
			return p2 - p1;
		}
	}

	;


	static ConfigurationSource getConfigurationSource(Bean<?> bean) {
		final Set<Annotation> qualifiers = bean.getQualifiers();
		for (final Annotation qualifier : qualifiers) {
			if (qualifier instanceof ConfigurationSource) {
				return (ConfigurationSource) qualifier;
			}
		}
		return null;
	}

}
