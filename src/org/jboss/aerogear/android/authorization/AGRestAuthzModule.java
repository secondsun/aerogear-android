package org.jboss.aerogear.android.authorization;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PathMeasure;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PatternMatcher;
import android.util.Log;
import android.widget.Toast;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.net.HttpHeaders;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.utils.URLEncodedUtils;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.http.HeaderAndBody;
import org.jboss.aerogear.android.http.HttpException;
import org.jboss.aerogear.android.http.HttpProvider;
import org.jboss.aerogear.android.impl.http.HttpRestProvider;
import org.jboss.aerogear.android.impl.util.UrlUtils;
import org.json.JSONException;
import org.json.JSONObject;

import static org.jboss.aerogear.android.impl.util.UrlUtils.appendToBaseURL;

public class AGRestAuthzModule implements AuthzModule {

    private final URL baseURL;
    private final URL authzEndpoint;
    private final URL accessTokenEndpoint;
    private final Uri redirectURL;
    private final List<String> scopes;
    private final String clientId;
    private final String clientSecret;

    public AGRestAuthzModule(AuthzConfig config) {
        this.baseURL = config.getBaseURL();
        this.authzEndpoint = appendToBaseURL(baseURL, config.getAuthzEndpoint());
        this.accessTokenEndpoint = appendToBaseURL(baseURL, config.getAccessTokenEndpoint());
        this.redirectURL = Uri.parse(config.getRedirectURL().toString());
        this.scopes = new ArrayList<String>(config.getScopes());
        this.clientId = config.getClientId();
        this.clientSecret = config.getClientSecret();
    }

    public void requestAccess(final Callback<String> callback, final Activity activity, BroadcastReceiver receiver) {
        try {
            String query = "?scope=%s&redirect_uri=%s&client_id=%s&response_type=code";
            query = String.format(query, formatScopes(),
                    URLEncoder.encode(redirectURL.toString(), Charsets.UTF_8.name()),
                    clientId);

            URL authURL = new URL(authzEndpoint.toString() + query);
            Log.e("URL", authURL.toString());

            final AGOAuthWebViewDialog dialog = AGOAuthWebViewDialog.newInstance(authURL, "Drive");
            dialog.setReceiver(new AGOAuthWebViewDialog.OAuthReceiver() {
                @Override
                public void receiveOAuthCode(String code) {
                    exchangeAuthorizationCodeForAccessToken(code, callback);
                    dialog.dismiss();
                }
            });
            dialog.show(activity.getFragmentManager(), "TAG");

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(AGRestAuthzModule.class.getName()).log(Level.SEVERE, null, ex);
            callback.onFailure(ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(AGRestAuthzModule.class.getName()).log(Level.SEVERE, null, ex);
            callback.onFailure(ex);
        }
    }

    private void exchangeAuthorizationCodeForAccessToken(String code, final Callback<String> callback) {
        final HttpProvider provider = new HttpRestProvider(accessTokenEndpoint);
        final String formTemplate = "%s=%s";
        provider.setDefaultHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");

        final Map<String, String> data = new HashMap<String, String>();
        final StringBuilder bodyBuilder = new StringBuilder();

        data.put("code", code);
        data.put("client_id", clientId);
        data.put("redirect_uri", redirectURL.toString());
        data.put("grant_type", "authorization_code");
        if (clientSecret != null) {
            data.put("client_secret", clientSecret);
        }

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

        new AsyncTask<Void, Void, HeaderAndBody>() {
            @Override
            protected HeaderAndBody doInBackground(Void... params) {
                try {
                    return provider.post(bodyBuilder.toString().getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            protected void onPostExecute(HeaderAndBody headerAndBody) {
                callback.onSuccess(new String(headerAndBody.getBody()));
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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

}
