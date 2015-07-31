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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class InjectorDataProvider implements ConfigurationDataProvider {

	private final List<ConfigurationDataProvider> delegateProviders;
	private final Map<String, ConfigurationDataProvider> providerByKey;

	InjectorDataProvider() {
		this.delegateProviders = new ArrayList<ConfigurationDataProvider>(64);
		this.providerByKey = new HashMap<String, ConfigurationDataProvider>(64);
	}

	void addDelegate(ConfigurationDataProvider provider) {
		assert null != provider;
		if (!delegateProviders.contains(provider)) {
			delegateProviders.add(provider);
			providerByKey.clear();
		}
	}

	@Override
	public boolean containsKey(String key) {
		final ConfigurationDataProvider cachedProvider = providerByKey.get(key);
		if ((null != cachedProvider) && (DummyProvider.INSTANCE != cachedProvider)) {
			return true;
		}
		for (final ConfigurationDataProvider provider : delegateProviders) {
			if (provider.containsKey(key)) {
				providerByKey.put(key, provider);
				return true;
			}
		}
		providerByKey.put(key, DummyProvider.INSTANCE);
		return false;
	}

	@Override
	public Object getRawObject(String key) {
		final ConfigurationDataProvider cachedProvider = providerByKey.get(key);
		if ((null != cachedProvider) && (DummyProvider.INSTANCE != cachedProvider)) {
			return cachedProvider.getRawObject(key);
		}
		for (final ConfigurationDataProvider provider : delegateProviders) {
			if (provider.containsKey(key)) {
				providerByKey.put(key, provider);
				return provider.getRawObject(key);
			}
		}
		providerByKey.put(key, DummyProvider.INSTANCE);
		return null;
	}

	enum DummyProvider implements ConfigurationDataProvider {
		INSTANCE {
			@Override public boolean containsKey(String key) { return false; }
			@Override public Object getRawObject(String key) { return null; }
		}
	}

}
