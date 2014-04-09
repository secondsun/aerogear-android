package org.jboss.aerogear.android.impl.authz.oauth2;

import org.jboss.aerogear.android.impl.authz.oauth2.AGOAuthWebViewDialog;
import org.jboss.aerogear.android.impl.authz.oauth2.OAUTH2AuthzSession;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.authentication.AuthorizationFields;
import org.jboss.aerogear.android.authorization.AuthzConfig;
import org.jboss.aerogear.android.authorization.AuthzModule;
import org.jboss.aerogear.android.impl.authz.AGAuthzService;
import org.jboss.aerogear.android.impl.authz.AuthorizationException;

import static org.jboss.aerogear.android.impl.util.UrlUtils.appendToBaseURL;

public class AGOAuth2AuthzModule implements AuthzModule {

    private final URL baseURL;
    private final URL authzEndpoint;
    private final Uri redirectURL;
    private final List<String> scopes;
    private final String clientId;
    private final AuthzConfig config;

    private static final IntentFilter AUTHZ_FILTER;
    private OAUTH2AuthzSession account;
    private AGAuthzService service;
    static {
        AUTHZ_FILTER = new IntentFilter();
        AUTHZ_FILTER.addAction("org.jboss.aerogear.android.authz.RECEIVE_AUTHZ");
    }

    public AGOAuth2AuthzModule(AuthzConfig config) {
        this.baseURL = config.getBaseURL();
        this.authzEndpoint = appendToBaseURL(baseURL, config.getAuthzEndpoint());
        this.redirectURL = Uri.parse(config.getRedirectURL());
        this.scopes = new ArrayList<String>(config.getScopes());
        this.clientId = config.getClientId();

        this.config = config;
    }

    @Override
    public boolean isAuthorized() {
        
        if (account == null) {
            return false;
        }
        
        return account.tokenIsNotExpired() && !Strings.isNullOrEmpty(account.getAccessToken());
    }
    
    @Override
    public void requestAccess(final String state, final Activity activity, final Callback<String> callback) {

        final AGAuthzService.AGAuthzServiceConnection connection = new AGAuthzService.AGAuthzServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className, IBinder iBinder) {

                final AGAuthzService.AGAuthzServiceConnection instance = this;

                super.onServiceConnected(className, iBinder);
                try {
                    service = getService();
                    final String accountId = config.getAccountId();

                    if (Strings.isNullOrEmpty(accountId)) {
                        throw new IllegalArgumentException("need to have accountId set");
                    }

                    if (!service.hasAccount(accountId)) {

                        String query = "?scope=%s&redirect_uri=%s&client_id=%s&state=%s&response_type=code";
                        query = String.format(query, formatScopes(),
                                URLEncoder.encode(redirectURL.toString(), Charsets.UTF_8.name()),
                                clientId, state);

                        if (config.getAdditionalAuthorizationParams() != null &&
                            config.getAdditionalAuthorizationParams().size() > 0 ) {
                            for (Pair<String, String> param : config.getAdditionalAuthorizationParams()) {
                                query += String.format("&%s=%s", URLEncoder.encode(param.first, Charsets.UTF_8.name()), URLEncoder.encode(param.second, Charsets.UTF_8.name()));
                            }
                        }
                        
                        URL authURL = new URL(authzEndpoint.toString() + query);

                        final AGOAuthWebViewDialog dialog = AGOAuthWebViewDialog.newInstance(authURL, "Drive");
                        dialog.setReceiver(new AGOAuthWebViewDialog.OAuthReceiver() {
                            @Override
                            public void receiveOAuthCode(String code) {
                                OAUTH2AuthzSession session = new OAUTH2AuthzSession();
                                session.setAuthorizationCode(code);
                                session.setAccountId(accountId);
                                session.setCliendId(clientId);
                                service.addAccount(session);

                                new Thread(new Runnable() {

                                    @Override
                                    public void run() {
                                        try {

                                            final String accessToken = service.fetchAccessToken(accountId, config);
                                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    account = service.getAccount(accountId);
                                                    activity.unbindService(instance);
                                                    callback.onSuccess(accessToken);
                                                    dialog.dismiss();
                                                }
                                            });

                                        } catch (final AuthorizationException ex) {
                                            Log.e(AGOAuth2AuthzModule.class.getName(), ex.toString(), ex);
                                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                @Override
                                                public void run() {

                                                    activity.unbindService(instance);
                                                    callback.onFailure(ex);
                                                    dialog.dismiss();
                                                }
                                            });

                                        }
                                    }
                                }).start();

                            }

                            @Override
                            public void receiveOAuthError(final String error) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        activity.unbindService(instance);
                                        callback.onFailure(new Exception(error));
                                    }
                                });
                            }
                        });

                        dialog.show(activity.getFragmentManager(), "TAG");

                    } else {

                        new AsyncTask<Object, Void, Object>() {
                            
                            @Override
                            protected Object doInBackground(Object... params) {
                                try {
                                    return service.fetchAccessToken((String) params[0], (AuthzConfig) params[1]);
                                } catch (AuthorizationException ex) {
                                    return ex;
                                }
                            }

                            @Override
                            protected void onPostExecute(Object result) {
                                if (result instanceof String || result == null) {
                                    account = service.getAccount(accountId);
                                    activity.unbindService(instance);
                                    callback.onSuccess((String) result);
                                } else {
                                    activity.unbindService(instance);
                                    callback.onFailure((Exception) result);
                                }
                            }

                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, accountId, config);

                    }
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(AGOAuth2AuthzModule.class.getName()).log(Level.SEVERE, null, ex);
                    activity.unbindService(instance);
                    callback.onFailure(ex);
                } catch (MalformedURLException ex) {
                    Logger.getLogger(AGOAuth2AuthzModule.class.getName()).log(Level.SEVERE, null, ex);
                    activity.unbindService(instance);
                    callback.onFailure(ex);
                }
            }
        };

        activity.bindService(
                new Intent(activity.getApplicationContext(), AGAuthzService.class
                ), connection, Context.BIND_AUTO_CREATE
        );

    }

    
    
    private String formatScopes() throws UnsupportedEncodingException {

        StringBuilder scopeValue = new StringBuilder();
        String append = "";
        for (String scope : scopes) {
            scopeValue.append(append);
            scopeValue.append(URLEncoder.encode(scope, Charsets.UTF_8.name()));
            append = "+";
        }

        return scopeValue.toString();
    }

    @Override
    public AuthorizationFields getAuthorizationFields(URI requestUri, String method, byte[] requestBody) {
        AuthorizationFields fields = new AuthorizationFields();
        
        fields.addHeader("Authorization", "Bearer " + account.getAccessToken());
        
        return fields;
    }

}
