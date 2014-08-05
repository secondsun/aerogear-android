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

import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import org.jboss.aerogear.android.Config;
import org.jboss.aerogear.android.code.PipeModule;
import org.jboss.aerogear.android.pipeline.paging.PageConfig;

/**
 *
 * @param <CONFIGURATION> configuration TODO: define this better
 */
public abstract class PipeConfiguration<CONFIGURATION extends PipeConfiguration<CONFIGURATION>> implements Config<CONFIGURATION> {

    private String name;
    private Collection<OnPipeCreatedListener> listeners;
    
    public PipeConfiguration() {
        listeners = new HashSet<OnPipeCreatedListener>();
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public CONFIGURATION setName(String name) {
        this.name = name;
        return (CONFIGURATION) this;
    }
    
        public Collection<OnPipeCreatedListener> getOnPipeCreatedListeners() {
        return listeners;
    }

    public CONFIGURATION addOnPipeCreatedListener(OnPipeCreatedListener listener) {
        this.listeners.add(listener);
        return (CONFIGURATION) this;
    }

    public CONFIGURATION setOnPipeCreatedListeners(Collection<OnPipeCreatedListener> listeners) {
        listeners.addAll(listeners);
        return (CONFIGURATION) this;
    }
    
    /**
     * 
     * Creates a pipe based on the current configuration.
     * 
     * @param <DATA> The data type of the Pipe
     * @param aClass The data type class of the Pipe
     * @return A pipe based on this configuration
     * 
     * @throws IllegalStateException if the Pipe can not be constructed.
     * 
     */
    public abstract <DATA> Pipe<DATA> forClass(Class<DATA> aClass);

    public abstract CONFIGURATION withUrl(URL url);

    //TODO: create module classes maybe?
    public abstract CONFIGURATION module(PipeModule module);

    public abstract CONFIGURATION timeout(Integer timeout);
    
    public abstract CONFIGURATION pageConfig(PageConfig pageConfig);

    public abstract CONFIGURATION requestBuilder(RequestBuilder builder);
    
    public abstract CONFIGURATION responseParser(ResponseParser responseParser);

}
