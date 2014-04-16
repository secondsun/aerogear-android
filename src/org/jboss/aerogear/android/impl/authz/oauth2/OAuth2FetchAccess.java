/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.aerogear.android.impl.authz.oauth2;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.impl.authz.AuthzConfig;
import org.jboss.aerogear.android.impl.authz.AGAuthzService;
import org.jboss.aerogear.android.impl.authz.AuthorizationException;

public class OAuth2FetchAccess {

    private final AGAuthzService service;

    public OAuth2FetchAccess(AGAuthzService service) {
        this.service = service;
    }

    public void fetchAccessCode(final String accountId, final AuthzConfig config, final Callback<String> callback) {

        if (Looper.myLooper() == Looper.getMainLooper()) {//foreground thread
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
                        callback.onSuccess((String) result);
                    } else {
                        callback.onFailure((Exception) result);
                    }
                }

            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, accountId, config);

        } else { //background thread
            new Handler(Looper.myLooper()).post(new Runnable() {

                @Override
                public void run() {
                    try {

                        String code = service.fetchAccessToken(accountId, config);
                        callback.onSuccess((String) code);

                    } catch (AuthorizationException ex) {
                        callback.onFailure(ex);
                    }
                }
            });
        }
    }

}
