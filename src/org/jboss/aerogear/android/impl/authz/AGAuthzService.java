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
package org.jboss.aerogear.android.impl.authz;

import org.jboss.aerogear.android.impl.authz.oauth2.OAUTH2AuthzSession;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.net.HttpHeaders;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jboss.aerogear.android.authorization.AuthzConfig;
import org.jboss.aerogear.android.datamanager.IdGenerator;
import org.jboss.aerogear.android.http.HeaderAndBody;
import org.jboss.aerogear.android.http.HttpException;
import org.jboss.aerogear.android.http.HttpProvider;
import org.jboss.aerogear.android.impl.datamanager.SQLStore;
import org.jboss.aerogear.android.impl.http.HttpRestProvider;

import static org.jboss.aerogear.android.impl.util.UrlUtils.appendToBaseURL;

public class AGAuthzService extends Service {

    private final AuthzBinder binder = new AuthzBinder(this);

    private SQLStore<OAUTH2AuthzSession> sessionStore;

    public AGAuthzService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    public String fetchAccessToken(String accountId, AuthzConfig config) throws AuthorizationException {
        OAUTH2AuthzSession storedAccount = sessionStore.read(accountId);
        if (storedAccount == null) {
            return null;
        }

        if (!Strings.isNullOrEmpty(storedAccount.getAccessToken()) && storedAccount.tokenIsNotExpired()) {
            return storedAccount.getAccessToken();
        } else if (!Strings.isNullOrEmpty(storedAccount.getRefreshToken())) {
            refreshAccount(storedAccount, config);
            sessionStore.save(storedAccount);
            return storedAccount.getAccessToken();
        } else if (!Strings.isNullOrEmpty(storedAccount.getAuthorizationCode())) {
            exchangeAuthorizationCodeForAccessToken(storedAccount, config);
            sessionStore.save(storedAccount);
            return storedAccount.getAccessToken();
        } else {
            return null;
        }

    }

    public void addAccount(OAUTH2AuthzSession account) {
        String accountId = account.getAccountId();

        if (hasAccount(accountId)) {
            sessionStore.remove(accountId);
        }

        sessionStore.save(account);
    }

    public boolean hasAccount(String accountId) {
        OAUTH2AuthzSession storedAccount = sessionStore.read(accountId);
        if (storedAccount == null) {
            return false;
        }
        return !Strings.isNullOrEmpty(storedAccount.getAuthorizationCode()) ||
               !Strings.isNullOrEmpty(storedAccount.getAccessToken());
    }

    public OAUTH2AuthzSession getAccount(String accountId) {
        return sessionStore.read(accountId);
    }
    
    public List<String> getAccounts() {
        return new ArrayList<String>(Collections2.<OAUTH2AuthzSession, String>transform(sessionStore.readAll(), new Function<OAUTH2AuthzSession, String>() {

            @Override
            public String apply(OAUTH2AuthzSession input) {
                return input.getAccountId();
            }
        }));
    }

    @Override
    public IBinder onBind(Intent intent) {
        openSessionStore();
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (sessionStore != null) {
            try {
                sessionStore.close();
            } catch (Exception ignore) {
            }
        }
        return super.onUnbind(intent);
    }

    private void openSessionStore() {
        
            sessionStore = new SQLStore<OAUTH2AuthzSession>(OAUTH2AuthzSession.class,
                    getApplicationContext(),
                    new GsonBuilder(),
                    new IdGenerator() {

                        @Override
                        public Serializable generate() {
                            return UUID.randomUUID().toString();
                        }
                    }, "AuthzSessionStore"
            );

            sessionStore.openSync();

        
    }

    private void exchangeAuthorizationCodeForAccessToken(OAUTH2AuthzSession storedAccount, AuthzConfig config) throws AuthorizationException {
        final Map<String, String> data = new HashMap<String, String>();

        data.put("code", storedAccount.getAuthorizationCode());
        data.put("client_id", storedAccount.getCliendId());
        if (config.getRedirectURL() != null) {
            data.put("redirect_uri", config.getRedirectURL());
        }
        data.put("grant_type", "authorization_code");
        if (config.getClientSecret() != null) {
            data.put("client_secret", config.getClientSecret());
        }
        runAccountAction(storedAccount, config, data);

    }

    private void refreshAccount(OAUTH2AuthzSession storedAccount, AuthzConfig config) throws AuthorizationException {
        final Map<String, String> data = new HashMap<String, String>();

        data.put("refresh_token", storedAccount.getRefreshToken());
        data.put("grant_type", "refresh_token");
        data.put("client_id", storedAccount.getCliendId());        
        if (config.getClientSecret() != null) {
            data.put("client_secret", config.getClientSecret());
        }
        runAccountAction(storedAccount, config, data);
    }

    private void runAccountAction(OAUTH2AuthzSession storedAccount, AuthzConfig config, final Map<String, String> data) throws AuthorizationException {
        try {
            final URL accessTokenEndpoint = appendToBaseURL(config.getBaseURL(), config.getAccessTokenEndpoint());

            final HttpProvider provider = getHttpProvider(accessTokenEndpoint);
            final String formTemplate = "%s=%s";
            provider.setDefaultHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");

            final StringBuilder bodyBuilder = new StringBuilder();

            String amp = "";
            for (Map.Entry<String, String> entry : data.entrySet()) {
                bodyBuilder.append(amp);
                try {
                    bodyBuilder.append(String.format(formTemplate, entry.getKey(), URLEncoder.encode(entry.getValue(), "UTF-8")));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                amp = "&";
            }

            HeaderAndBody headerAndBody;

            try {
                headerAndBody = provider.post(bodyBuilder.toString().getBytes("UTF-8"));

            } catch (HttpException exception) {
                if (exception.getStatusCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
                    JsonElement response = new JsonParser().parse(new String(exception.getData()));
                    JsonObject jsonResponseObject = response.getAsJsonObject();
                    String error = "";
                    if (jsonResponseObject.has("error")) {
                        error = jsonResponseObject.get("error").getAsString();
                    }

                    throw new AuthorizationException(error);
                } else {
                    throw exception;
                }
            }
            JsonElement response = new JsonParser().parse(new String(headerAndBody.getBody()));
            JsonObject jsonResponseObject = response.getAsJsonObject();

            String accessToken = jsonResponseObject.get("access_token").getAsString();
            storedAccount.setAccessToken(accessToken);

            //Will need to check this one day
            //String tokenType = jsonResponseObject.get("token_type").getAsString();
            if (jsonResponseObject.has("expires_in")) {
                Long expiresIn = jsonResponseObject.get("expires_in").getAsLong();
                Long expires_on = new Date().getTime() + expiresIn * 1000;
                storedAccount.setExpires_on(expires_on);
            }

            if (jsonResponseObject.has("refresh_token")) {
                String refreshToke = jsonResponseObject.get("refresh_token").getAsString();
                storedAccount.setRefreshToken(refreshToke);
            }

        } catch (UnsupportedEncodingException ex) {
            //Should never happen...
            Log.d(AGAuthzService.class.getName(), null, ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * This method allows an implementation to change how the HttpProvider is
     * fetched. Override is mostly used for testing.
     *
     * @param url the url endpoint
     * @return a httpProvider
     */
    protected HttpProvider getHttpProvider(URL url) {
        return new HttpRestProvider(url);
    }

    public static class AuthzBinder extends Binder {

        private final AGAuthzService service;

        private AuthzBinder(AGAuthzService service) {
            this.service = service;
        }

        public AGAuthzService getService() {
            return service;
        }

    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    public static class AGAuthzServiceConnection implements ServiceConnection {

        private AGAuthzService service;
        private boolean bound = false;

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder iBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            AuthzBinder binder = (AuthzBinder) iBinder;
            this.service = binder.service;
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }

        public AGAuthzService getService() {
            return service;
        }

        public boolean isBound() {
            return bound;
        }

    };

}
