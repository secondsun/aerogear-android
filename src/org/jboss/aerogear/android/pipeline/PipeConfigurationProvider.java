package org.jboss.aerogear.android.pipeline;

public interface PipeConfigurationProvider<CONFIGURATION extends PipeConfiguration<?> {
    CONFIGURATION configuration();
}
