package cz.auderis.tools.config.cdi;

import cz.auderis.test.category.UnitTest;
import cz.auderis.tools.config.ConfigurationDataProvider;
import cz.auderis.tools.config.annotation.ConfigurationObject;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import static cz.auderis.tools.config.cdi.MockedDataSupport.mockConfigValues;
import static cz.auderis.tools.config.cdi.MockedDataSupport.undefined;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(CdiRunner.class)
@AdditionalClasses(ConfigurationInjectorExtension.class)
@SuppressWarnings("CdiInjectionPointsInspection")
public class TestMultipleProviders {

	interface TestCfgObject {
		String getName();
		String getAlias();
		String getTitle();
	}

	@Produces
	@ConfigurationSource(priority = 20)
	static ConfigurationDataProvider providerA = mock(ConfigurationDataProvider.class);

	@Produces
	@ConfigurationSource(priority = 10)
	static ConfigurationDataProvider providerB = mock(ConfigurationDataProvider.class);

	@Produces
	@ConfigurationSource(priority = 999)
	static ConfigurationDataProvider providerC = mock(ConfigurationDataProvider.class);

	@Inject
	@ConfigurationObject
	private TestCfgObject cfg;

	@Before
	public void resetProviders() {
		Mockito.reset(providerA, providerB, providerC);
	}

	@Test
	@Category(UnitTest.class)
	public void shouldCorrectlyResolvePriorities() throws Exception {
		// Given
		mockConfigValues(providerA, "name", "nameA", "alias", "aliasA");
		mockConfigValues(providerB, "name", "nameB");
		mockConfigValues(providerC, "alias", "aliasC");

		// When
		final String cfgName = cfg.getName();
		final String cfgAlias = cfg.getAlias();

		// Then
		assertThat(cfgName, is("nameA"));
		assertThat(cfgAlias, is("aliasC"));
	}

	@Test
	@Category(UnitTest.class)
	public void shouldUseFirstAvailableProvider() throws Exception {
		// Given
		mockConfigValues(providerA, "alias", "aliasA");
		mockConfigValues(providerB, "alias", "aliasB");

		// When
		final String cfgAlias = cfg.getAlias();

		// Then
		assertThat(cfgAlias, is("aliasA"));
	}

	@Test
	@Category(UnitTest.class)
	public void shouldCorrectlyFallbackToOtherProvider() throws Exception {
		// Given
		mockConfigValues(providerA, "title", undefined());
		mockConfigValues(providerB, "title", "titleB");

		// When
		final String cfgTitle = cfg.getTitle();

		// Then
		assertThat(cfgTitle, is("titleB"));
	}

}
