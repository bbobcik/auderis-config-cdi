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
