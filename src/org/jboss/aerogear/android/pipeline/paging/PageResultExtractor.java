/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.android.pipeline.paging;

import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.http.HeaderAndBody;

/**
 * Classes which implement this interface are responsible for consuming a response from a server and extracting paging information (if any).
 */
public interface PageResultExtractor<T extends PageConfig> {

    /**
     * 
     * Extracts a usable ReadFilter from the response of a server for the "next" result set.
     * 
     * @param response the server's response from a Pipe.read or Pipe.readWithFilter call.
     * @param config the Pipe's PageConfig.
     * @return a ReadFilter to be used to get the "next" page from Pipe.readWithFilter.  
     */
    public ReadFilter getNextFilter(HeaderAndBody response, T config);

    /**
     * 
     * Extracts a usable ReadFilter from the response of a server for the "previous" result set.
     * 
     * @param response the server's response from a Pipe.read or Pipe.readWithFilter call.
     * @param config the Pipe's PageConfig.
     * @return a ReadFilter to be used to get the "previous" page from Pipe.readWithFilter.  
     */
    public ReadFilter getPreviousFilter(HeaderAndBody response, T config);
}
