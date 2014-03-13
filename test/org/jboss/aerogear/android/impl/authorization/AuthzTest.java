package org.jboss.aerogear.android.impl.authorization;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import com.google.common.collect.Lists;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.authorization.AuthzConfig;
import org.jboss.aerogear.android.impl.authz.AGAuthzService;
import org.jboss.aerogear.android.impl.authz.AGRestAuthzModule;
import org.jboss.aerogear.android.impl.authz.OAUTH2AuthzSession;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class AuthzTest {

    public void testParcleOAUTH2AuthzSession() {
        OAUTH2AuthzSession session = new OAUTH2AuthzSession();
        session.setAccessToken("accessToken");
        session.setAccountId("accountId");
        session.setAuthorizationCode("authzCode");
        session.setExpires_on(92314872492734897l);
        session.setRefreshToken("refreshToken");
        session.setCliendId("testClientId");
        Parcel dest = Parcel.obtain();
        session.writeToParcel(dest, 0);
        
        assertEquals(session, OAUTH2AuthzSession.CREATOR.createFromParcel(dest));
        
    }
    
    @Test
    public void testAddAccounts() {
        Intent accessIntent = new Intent(Robolectric.application, AGAuthzService.class);
        accessIntent.setData(Uri.parse("https://accounts.google.com/o/oauth2/auth?scope=testScope&redirect_uri=redirect&client_id=clientId&state=testState&response_type=code"));
        
        AGAuthzService service = new AGAuthzService();
        
        ((AGAuthzService.AuthzBinder)service.onBind(null)).getService();
        
        OAUTH2AuthzSession session = new OAUTH2AuthzSession();
        session.setAccessToken("accessToken");
        session.setAccountId("accountId");
        session.setAuthorizationCode("authCode");
        session.setExpires_on(2424);
        session.setRefreshToken("allAboardTheRefreshExPress");
        
        service.addAccount(session);
        
        List<String> accounts = service.getAccounts();
        assertEquals(1, accounts.size());
        assertEquals("accountId", accounts.get(0));
        
    }
    
    /**
     * This test will make sure requestAccess fires an intent if the config 
     * isn't associated with a service
     * 
     */
    @Test
    @Ignore
    public void fireIntentIfNotConnected() {
                
        fail("not implemented");
    }

    @Test
    @Ignore
    public void serviceStoresRequestToWeb() {
        
        fail("not implemented");
    }
    
    @Test
    @Ignore
    public void serviceStoresResponseFromWeb() {
        
        fail("not implemented");
    }
    
    @Test
    @Ignore
    public void serviceSendsRequestToWeb() throws InterruptedException {
        
        fail("not implemented");
    }
    
    @Test
    @Ignore
    public void callbackIsCalledIfConnected() {
        
        Activity mockContext = mock(Activity.class);    
        BroadcastReceiver mockReceiver = mock(BroadcastReceiver.class);
        Callback mockCallback = mock(Callback.class);
        
        AGRestAuthzModule module = new AGRestAuthzModule(makeConfig());

        module.requestAccess("testScope", mockContext, mockCallback);

        verify(mockCallback, times(1)).onSuccess(any());
    }
    
    @Test
    @Ignore
    public void exchangeAccessToken() {
        Assert.fail("Not implemented");
    }

    @Test
    @Ignore
    public void refreshToken() {
        Assert.fail("Not implemented");
    }


    
    @Test
    @Ignore
    public void sessionObjectPersistence() {
        Assert.fail("Not implemented");
    }

    @Test
    @Ignore
    public void serviceTest() {
        Assert.fail("Not implemented");
    }

    private AuthzConfig makeConfig() {
        try {
            AuthzConfig conf = new AuthzConfig(new URL("https://accounts.google.com"), "restMod");
            conf.setAccessTokenEndpoint("/o/oauth2/token");
            conf.setAuthzEndpoint("/o/oauth2/auth");
            conf.setClientId("clientId");
            conf.setClientSecret("clientSecret");
            conf.setRedirectURL("redirect");
            conf.setScopes(Lists.newArrayList("testScope"));
            return conf;
        } catch (MalformedURLException ex) {
            //This will NEVER be called, but I'll rethrow it just in case something changes
            throw new RuntimeException(ex);
        }
        
    }

}
