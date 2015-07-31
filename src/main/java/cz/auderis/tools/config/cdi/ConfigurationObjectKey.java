package cz.auderis.tools.config.cdi;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

final class ConfigurationObjectKey {

	final Class<?> type;
	final Set<Annotation> qualifiers;

	ConfigurationObjectKey(Class<?> type, Set<Annotation> qualifiers) {
		assert null != type;
		assert null != qualifiers;
		this.type = type;
		if (qualifiers.isEmpty()) {
			this.qualifiers = Collections.emptySet();
		} else {
			final Set<Annotation> qualifiersCopy = new HashSet<Annotation>(qualifiers);
			this.qualifiers = Collections.unmodifiableSet(qualifiersCopy);
		}
	}

	Class<?> getType() {
		return type;
	}

	Set<Annotation> getQualifiers() {
		return qualifiers;
	}

	@Override
	public int hashCode() {
		int result = qualifiers.hashCode();
		result = 31 * result + type.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if ((null == obj) || (getClass() != obj.getClass())) {
			return false;
		}
		final ConfigurationObjectKey other = (ConfigurationObjectKey) obj;
		if (!type.equals(other.type)) {
			return false;
		} else if (!qualifiers.equals(other.qualifiers)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "CfgObjKey[class=" + type + ", qualifiers=" + qualifiers + ']';
	}

}
