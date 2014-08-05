/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.android.pipeline;

import org.jboss.aerogear.android.impl.pipeline.RestfulPipeConfiguration;
import java.util.HashMap;
import java.util.Map;
import org.jboss.aerogear.android.ConfigurationProvider;


public class PipeManager {

     private static Map<String, Pipe<?>> pipes = new HashMap<String, Pipe<?>>();

    private static Map<Class<? extends PipeConfiguration<?>>, ConfigurationProvider<?>>
            configurationProviderMap = new HashMap<Class<? extends PipeConfiguration<?>>, ConfigurationProvider<?>>();

    private static OnPipeCreatedListener onPipeCreatedListener = new OnPipeCreatedListener() {
        @Override
        public void onPipeCreated(PipeConfiguration<?> configuration, Pipe<?> pipe) {
            pipes.put(configuration.getName(), pipe);
        }
    };

    static {
        RestfulPipeConfigurationProvider configurationProvider = new RestfulPipeConfigurationProvider();
        PipeManager.registerConfigurationProvider(RestfulPipeConfiguration.class, configurationProvider);
    }

    private PipeManager() {
    }

    public static <CFG extends PipeConfiguration<CFG>> void registerConfigurationProvider
            (Class<CFG> configurationClass, ConfigurationProvider<CFG> provider) {
        configurationProviderMap.put(configurationClass, provider);
    }

    public static <CFG extends PipeConfiguration<CFG>> CFG config(String name, Class<CFG> pipeImplementationClass) {

        @SuppressWarnings("unchecked")
        ConfigurationProvider<? extends PipeConfiguration<CFG>> provider =
                (ConfigurationProvider<? extends PipeConfiguration<CFG>>)
                        configurationProviderMap.get(pipeImplementationClass);

        if (provider == null) {
            throw new IllegalArgumentException("Configuration not registered!");
        }

        return provider.newConfiguration()
                .setName(name);

    }

    public static Pipe getPipe(String name) {
        return pipes.get(name);
    }



}
