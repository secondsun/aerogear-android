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

import org.jboss.aerogear.android.impl.pipeline.paging.DefaultParameterProvider;

public class PageConfig {

    public static enum MetadataLocations implements MetadataLocation {
        WEB_LINKING("webLinking"),
        HEADERS("headers"),
        BODY("body");

        private final String value;

        private MetadataLocations(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

    }

    /**
     * Indicates whether paging information is received from the response header, the response body (body) or via RFC 5988 (webLinking), which is the default
     */
    private MetadataLocation metadataLocation = MetadataLocations.WEB_LINKING;

    /**
     * The identified for the element containing data for the next page (default: next)
     */
    private String nextIdentifier = "next";

    /**
     * The identified for the element containing data for the previous page (default: previous)
     */
    private String previousIdentifier = "previous";

    /**
     * The offset of the first element that should be included in the returned collection (default: 0)
     */
    private String offsetValue = "0";

    /**
     * The maximum number of results the server should return (default: 10)
     */
    private Integer limitValue = 10;

    /**
     * The {@link ParameterProvider} for paging.  Defaults to {@link DefaultParameterProvider}
     */
    private ParameterProvider parameterProvider = new DefaultParameterProvider();

    private PageResultExtractor pageHeaderParser;

    public MetadataLocation getMetadataLocation() {
        return metadataLocation;
    }

    public void setMetadataLocation(MetadataLocation metadataLocation) {
        this.metadataLocation = metadataLocation;
    }

    public String getNextIdentifier() {
        return nextIdentifier;
    }

    public void setNextIdentifier(String nextIdentifier) {
        this.nextIdentifier = nextIdentifier;
    }

    public String getPreviousIdentifier() {
        return previousIdentifier;
    }

    public void setPreviousIdentifier(String previousIdentifier) {
        this.previousIdentifier = previousIdentifier;
    }

    public String getOffsetValue() {
        return offsetValue;
    }

    public void setOffsetValue(String offsetValue) {
        this.offsetValue = offsetValue;
    }

    public Integer getLimitValue() {
        return limitValue;
    }

    public void setLimitValue(Integer limitValue) {
        this.limitValue = limitValue;
    }

    public ParameterProvider getParameterProvider() {
        return parameterProvider;
    }

    public void setParameterProvider(ParameterProvider parameterProvider) {
        this.parameterProvider = parameterProvider;
    }

    public PageResultExtractor getPageHeaderParser() {
        return pageHeaderParser;
    }

    public void setPageHeaderParser(PageResultExtractor pageHeaderParser) {
        this.pageHeaderParser = pageHeaderParser;
    }

}
