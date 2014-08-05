package org.jboss.aerogear.android.impl.pipeline;

import java.net.MalformedURLException;
import java.net.URL;
import static junit.framework.Assert.assertEquals;
import org.jboss.aerogear.android.ConfigurationProvider;
import org.jboss.aerogear.android.authentication.AuthenticationModule;
import org.jboss.aerogear.android.authentication.impl.HttpBasicAuthenticationModule;
import org.jboss.aerogear.android.authorization.AuthzModule;
import org.jboss.aerogear.android.code.PipeModule;
import org.jboss.aerogear.android.impl.helper.Data;
import static org.jboss.aerogear.android.impl.helper.UnitTestUtils.getPrivateField;
import static org.jboss.aerogear.android.impl.pipeline.PipeTypes.REST;
import org.jboss.aerogear.android.impl.util.UrlUtils;
import org.jboss.aerogear.android.pipeline.Pipe;
import org.jboss.aerogear.android.pipeline.PipeConfiguration;
import org.jboss.aerogear.android.pipeline.PipeManager;
import org.jboss.aerogear.android.pipeline.RequestBuilder;
import org.jboss.aerogear.android.pipeline.ResponseParser;
import org.jboss.aerogear.android.pipeline.paging.PageConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.mock;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class PipeManagerTest {

    private URL url;

    @Before
    public void setup() throws MalformedURLException {
        url = new URL("http://server.com/context/");
    }

    @Test
    public void defaultPipeSetup() {

        Pipe newPipe = PipeManager.config("data", RestfulPipeConfiguration.class ).withUrl(url).forClass(Data.class);

        assertEquals("verifying the given URL", "http://server.com/context/data", newPipe.getUrl().toString());
        assertEquals("verifying the type", REST, newPipe.getType());
        assertEquals(newPipe, PipeManager.getPipe("data"));
    }
    

    @Test(expected = IllegalStateException.class)
    public void nullUrlCantHappen() {

        Pipe newPipe = PipeManager.config("data", RestfulPipeConfiguration.class ).withUrl(null).forClass(Data.class);

        Assert.assertNotNull(newPipe.getUrl());
        
    }    

    /**
     * This test will show that the Pipe URL Creation can be overriden
     */
    @Test
    public void customPipeSetup() {
        PipeManager.registerConfigurationProvider(IStubPipeConfiguration.class, new IStubPipeConfigurationProvider());
        Pipe newPipe = PipeManager.config("custom", IStubPipeConfiguration.class).withUrl(url).id(5).forClass(Data.class);

        assertEquals("verifying the given URL", "http://server.com/context/data/5", newPipe.getUrl().toString());
        assertEquals(newPipe, PipeManager.getPipe("custom"));

    }

    /**
     * This test will show that auth can be added to a pipe.
     *
     * @throws java.lang.NoSuchFieldException thrown by Java, shouldn't happen if the test works.
     * @throws java.lang.IllegalArgumentException thrown by Java, shouldn't happen if the test works.
     * @throws java.lang.IllegalAccessException thrown by Java, shouldn't happen if the test works.
     */
    @Test
    public void addAuthToPipe() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        AuthenticationModule basicModule = new HttpBasicAuthenticationModule(url);

        Pipe newPipe = PipeManager.config("auth", RestfulPipeConfiguration.class).withUrl(url).module(basicModule).forClass(Data.class);

        RestRunner runner = getPrivateField(newPipe, "restRunner", RestRunner.class);
        AuthenticationModule module = getPrivateField(runner, "authModule", AuthenticationModule.class);

        //For now we will assume that if the module is set correctly then 
        //restRunner will call it correctly.
        assertEquals(basicModule, module);

        /*
         TODO: Eventually we will want to break the dependency on the 
         AuthenticationModule type from RestRunner.  This will happen 
         during modularization and this test will be rewritten
         */
    }

    /**
     * This test will show that authz can be added to a pipe.
     * 
     * @throws java.lang.NoSuchFieldException thrown by Java, shouldn't happen if the test works.
     * @throws java.lang.IllegalArgumentException thrown by Java, shouldn't happen if the test works.
     * @throws java.lang.IllegalAccessException thrown by Java, shouldn't happen if the test works.
     */
    @Test
    @Ignore(value = "Authz hasn't been converted to module yet.")
    public void addAuthzToPipe() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        AuthzModule authzModule = mock(AuthzModule.class);

        Pipe newPipe = PipeManager.config( "auth", RestfulPipeConfiguration.class).withUrl(url).module((PipeModule)authzModule).forClass(Data.class);

        RestRunner runner = getPrivateField(newPipe, "restRunner", RestRunner.class);
        AuthzModule module = getPrivateField(runner, "authzModule", AuthzModule.class);

        //For now we will assume that if the module is set correctly then 
        //restRunner will call it correctly.
        assertEquals(authzModule, module);

        /*
         TODO: Eventually we will want to break the dependency on the 
         AuthzModule type from RestRunner.  This will happen 
         during modularization and this test will be rewritten
         */    
    }

    /**
     * This test will show paging handling can be added to a Pipeline
     *
     * @throws java.lang.NoSuchFieldException thrown by Java, shouldn't happen if the test works.
     * @throws java.lang.IllegalArgumentException thrown by Java, shouldn't happen if the test works.
     * @throws java.lang.IllegalAccessException thrown by Java, shouldn't happen if the test works.
     * 
     */
    @Test
    public void addPagingToPipe() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        PageConfig pageConfig = new PageConfig();
        Pipe newPipe = PipeManager.config( "auth", RestfulPipeConfiguration.class).withUrl(url).pageConfig(pageConfig).forClass(Data.class);
        PageConfig appliedConfig = getPrivateField(newPipe, "pageConfig", PageConfig.class);
        
        assertEquals(pageConfig, appliedConfig);
    }

    /**
     * This test will show that the upload mechanism can be swapped to
     * multipart.
     * 
     * @throws java.lang.NoSuchFieldException thrown by Java, shouldn't happen if the test works.
     * @throws java.lang.IllegalArgumentException thrown by Java, shouldn't happen if the test works.
     * @throws java.lang.IllegalAccessException thrown by Java, shouldn't happen if the test works.
     */
    @Test
    public void addMultipartToPipe() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        RequestBuilder multipartBuilder = new MultipartRequestBuilder();
        Pipe newPipe = PipeManager.config( "auth", RestfulPipeConfiguration.class).withUrl(url).requestBuilder(multipartBuilder).forClass(Data.class);
        
        RestRunner runner = getPrivateField(newPipe, "restRunner", RestRunner.class);
        RequestBuilder runnerBuilder = runner.getRequestBuilder();
        assertEquals(multipartBuilder, runnerBuilder);
        
        /*
        TODO: What should happen when requestBuilders are chained?
         */
    }

    private static class IStubPipeConfiguration extends PipeConfiguration<IStubPipeConfiguration> {
        private int id;
        private URL url;

        public IStubPipeConfiguration id(int id) {
            this.id = id;
            return this;
        }

        @Override
        public <DATA> Pipe<DATA> buildPipeForClass(Class<DATA> aClass) {
            return new RestAdapter<DATA>(aClass, UrlUtils.appendToBaseURL(url, aClass.getSimpleName().toLowerCase() + "/" + id + ""));
        }

        @Override
        public IStubPipeConfiguration withUrl(URL url) {
            this.url = url;
            return this;
        }

        @Override
        public IStubPipeConfiguration module(PipeModule module) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public IStubPipeConfiguration pageConfig(PageConfig pageConfig) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public IStubPipeConfiguration requestBuilder(RequestBuilder multipartBuilder) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public IStubPipeConfiguration timeout(Integer timeout) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public IStubPipeConfiguration responseParser(ResponseParser responseParser) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
        
        
    }

    private static class IStubPipeConfigurationProvider  implements ConfigurationProvider<IStubPipeConfiguration> {

        public IStubPipeConfigurationProvider() {
        }

        @Override
        public IStubPipeConfiguration newConfiguration() {
            return new IStubPipeConfiguration();
        }
    }

}
