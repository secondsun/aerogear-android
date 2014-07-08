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
package org.jboss.aerogear.android.impl.http;

import java.net.URL;

import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.authentication.impl.HttpBasicAuthenticationModule;
import org.jboss.aerogear.android.http.HeaderAndBody;
import org.jboss.aerogear.android.http.HttpException;
import org.jboss.aerogear.android.http.HttpProvider;

import android.util.Pair;

/**
 * This is a stopgap class to provide HTTP Basic Authentication until we move
 * Authenticator support to the HttpProvider level.
 */
public class HttpRestProviderForPush implements HttpProvider {

    private final HttpRestProvider provider;

    private HttpBasicAuthenticationModule basicAuth;

    public HttpRestProviderForPush(URL url, Integer timeout) {
        this.provider = new HttpRestProvider(url, timeout);
    }

    @Override
    public URL getUrl() {
        return this.provider.getUrl();
    }

    @Override
    public HeaderAndBody get() throws HttpException {
        return this.provider.get();
    }

    @Override
    public HeaderAndBody post(String data) throws HttpException {
        return this.provider.post(data);
    }

    @Override
    public HeaderAndBody post(byte[] data) throws HttpException {
        return this.provider.post(data);
    }

    @Override
    public HeaderAndBody put(String id, String data) throws HttpException {
        return this.provider.put(id, data);
    }

    @Override
    public HeaderAndBody put(String id, byte[] data) throws HttpException {
        return this.provider.put(id, data);
    }

    @Override
    public HeaderAndBody delete(String id) throws HttpException {
        return this.provider.delete(id);
    }

    @Override
    public void setDefaultHeader(String headerName, String headerValue) {
        this.provider.setDefaultHeader(headerName, headerValue);
    }

    public void setPasswordAuthentication(final String username, final String password) {
        basicAuth = new HttpBasicAuthenticationModule(getUrl());
        basicAuth.login(username, password, new EmptyCallback());
        for (Pair<String, String> header : basicAuth.getAuthorizationFields(null, null, null).getHeaders()) {
            setDefaultHeader(header.first, header.second);
        }
    }

    /**
     * This class does nothing, but basic auth needs some form of callback.
     */
    private static class EmptyCallback implements Callback<HeaderAndBody> {

        private static final long serialVersionUID = 1L;

        @Override
        public void onSuccess(HeaderAndBody data) {
        }

        @Override
        public void onFailure(Exception e) {
        }

    }

}
