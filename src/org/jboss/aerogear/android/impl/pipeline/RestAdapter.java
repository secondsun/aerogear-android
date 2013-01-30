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

package org.jboss.aerogear.android.impl.pipeline;

import org.jboss.aerogear.android.impl.pipeline.paging.DefaultParameterProvider;
import org.jboss.aerogear.android.impl.pipeline.paging.WrappingPagedList;
import org.jboss.aerogear.android.impl.pipeline.paging.URIPageHeaderParser;
import org.jboss.aerogear.android.impl.pipeline.paging.WebLink;
import org.jboss.aerogear.android.impl.pipeline.paging.URIBodyPageParser;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.Provider;
import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.authentication.AuthenticationModule;
import org.jboss.aerogear.android.authentication.AuthorizationFields;
import org.jboss.aerogear.android.http.HeaderAndBody;
import org.jboss.aerogear.android.http.HttpProvider;
import org.jboss.aerogear.android.impl.core.HttpProviderFactory;
import org.jboss.aerogear.android.impl.reflection.Property;
import org.jboss.aerogear.android.impl.reflection.Scan;
import org.jboss.aerogear.android.pipeline.Pipe;
import org.jboss.aerogear.android.pipeline.PipeType;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.http.client.utils.URIUtils;
import org.jboss.aerogear.android.impl.util.ParseException;
import org.jboss.aerogear.android.impl.util.WebLinkParser;
import org.jboss.aerogear.android.pipeline.paging.PageConfig;
import org.jboss.aerogear.android.pipeline.paging.ParameterProvider;
import org.json.JSONObject;

/**
 * Rest implementation of {@link Pipe}.
 */
public final class RestAdapter<T> implements Pipe<T> {

    private final PageConfig pageConfig;
    private static final String TAG = RestAdapter.class.getSimpleName();
    private final Gson gson;
    private String dataRoot = "";
    private ParameterProvider parameterProvider = new DefaultParameterProvider();
    /**
     * A class of the Generic type this pipe wraps. This is used by GSON for
     * deserializing.
     */
    private final Class<T> klass;
    /**
     * A class of the Generic collection type this pipe wraps. This is used by
     * JSON for deserializing collections.
     */
    private final Class<T[]> arrayKlass;
    private final URL baseURL;
    private final Provider<HttpProvider> httpProviderFactory = new HttpProviderFactory();
    private AuthenticationModule authModule;
    private Charset encoding = Charset.forName("UTF-8");

    public RestAdapter(Class<T> klass, URL baseURL) {
        this.klass = klass;
        this.arrayKlass = asArrayClass(klass);
        this.baseURL = baseURL;
        this.gson = new Gson();
        this.pageConfig = null;
    }

    public RestAdapter(Class<T> klass, URL baseURL,
            GsonBuilder gsonBuilder) {
        this.klass = klass;
        this.arrayKlass = asArrayClass(klass);
        this.baseURL = baseURL;
        this.gson = gsonBuilder.create();
        this.pageConfig = null;
    }

    public RestAdapter(Class<T> klass, URL baseURL, PageConfig pageconfig) {
        this.klass = klass;
        this.arrayKlass = asArrayClass(klass);
        this.baseURL = baseURL;
        this.gson = new Gson();
        this.pageConfig = pageconfig;
    }

    public RestAdapter(Class<T> klass, URL baseURL,
            GsonBuilder gsonBuilder, PageConfig pageconfig) {
        this.klass = klass;
        this.arrayKlass = asArrayClass(klass);
        this.baseURL = baseURL;
        this.gson = gsonBuilder.create();
        this.pageConfig = pageconfig;
        if (pageconfig != null) {
            if (pageconfig.getPageHeaderParser() == null) {
                if (PageConfig.MetadataLocations.BODY.equals(pageconfig.getMetadataLocation())) {
                    pageconfig.setPageHeaderParser(new URIBodyPageParser(baseURL));
                } else if (PageConfig.MetadataLocations.HEADERS.equals(pageconfig.getMetadataLocation())) {
                    pageconfig.setPageHeaderParser(new URIPageHeaderParser(baseURL));
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PipeType getType() {
        return PipeTypes.REST;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL getUrl() {
        return baseURL;
    }

    @Override
    public void readWithFilter(ReadFilter filter, final Callback<List<T>> callback) {
        if (filter == null) {
            filter = new ReadFilter();
        }
        final ReadFilter innerFilter = filter;

        new AsyncTask<Void, Void, Void>() {
            List<T> result = null;
            Exception exception = null;

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    HttpProvider httpProvider;
                    if (innerFilter.getLinkUri() == null) {
                        httpProvider = getHttpProvider(parameterProvider.getParameters(innerFilter));
                    } else {
                        httpProvider = getHttpProvider(innerFilter.getLinkUri());
                    }
                    HeaderAndBody httpResponse = httpProvider.get();
                    byte[] responseBody = httpResponse.getBody();
                    String responseAsString = new String(responseBody, encoding);
                    JsonParser parser = new JsonParser();
                    JsonElement httpJsonResult = parser.parse(responseAsString);
                    httpJsonResult = getResultElement(httpJsonResult, dataRoot);
                    if (httpJsonResult.isJsonArray()) {
                        T[] resultArray = gson.fromJson(httpJsonResult, arrayKlass);
                        this.result = Arrays.asList(resultArray);
                        if (pageConfig != null) {
                            this.result = computePagedList(this.result, httpResponse, innerFilter.getWhere());
                        }
                    } else {
                        T resultObject = gson.fromJson(httpJsonResult, klass);
                        List<T> resultList = new ArrayList<T>(1);
                        resultList.add(resultObject);
                        this.result = resultList;
                        if (pageConfig != null) {
                            this.result = computePagedList(this.result, httpResponse, innerFilter.getWhere());
                        }
                    }
                } catch (Exception e) {
                    exception = e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void ignore) {
                super.onPostExecute(ignore);
                if (exception == null) {
                    callback.onSuccess(this.result);
                } else {
                    callback.onFailure(exception);
                }
            }
        }.execute();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void read(final Callback<List<T>> callback) {
        readWithFilter(null, callback);
    }

    @Override
    public void save(final T data, final Callback<T> callback) {
        final String id;

        try {
            String recordIdFieldName = Scan.recordIdFieldNameIn(data.getClass());
            Object result = new Property(data.getClass(), recordIdFieldName).getValue(data);
            id = result == null ? null : result.toString();
        } catch (Exception e) {
            callback.onFailure(e);
            return;
        }

        new AsyncTask<Void, Void, Void>() {
            T result = null;
            Exception exception = null;

            @Override
            protected Void doInBackground(Void... voids) {
                try {

                    String body = gson.toJson(data);
                    final HttpProvider httpProvider = getHttpProvider();

                    HeaderAndBody result;
                    if (id == null || id.length() == 0) {
                        result = httpProvider.post(body);
                    } else {
                        result = httpProvider.put(id, body);
                    }

                    this.result = gson.fromJson(new String(result.getBody(), encoding), klass);

                } catch (Exception e) {
                    exception = e;
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void ignore) {
                super.onPostExecute(ignore);
                if (exception == null) {
                    callback.onSuccess(this.result);
                } else {
                    callback.onFailure(exception);
                }
            }
        }.execute();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final String id, final Callback<Void> callback) {

        new AsyncTask<Void, Void, Void>() {
            Exception exception = null;

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    HttpProvider httpProvider = getHttpProvider();
                    httpProvider.delete(id);
                } catch (Exception e) {
                    exception = e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void ignore) {
                super.onPostExecute(ignore);
                if (exception == null) {
                    callback.onSuccess(null);
                } else {
                    callback.onFailure(exception);
                }
            }
        }.execute();

    }

    /**
     * This will return a class of the type T[] from a given class. When we read
     * from the AG pipe, Java needs a reference to a generic array type.
     *
     * @param klass
     * @return an array of klass with a length of 1
     */
    private Class<T[]> asArrayClass(Class<T> klass) {

        return (Class<T[]>) Array.newInstance(klass, 1).getClass();
    }

    private URL appendQuery(String query, URL baseURL) {
        try {
            URI baseURI = baseURL.toURI();
            String baseQuery = baseURI.getQuery();
            if (baseQuery == null || baseQuery.isEmpty()) {
                baseQuery = query;
            } else {
                if (query != null && !query.isEmpty()) {
                    baseQuery = baseQuery + "&" + query;
                }
            }

            return new URI(baseURI.getScheme(), baseURI.getUserInfo(), baseURI.getHost(), baseURI.getPort(), baseURI.getPath(), baseQuery, baseURI.getFragment()).toURL();
        } catch (MalformedURLException ex) {
            Log.e(TAG, "The URL could not be created from " + baseURL.toString(), ex);
            throw new RuntimeException(ex);
        } catch (URISyntaxException ex) {
            Log.e(TAG, "Error turning " + query + " into URI query.", ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void setAuthenticationModule(AuthenticationModule module) {
        this.authModule = module;
    }

    /**
     * Apply authentication if the token is present
     */
    private AuthorizationFields loadAuth() {

        if (authModule != null && authModule.isLoggedIn()) {
            return authModule.getAuthorizationFields();
        }

        return new AuthorizationFields();
    }

    /**
     * Sets the encoding of the Pipe. May not be null.
     *
     * @param encoding
     * @throws IllegalArgumentException if encoding is null
     */
    public void setEncoding(Charset encoding) {
        this.encoding = encoding;
    }

    /**
     *
     * @param queryParameters
     * @return a url with query params added
     */
    private URL addAuthorization(List<Pair<String, String>> queryParameters, URL baseURL) {

        StringBuilder queryBuilder = new StringBuilder();

        String amp = "";
        for (Pair<String, String> parameter : queryParameters) {
            try {
                queryBuilder.append(amp)
                            .append(URLEncoder.encode(parameter.first, "UTF-8"))
                            .append("=")
                            .append(URLEncoder.encode(parameter.second, "UTF-8"));

                amp = "&";
            } catch (UnsupportedEncodingException ex) {
                Log.e(TAG, "UTF-8 encoding is not supported.", ex);
                throw new RuntimeException(ex);

            }
        }

        return appendQuery(queryBuilder.toString(), baseURL);

    }

    private void addAuthHeaders(HttpProvider httpProvider, AuthorizationFields fields) {
        List<Pair<String, String>> authHeaders = fields.getHeaders();

        for (Pair<String, String> header : authHeaders) {
            httpProvider.setDefaultHeader(header.first, header.second);
        }

    }

    private HttpProvider getHttpProvider() {
        return getHttpProvider(URI.create(""));
    }

    private HttpProvider getHttpProvider(URI relativeUri) {
        try {
            AuthorizationFields fields = loadAuth();

            URL authorizedURL = addAuthorization(fields.getQueryParameters(), URIUtils.resolve(baseURL.toURI(), relativeUri).toURL());

            final HttpProvider httpProvider = httpProviderFactory.get(authorizedURL);
            addAuthHeaders(httpProvider, fields);
            return httpProvider;
        } catch (MalformedURLException ex) {
            Log.e(TAG, "error resolving " + baseURL + " with " + relativeUri, ex);
            throw new RuntimeException(ex);
        } catch (URISyntaxException ex) {
            Log.e(TAG, "error resolving " + baseURL + " with " + relativeUri, ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * 
     * This method checks for paging information and returns the appropriate data
     * 
     * @param result
     * @param httpResponse
     * @param where
     * @return a {@link WrappingPagedList} if there is paging, result if not.
     */
    private List<T> computePagedList(List<T> result, HeaderAndBody httpResponse, JSONObject where) {
        ReadFilter previousRead = null;
        ReadFilter nextRead = null;

        if (PageConfig.MetadataLocations.WEB_LINKING.equals(pageConfig.getMetadataLocation())) {
            String webLinksRaw = "";
            final String relHeader = "rel";
            final String nextIdentifier = pageConfig.getNextIdentifier();
            final String prevIdentifier = pageConfig.getPreviousIdentifier();
            try {
                webLinksRaw = getWebLinkHeader(httpResponse);
                if (webLinksRaw == null) { //no paging, return result
                    return result;
                }
                List<WebLink> webLinksParsed = WebLinkParser.parse(webLinksRaw);
                for (WebLink link : webLinksParsed) {
                    if (nextIdentifier.equals(link.getParameters().get(relHeader))) {
                        nextRead = new ReadFilter();
                        nextRead.setLinkUri(new URI(link.getUri()));
                    } else if (prevIdentifier.equals(link.getParameters().get(relHeader))) {
                        previousRead = new ReadFilter();
                        previousRead.setLinkUri(new URI(link.getUri()));
                    }

                }
            } catch (URISyntaxException ex) {
                Log.e(TAG, webLinksRaw + " did not contain a valid context URI", ex);
                throw new RuntimeException(ex);
            } catch (ParseException ex) {
                Log.e(TAG, webLinksRaw + " could not be parsed as a web link header", ex);
                throw new RuntimeException(ex);
            }
        } else if (pageConfig.getMetadataLocation().equals(PageConfig.MetadataLocations.HEADERS)) {
            nextRead = pageConfig.getPageHeaderParser().getNextFilter(httpResponse, RestAdapter.this.pageConfig);
            previousRead = pageConfig.getPageHeaderParser().getPreviousFilter(httpResponse, RestAdapter.this.pageConfig);
        } else if (pageConfig.getMetadataLocation().equals(PageConfig.MetadataLocations.BODY)) {
            nextRead = pageConfig.getPageHeaderParser().getNextFilter(httpResponse, RestAdapter.this.pageConfig);
            previousRead = pageConfig.getPageHeaderParser().getPreviousFilter(httpResponse, RestAdapter.this.pageConfig);
        } else {
            throw new IllegalStateException("Not supported");
        }
        if (nextRead != null) {
            nextRead.setWhere(where);
        }

        if (previousRead != null) {
            previousRead.setWhere(where);
        }

        return new WrappingPagedList<T>(this, result, nextRead, previousRead);
    }

    private String getWebLinkHeader(HeaderAndBody httpResponse) {
        String linkHeaderName = "Link";
        Object header = httpResponse.getHeader(linkHeaderName);
        if (header != null) {
            return header.toString();
        }
        return null;
    }

    public String getDataRoot() {
        return dataRoot;
    }

    protected void setDataRoot(String dataRoot) {
        this.dataRoot = dataRoot;
    }

    private JsonElement getResultElement(JsonElement element, String dataRoot) {
        String[] identifiers = dataRoot.split("\\.");
        for (String identifier : identifiers) {
            JsonElement newElement = element.getAsJsonObject().get(identifier);
            if (newElement == null) {
                return element;
            } else {
                element = newElement;
            }
        }
        return element;
    }

    public ParameterProvider getParameterProvider() {
        return parameterProvider;
    }

    protected void setParameterProvider(ParameterProvider parameterProvider) {
        this.parameterProvider = parameterProvider;
    }

}
