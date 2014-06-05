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
package org.jboss.aerogear.android.impl.authorization;

import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import com.google.common.collect.Lists;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.jboss.aerogear.android.impl.authz.AuthzConfig;
import org.jboss.aerogear.android.impl.authz.AuthzService;
import org.jboss.aerogear.android.impl.authz.oauth2.OAuth2AuthzSession;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RobolectricTestRunner.class)
public class AuthzTest {

    @Test
    public void testParcleOAUTH2AuthzSession() {
        OAuth2AuthzSession session = new OAuth2AuthzSession();
        session.setAccessToken("accessToken");
        session.setAccountId("accountId");
        session.setAuthorizationCode("authzCode");
        session.setExpires_on(92314872492734897l);
        session.setRefreshToken("refreshToken");
        session.setCliendId("testClientId");
        Parcel dest = Parcel.obtain();
        session.writeToParcel(dest, 0);

        assertEquals(session, OAuth2AuthzSession.CREATOR.createFromParcel(dest));

    }

    @Test
    public void testAddAccounts() {
        Intent accessIntent = new Intent(Robolectric.application, AuthzService.class);
        accessIntent.setData(Uri
                .parse("https://accounts.google.com/o/oauth2/auth?scope=testScope&redirect_uri=redirect&client_id=clientId&state=testState&response_type=code"));

        AuthzService service = new AuthzService();

        ((AuthzService.AuthzBinder) service.onBind(null)).getService();

        OAuth2AuthzSession session = new OAuth2AuthzSession();
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
