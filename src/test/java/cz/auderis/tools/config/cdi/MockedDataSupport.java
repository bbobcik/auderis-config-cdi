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

import cz.auderis.tools.config.ConfigurationDataProvider;

import static org.mockito.Mockito.doReturn;

final class MockedDataSupport {

	static final Object UNDEFINED = new Object[0];

	static Object undefined() {
		return UNDEFINED;
	}

	static void mockConfigValues(ConfigurationDataProvider mock, Object... keysAndValues) {
		assert null != mock;
		assert null != keysAndValues;
		final int length = keysAndValues.length;
		for (int i=0; i< length; i += 2) {
			assert keysAndValues[i] instanceof String;
			assert i + 1 < length;
			final String key = (String) keysAndValues[i];
			final Object value = keysAndValues[i + 1];
			doReturn(value != UNDEFINED).when(mock).containsKey(key);
			doReturn(value).when(mock).getRawObject(key);
		}
	}


	private MockedDataSupport() {
		throw new AssertionError();
	}

}
