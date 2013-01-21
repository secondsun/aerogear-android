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
package org.jboss.aerogear.android.pipeline;


public class PageConfig {

    public static enum MetadataLocation {
        WEB_LINKING("webLinking"),
        HEADERS("headers"),
        BODY("body");

        private final String value;

        private MetadataLocation(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

    }

    public static enum PagingLocation {
        QUERY("query"), HEADER("header");

        private final String value;

        private PagingLocation(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

    }

    /**
     * indicates whether paging information (see identifiers) is received from the response header, the response body (body) or via RFC 5988 (webLinking), which is the default
     */
    private String metadataLocation = MetadataLocation.WEB_LINKING.toString();

    /**
     *  indicate whether paging information is sent as query parameters (default), or on the request header.
     */
    private String pagingLocation = PagingLocation.QUERY.toString();

    /**
     * the next identifier name (default: next)
     */
    private String nextIdentifier = "next";

    /**
     *  the previous identifier name (default: previous)
     */
    private String previousIdentifier = "previous";

    /**
     * the offset of the first element that should be included in the returned collection (default: 0)
     */
    private String offsetValue = "0";

    /**
     * the maximum number of results the server should return (default: 10)
     */
    private Integer limitValue = 10;

    private ParameterProvider parameterProvider;

    public String getMetadataLocation() {
        return metadataLocation;
    }

    public void setMetadataLocation(String metadataLocation) {
        this.metadataLocation = metadataLocation;
    }

    public String getPagingLocation() {
        return pagingLocation;
    }

    public void setPagingLocation(String pagingLocation) {
        this.pagingLocation = pagingLocation;
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

    
    
}
