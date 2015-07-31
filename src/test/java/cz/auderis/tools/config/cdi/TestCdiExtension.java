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

import cz.auderis.test.category.SanityTest;
import cz.auderis.test.category.UnitTest;
import cz.auderis.tools.config.ConfigurationDataProvider;
import cz.auderis.tools.config.annotation.ConfigurationObject;
import org.hamcrest.Matchers;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import static cz.auderis.tools.config.cdi.MockedDataSupport.mockConfigValues;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(CdiRunner.class)
@AdditionalClasses(ConfigurationInjectorExtension.class)
public class TestCdiExtension {

	@SuppressWarnings("CdiInjectionPointsInspection")
	public static class CfgFieldConsumer {
		@Inject @ConfigurationObject TestCfgObject cfg;
	}

	@SuppressWarnings("CdiInjectionPointsInspection")
	public static class CfgConstructorConsumer {
		final TestCfgObject cfg;

		@Inject
		public CfgConstructorConsumer(@ConfigurationObject TestCfgObject injectedCfg) {
			this.cfg = injectedCfg;
		}
	}

	@SuppressWarnings("CdiInjectionPointsInspection")
	public static class CfgInitializerConsumer {
		TestCfgObject cfg;

		@Inject
		private void init(@ConfigurationObject TestCfgObject injectedCfg) {
			this.cfg = injectedCfg;
		}
	}

	interface TestCfgObject {
		String getName();
	}

	@Produces
	@ConfigurationSource
	static ConfigurationDataProvider cfgProvider = mock(ConfigurationDataProvider.class);

	@Inject
	CfgFieldConsumer testConsumer;

	@Inject
	CfgConstructorConsumer testConsumer2;

	@Inject
	CfgInitializerConsumer testConsumer3;

	@Test
	@Category(SanityTest.class)
	public void shouldInjectIntoField() throws Exception {
		assertThat(testConsumer, notNullValue());
		assertThat(testConsumer.cfg, Matchers.instanceOf(TestCfgObject.class));
	}

	@Test
	@Category(SanityTest.class)
	public void shouldInjectIntoConstructor() throws Exception {
		assertThat(testConsumer2, notNullValue());
		assertThat(testConsumer2.cfg, Matchers.instanceOf(TestCfgObject.class));
	}

	@Test
	@Category(SanityTest.class)
	public void shouldInjectIntoInitMethod() throws Exception {
		assertThat(testConsumer3, notNullValue());
		assertThat(testConsumer3.cfg, Matchers.instanceOf(TestCfgObject.class));
	}

	@Test
	@Category({ UnitTest.class, SanityTest.class})
	public void shouldReturnCorrectValue() throws Exception {
		// Given
		final String name = "Xyz";
		mockConfigValues(cfgProvider, "name", name);

		// When
		final String obtainedName = testConsumer.cfg.getName();
		final String obtainedName2 = testConsumer2.cfg.getName();
		final String obtainedName3 = testConsumer3.cfg.getName();

		// Then
		assertThat(obtainedName, is(name));
		assertThat(obtainedName2, is(name));
		assertThat(obtainedName3, is(name));
	}

}
