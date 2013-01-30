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

import org.jboss.aerogear.android.impl.pipeline.paging.WrappingPagedList;
import android.graphics.Point;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import junit.framework.Assert;
import org.jboss.aerogear.android.http.HeaderAndBody;
import org.jboss.aerogear.android.http.HttpProvider;
import org.jboss.aerogear.android.impl.http.HttpStubProvider;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;

import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.Pipeline;
import org.jboss.aerogear.android.Provider;
import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.RecordId;
import org.jboss.aerogear.android.authentication.AuthenticationModule;
import org.jboss.aerogear.android.authentication.AuthorizationFields;
import org.jboss.aerogear.android.impl.core.HttpProviderFactory;
import org.jboss.aerogear.android.impl.helper.Data;
import org.jboss.aerogear.android.impl.helper.UnitTestUtils;
import org.jboss.aerogear.android.pipeline.Pipe;
import org.jboss.aerogear.android.pipeline.paging.PageConfig;
import org.jboss.aerogear.android.pipeline.paging.PagedList;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class RestAdapterTest {

    private static final String TAG = RestAdapterTest.class.getSimpleName();
    private static final String SERIALIZED_POINTS = "{\"points\":[{\"x\":0,\"y\":0},{\"x\":1,\"y\":2},{\"x\":2,\"y\":4},{\"x\":3,\"y\":6},{\"x\":4,\"y\":8},{\"x\":5,\"y\":10},{\"x\":6,\"y\":12},{\"x\":7,\"y\":14},{\"x\":8,\"y\":16},{\"x\":9,\"y\":18}],\"id\":\"1\"}";
    private URL url;
    private final Provider<HttpProvider> stubHttpProviderFactory = new Provider<HttpProvider>() {

        @Override
        public HttpProvider get(Object... in) {
            return new HttpStubProvider((URL) in[0]);
        }
    };

    @Before
    public void setup() throws MalformedURLException {
        url = new URL("http://server.com/context/");
    }

    @Test
    public void testPipeTypeProperty() throws Exception {
        Pipe<Data> restPipe = new RestAdapter<Data>(Data.class, url);
        UnitTestUtils.setPrivateField(restPipe, "httpProviderFactory", stubHttpProviderFactory);
        Assert.assertEquals("verifying the (default) type", PipeTypes.REST, restPipe.getType());
    }

    @Test
    public void testPipeURLProperty() throws Exception {
        Pipe<Data> restPipe = new RestAdapter<Data>(Data.class, url);
        UnitTestUtils.setPrivateField(restPipe, "httpProviderFactory", stubHttpProviderFactory);
        assertEquals("verifying the given URL", "http://server.com/context/", restPipe.getUrl().toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPipeFactoryPipeConfigEncoding() {
        PipeConfig config = new PipeConfig(url, Data.class);
        config.setEncoding("UTF-16");
        assertEquals(Charset.forName("UTF-16"), config.getEncoding());
        config.setEncoding((Charset) null);
    }

    @Test
    public void testPipeFactoryPipeConfigGson() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(Point.class, new RestAdapterTest.PointTypeAdapter());

        DefaultPipeFactory factory = new DefaultPipeFactory();
        PipeConfig pc = new PipeConfig(url, ListClassId.class);

        pc.setGsonBuilder(builder);
        Pipe<ListClassId> restPipe = factory.createPipe(ListClassId.class, pc);

        Field gsonField = restPipe.getClass().getDeclaredField("gson");
        gsonField.setAccessible(true);
        Gson gson = (Gson) gsonField.get(restPipe);

        gson.toJson(new ListClassId());

    }

    @Test(timeout = 500L)
    public void testEncoding() throws Exception {
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(Point.class, new RestAdapterTest.PointTypeAdapter());
        final Charset utf_16 = Charset.forName("UTF-16");

        final HttpStubProvider provider = new HttpStubProvider(url, new HeaderAndBody(SERIALIZED_POINTS.getBytes(utf_16), new HashMap<String, Object>()));

        RestAdapter<ListClassId> restPipe = new RestAdapter<ListClassId>(ListClassId.class, url, builder);

        UnitTestUtils.setPrivateField(restPipe, "httpProviderFactory", new Provider<HttpProvider>() {

            @Override
            public HttpProvider get(Object... in) {
                return provider;
            }
        });
        restPipe.setEncoding(utf_16);

        runRead(restPipe);

    }

    @Test(timeout = 500L)
    public void testConfigSetEncoding() throws Exception {
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(
                Point.class, new RestAdapterTest.PointTypeAdapter());
        final Charset utf_16 = Charset.forName("UTF-16");

        Pipeline pipeline = new Pipeline(url);
        PipeConfig config = new PipeConfig(url, ListClassId.class);
        config.setEncoding(utf_16);
        config.setGsonBuilder(builder);

        RestAdapter<ListClassId> restPipe = (RestAdapter<ListClassId>) pipeline
                .pipe(ListClassId.class, config);

        assertEquals(utf_16, UnitTestUtils.getPrivateField(restPipe, "encoding"));

    }

    @Test
    public void testSingleObjectRead() throws Exception {
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(Point.class, new RestAdapterTest.PointTypeAdapter());
        HeaderAndBody response = new HeaderAndBody(SERIALIZED_POINTS.getBytes(), new HashMap<String, Object>());
        final HttpStubProvider provider = new HttpStubProvider(url, response);
        RestAdapter<ListClassId> restPipe = new RestAdapter<ListClassId>(ListClassId.class, url, builder);
        UnitTestUtils.setPrivateField(restPipe, "httpProviderFactory", new Provider<HttpProvider>() {

            @Override
            public HttpProvider get(Object... in) {
                return provider;
            }
        });
        List<ListClassId> result = runRead(restPipe);

        List<Point> returnedPoints = result.get(0).points;
        assertEquals(10, returnedPoints.size());

    }

    @Test
    public void testSingleObjectReadWithNestedResult() throws Exception {
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(Point.class, new RestAdapterTest.PointTypeAdapter());
        HeaderAndBody response = new HeaderAndBody(("{\"result\":{\"points\":" + SERIALIZED_POINTS + "}}").getBytes(), new HashMap<String, Object>());
        final HttpStubProvider provider = new HttpStubProvider(url, response);
        RestAdapter<ListClassId> restPipe = new RestAdapter<ListClassId>(ListClassId.class, url, builder);
        restPipe.setDataRoot("result.points");
        UnitTestUtils.setPrivateField(restPipe, "httpProviderFactory", new Provider<HttpProvider>() {

            @Override
            public HttpProvider get(Object... in) {
                return provider;
            }
        });
        List<ListClassId> result = runRead(restPipe);

        List<Point> returnedPoints = result.get(0).points;
        assertEquals(10, returnedPoints.size());

    }

    @Test
    public void testGsonBuilderProperty() throws Exception {
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(Point.class, new RestAdapterTest.PointTypeAdapter());

        final StringBuilder request = new StringBuilder("");

        final HttpStubProvider provider = new HttpStubProvider(url) {
            @Override
            public HeaderAndBody put(String id, String data) {
                request.delete(0, request.length());
                request.append(data);
                return new HeaderAndBody(data.getBytes(), new HashMap<String, Object>());
            }

            @Override
            public HeaderAndBody post(String data) {
                request.delete(0, request.length());
                request.append(data);
                return new HeaderAndBody(data.getBytes(), new HashMap<String, Object>());
            }
        };

        Pipe<ListClassId> restPipe = new RestAdapter<ListClassId>(ListClassId.class, url, builder);

        UnitTestUtils.setPrivateField(restPipe, "httpProviderFactory", new Provider<HttpProvider>() {

            @Override
            public HttpProvider get(Object... in) {
                return provider;
            }
        });

        final CountDownLatch latch = new CountDownLatch(1);
        final ListClassId listClass = new ListClassId(true);
        final List<Point> returnedPoints = new ArrayList<Point>(10);

        restPipe.save(listClass, new Callback<ListClassId>() {
            @Override
            public void onSuccess(ListClassId data) {
                returnedPoints.addAll(data.points);
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                throw new RuntimeException(e);
            }
        });

        latch.await(2, TimeUnit.SECONDS);
        assertEquals(SERIALIZED_POINTS, request.toString());
        assertEquals(listClass.points, returnedPoints);
    }

    @Test
    public void runReadWithFilterUsingUri() throws Exception {

        HttpProviderFactory factory = mock(HttpProviderFactory.class);
        when(factory.get(anyObject())).thenReturn(mock(HttpProvider.class));

        RestAdapter<Data> adapter = new RestAdapter<Data>(Data.class, url);
        UnitTestUtils.setPrivateField(adapter, "httpProviderFactory", factory);

        ReadFilter filter = new ReadFilter();
        filter.setLinkUri(URI.create("?limit=10&where=%7B%22model%22:%22BMW%22%7D&token=token"));

        adapter.readWithFilter(filter, new Callback<List<Data>>() {

            @Override
            public void onSuccess(List<Data> data) {
            }

            @Override
            public void onFailure(Exception e) {
            }
        });

        verify(factory).get(eq(new URL(url.toString() + "?limit=10&where=%7B%22model%22:%22BMW%22%7D&token=token")));
    }

    @Test
    public void runReadWithFilterAndAuthenticaiton() throws Exception {

        HttpProviderFactory factory = mock(HttpProviderFactory.class);
        when(factory.get(anyObject())).thenReturn(mock(HttpProvider.class));

        AuthorizationFields authFields = new AuthorizationFields();
        authFields.addQueryParameter("token", "token");

        AuthenticationModule urlModule = mock(AuthenticationModule.class);
        when(urlModule.isLoggedIn()).thenReturn(true);
        when(urlModule.getAuthorizationFields()).thenReturn(authFields);

        RestAdapter<Data> adapter = new RestAdapter<Data>(Data.class, url);
        UnitTestUtils.setPrivateField(adapter, "httpProviderFactory", factory);

        ReadFilter filter = new ReadFilter();
        filter.setLimit(10);
        filter.setWhere(new JSONObject("{\"model\":\"BMW\"}"));

        adapter.setAuthenticationModule(urlModule);

        adapter.readWithFilter(filter, new Callback<List<Data>>() {

            @Override
            public void onSuccess(List<Data> data) {
            }

            @Override
            public void onFailure(Exception e) {
                Logger.getLogger(getClass().getSimpleName()).log(Level.SEVERE, TAG, e);
            }
        });

        verify(factory).get(new URL(url.toString() + "?limit=10&where=%7B%22model%22:%22BMW%22%7D&token=token"));
    }

    /**
     * This test tests the default paging configuration.
     */
    @Test()
    public void testLinkPagingReturnsData() throws InterruptedException, NoSuchFieldException, NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException, Exception {
        Pipeline pipeline = new Pipeline(url);

        final HttpStubProvider provider = new HttpStubProvider(url, new HeaderAndBody(SERIALIZED_POINTS.getBytes(), new HashMap<String, Object>()));

        PageConfig pageConfig = new PageConfig();
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(Point.class, new RestAdapterTest.PointTypeAdapter());

        PipeConfig pipeConfig = new PipeConfig(url, ListClassId.class);
        pipeConfig.setGsonBuilder(builder);
        pipeConfig.setPageConfig(pageConfig);

        Pipe<ListClassId> dataPipe = pipeline.pipe(ListClassId.class, pipeConfig);

        UnitTestUtils.setPrivateField(dataPipe, "httpProviderFactory", new Provider<HttpProvider>() {

            @Override
            public HttpProvider get(Object... in) {
                return provider;
            }
        });

        ReadFilter onePageFilter = new ReadFilter();

        onePageFilter.setLimit(1);
        runRead(dataPipe, onePageFilter);
        List<ListClassId> result = runRead(dataPipe, onePageFilter);

        assertNotNull(result);
        assertFalse(result instanceof PagedList);

    }

    /**
     * This test tests the default paging configuration.
     */
    @Test
    public void testDefaultPaging() throws InterruptedException, NoSuchFieldException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException,
            URISyntaxException {
        Pipeline pipeline = new Pipeline(url);

        PageConfig pageConfig = new PageConfig();
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(Point.class, new RestAdapterTest.PointTypeAdapter());

        PipeConfig pipeConfig = new PipeConfig(url, ListClassId.class);
        pipeConfig.setGsonBuilder(builder);
        pipeConfig.setPageConfig(pageConfig);

        Pipe<ListClassId> dataPipe = pipeline.pipe(ListClassId.class, pipeConfig);

        UnitTestUtils.setPrivateField(dataPipe, "httpProviderFactory", new Provider<HttpProvider>() {

            @Override
            public HttpProvider get(Object... in) {
                HashMap<String, Object> headers = new HashMap<String, Object>(1);
                headers
                        .put(
                                "Link",
                                "<http://example.com/TheBook/chapter2>; rel=\"previous\";title=\"previous chapter\",<http://example.com/TheBook/chapter3>; rel=\"next\";title=\"next chapter\"");
                HttpStubProvider provider = new HttpStubProvider(url, new HeaderAndBody(SERIALIZED_POINTS.getBytes(), headers));

                return provider;
            }
        });

        ReadFilter onePageFilter = new ReadFilter();
        onePageFilter.setLimit(1);
        List<ListClassId> resultList = runRead(dataPipe, onePageFilter);
        assertTrue(resultList instanceof PagedList);
        WrappingPagedList<ListClassId> pagedList = (WrappingPagedList<ListClassId>) resultList;

        assertEquals(new URI("http://example.com/TheBook/chapter3"), pagedList.getNextFilter().getLinkUri());
        assertEquals(new URI("http://example.com/TheBook/chapter2"), pagedList.getPreviousFilter().getLinkUri());
    }

    @Test
    public void testBuildPagedResultsFromHeaders() throws Exception {
        PageConfig pageConfig = new PageConfig();
        pageConfig.setMetadataLocation(PageConfig.MetadataLocations.HEADERS);

        RestAdapter adapter = new RestAdapter(Data.class, url, new GsonBuilder(), pageConfig);
        List<Data> list = new ArrayList<Data>();
        HeaderAndBody response = new HeaderAndBody(new byte[] {}, new HashMap<String, Object>() {
            {
                put("next", "chapter3");
                put("previous", "chapter2");
            }
        });
        JSONObject where = new JSONObject();
        Method method = adapter.getClass().getDeclaredMethod("computePagedList", List.class, HeaderAndBody.class, JSONObject.class);
        method.setAccessible(true);

        WrappingPagedList<Data> pagedList = (WrappingPagedList<Data>) method.invoke(adapter, list, response, where);
        assertEquals(new URI("http://server.com/context/chapter3"), pagedList.getNextFilter().getLinkUri());
        assertEquals(new URI("http://server.com/context/chapter2"), pagedList.getPreviousFilter().getLinkUri());

    }

    @Test
    public void testBuildPagedResultsFromBody() throws Exception {
        PageConfig pageConfig = new PageConfig();
        pageConfig.setMetadataLocation(PageConfig.MetadataLocations.BODY);
        pageConfig.setNextIdentifier("pages.next");
        pageConfig.setPreviousIdentifier("pages.previous");
        RestAdapter adapter = new RestAdapter(Data.class, url, new GsonBuilder(), pageConfig);
        List<Data> list = new ArrayList<Data>();
        HeaderAndBody response = new HeaderAndBody("{\"pages\":{\"next\":\"chapter3\",\"previous\":\"chapter2\"}}".getBytes(), new HashMap<String, Object>());
        JSONObject where = new JSONObject();
        Method method = adapter.getClass().getDeclaredMethod("computePagedList", List.class, HeaderAndBody.class, JSONObject.class);
        method.setAccessible(true);

        WrappingPagedList<Data> pagedList = (WrappingPagedList<Data>) method.invoke(adapter, list, response, where);
        assertEquals(new URI("http://server.com/context/chapter3"), pagedList.getNextFilter().getLinkUri());
        assertEquals(new URI("http://server.com/context/chapter2"), pagedList.getPreviousFilter().getLinkUri());

    }

    private <T> List<T> runRead(Pipe<T> restPipe) throws InterruptedException {
        return runRead(restPipe, null);
    }

    /**
     * Runs a read method, returns the result of the call back and makes sure no
     * exceptions are thrown
     *
     * @param restPipe
     */
    private <T> List<T> runRead(Pipe<T> restPipe, ReadFilter readFilter) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean hasException = new AtomicBoolean(false);
        final AtomicReference<List<T>> resultRef = new AtomicReference<List<T>>();

        restPipe.readWithFilter(readFilter, new Callback<List<T>>() {
            @Override
            public void onSuccess(List<T> data) {
                resultRef.set(data);
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                hasException.set(true);
                Logger.getLogger(RestAdapterTest.class.getSimpleName()).log(Level.SEVERE, e.getMessage(), e);
                latch.countDown();
            }
        });

        latch.await(2, TimeUnit.SECONDS);
        Assert.assertFalse(hasException.get());

        return resultRef.get();
    }

    /**
     * Runs a read method, returns the result of the call back and rethrows the underlying exception
     *
     * @param restPipe
     */
    private <T> List<T> runReadForException(Pipe<T> restPipe, ReadFilter readFilter) throws InterruptedException, Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean hasException = new AtomicBoolean(false);
        final AtomicReference<Exception> exceptionref = new AtomicReference<Exception>();
        restPipe.readWithFilter(readFilter, new Callback<List<T>>() {
            @Override
            public void onSuccess(List<T> data) {
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                hasException.set(true);
                exceptionref.set(e);
                latch.countDown();
            }
        });

        latch.await(2, TimeUnit.SECONDS);
        Assert.assertTrue(hasException.get());

        throw exceptionref.get();
    }

    public final static class ListClassId {

        List<Point> points = new ArrayList<Point>(10);
        @RecordId
        String id = "1";

        public ListClassId(boolean build) {
            if (build) {
                for (int i = 0; i < 10; i++) {
                    points.add(new Point(i, i * 2));
                }
            }
        }

        public ListClassId() {

        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object obj) {
            try {
                return points.equals(((ListClassId) obj).points);
            } catch (Throwable ignore) {
                return false;
            }
        }
    }

    private static class PointTypeAdapter implements InstanceCreator, JsonSerializer, JsonDeserializer {

        @Override
        public Object createInstance(Type type) {
            return new Point();
        }

        @Override
        public JsonElement serialize(Object src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            object.addProperty("x", ((Point) src).x);
            object.addProperty("y", ((Point) src).y);
            return object;
        }

        @Override
        public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new Point(json.getAsJsonObject().getAsJsonPrimitive("x").getAsInt(),
                    json.getAsJsonObject().getAsJsonPrimitive("y").getAsInt());
        }
    }
}
