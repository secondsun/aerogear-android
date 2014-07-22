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
import org.jboss.aerogear.android.code.PipeModule;
import org.jboss.aerogear.android.pipeline.paging.PageConfig;

/**
 *
 * @param <CONFIGURATION> configuration TODO: define this better
 */
public interface PipeConfiguration<CONFIGURATION extends PipeConfiguration<CONFIGURATION>> {

    public <DATA> Pipe<DATA> forClass(Class<DATA> aClass);

    public CONFIGURATION withUrl(URL url);

    //TODO: create module classes maybe?
    public CONFIGURATION module(PipeModule module);

    public CONFIGURATION pageConfig(PageConfig pageConfig);

    public CONFIGURATION requestBuilder(RequestBuilder multipartBuilder);
    
}
