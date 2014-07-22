package org.jboss.aerogear.android.pipeline;

import java.util.HashMap;
import java.util.Map;

public class Pipeline2 {

    private static final Map<String, Pipe> pipes = new HashMap<String, Pipe>();

    private static final Map<Class<? extends PipeConfiguration>, PipeConfigurationProvider<?>> configurationProviderMap
            = new HashMap<Class<? extends PipeConfiguration>, PipeConfigurationProvider<?>>();

    public static <CONFIGURATION extends PipeConfiguration<?>> CONFIGURATION config(Class<CONFIGURATION> configurationClass, String name) {
        PipeConfigurationProvider<?> provider = configurationProviderMap.get(configurationClass);
        return provider.configuration();
    }

    public static <CONFIGURAION extends PipeConfiguration<?>> void registerConfigurationProvider(Class<CONFIGURAION> klass, PipeConfigurationProvider<CONFIGURAION> provider) {
        configurationProviderMap.put(klass, provider);
    }
    
}
