/**
 * JBoss, Home of Professional Open Source Copyright Red Hat, Inc., and
 * individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.jboss.aerogear.android.impl.pipeline;

import java.net.URL;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.pipeline.Pipe;
import org.jboss.aerogear.android.pipeline.PipeHandler;
import org.jboss.aerogear.android.pipeline.PipeType;
import org.jboss.aerogear.android.pipeline.RequestBuilder;
import org.jboss.aerogear.android.pipeline.ResponseParser;

import android.util.Log;

import org.jboss.aerogear.android.http.HeaderAndBody;
import org.jboss.aerogear.android.impl.reflection.Property;
import org.jboss.aerogear.android.impl.reflection.Scan;

/**
 * Rest implementation of {@link Pipe}.
 */
public final class RestAdapter<T> implements Pipe<T> {

    private static final String TAG = RestAdapter.class.getSimpleName();
    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 64;
    private static final int KEEP_ALIVE = 1;
    private static final BlockingQueue<Runnable> WORK_QUEUE = new LinkedBlockingQueue<Runnable>(10);
    public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE,
            TimeUnit.SECONDS, WORK_QUEUE);

    /**
     * A class of the Generic type this pipe wraps. This is used by GSON for
     * deserializing.
     */
    private final Class<T> klass;
    /**
     * A class of the Generic collection type this pipe wraps. This is used by
     * JSON for deserializing collections.
     */
    private final URL url;
    private final PipeHandler<T> restRunner;
    private final RequestBuilder<T> requestBuilder;
    private final ResponseParser<T> responseParser;

    /**
     *
     * This will configure the Adapter as with sane RESTful defaults.
     *
     * @param klass The type that this adapter will consume and produce
     * @param absoluteURL the RESTful URL endpoint.
     */
    public RestAdapter(Class<T> klass, URL absoluteURL) {
        this.restRunner = new RestRunner<T>(klass, absoluteURL);
        this.klass = klass;
        this.url = absoluteURL;
        this.requestBuilder = new GsonRequestBuilder<T>();
        this.responseParser = new GsonResponseParser<T>();
    }

    /**
     *
     * This will build an adapter based on a configuration.
     *
     * @param klass The type that this adapter will consume and produce
     * @param absoluteURL the RESTful URL endpoint.
     * @param config A PipeConfig to use. NOTE: the URL's provided in the config
     * are ignored in deference to the absoluteURL parameter.
     */
    @SuppressWarnings("unchecked")
    public RestAdapter(Class<T> klass, URL absoluteURL, PipeConfig config) {
        this.klass = klass;
        this.url = absoluteURL;

        this.requestBuilder = config.getRequestBuilder();
        this.responseParser = config.getResponseParser();

        if (config.getHandler() != null) {
            this.restRunner = (PipeHandler<T>) config.getHandler();
        } else {
            this.restRunner = new RestRunner<T>(klass, absoluteURL, config);
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
        return url;
    }

    @Override
    public void read(ReadFilter filter, final Callback<List<T>> callback) {
        if (filter == null) {
            filter = new ReadFilter();
        }
        final ReadFilter innerFilter = filter;

        THREAD_POOL_EXECUTOR.execute(new Runnable() {
            List<T> result = null;
            Exception exception = null;

            @Override
            public void run() {
                try {
                    HeaderAndBody response = restRunner.onRawReadWithFilter(innerFilter, RestAdapter.this);
                    this.result = getResponseParser().handleResponse(response, klass);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                    this.exception = e;
                }
                if (exception == null) {
                    callback.onSuccess(this.result);
                } else {
                    callback.onFailure(exception);
                }
            }
        });

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void read(final Callback<List<T>> callback) {
        THREAD_POOL_EXECUTOR.execute(new Runnable() {
            List<T> result = null;
            Exception exception = null;

            @Override
            public void run() {
                try {
                    this.result = getResponseParser().handleResponse(restRunner.onRawRead(RestAdapter.this), klass);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                    this.exception = e;
                }
                if (exception == null) {
                    callback.onSuccess(this.result);
                } else {
                    callback.onFailure(exception);
                }
            }
        });
    }

    @Override
    public void save(final T data, final Callback<T> callback) {

        THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                T result = null;
                Exception exception = null;

                try {
                    String id;

                    String recordIdFieldName = Scan.recordIdFieldNameIn(data.getClass());
                    Object idObject = new Property(data.getClass(), recordIdFieldName).getValue(data);
                    id = idObject == null ? null : idObject.toString();

                    byte[] body = requestBuilder.getBody(data);
                    
                    HeaderAndBody response = restRunner.onRawSave(id, body);
                    
                    result = getResponseParser().handleResponse(response, klass).get(0);
                } catch (Exception e) {
                    exception = e;
                }

                if (exception == null) {
                    callback.onSuccess(result);
                } else {
                    callback.onFailure(exception);
                }
            }
        });

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final String id, final Callback<Void> callback) {

        THREAD_POOL_EXECUTOR.execute(new Runnable() {
            Exception exception = null;

            @Override
            public void run() {
                try {
                    RestAdapter.this.restRunner.onRemove(id);
                } catch (Exception e) {
                    exception = e;
                }
                if (exception == null) {
                    callback.onSuccess(null);
                } else {
                    callback.onFailure(exception);
                }
            }
        });

    }

    @Override
    public PipeHandler<T> getHandler() {
        return restRunner;
    }

    @Override
    public Class<T> getKlass() {
        return klass;
    }

    @Override
    public RequestBuilder<T> getRequestBuilder() {
        return this.requestBuilder;
    }

    @Override
    public ResponseParser<T> getResponseParser() {
        return this.responseParser;
    }

}
