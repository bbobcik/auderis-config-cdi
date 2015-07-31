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
