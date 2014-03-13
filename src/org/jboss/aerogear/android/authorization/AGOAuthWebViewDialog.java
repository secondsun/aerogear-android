package org.jboss.aerogear.android.authorization;

import android.app.DialogFragment;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.jboss.aerogear.R;
import java.io.UnsupportedEncodingException;
import java.net.URL;

/**
 * Created by summers on 3/13/14.
 */
public class AGOAuthWebViewDialog extends DialogFragment {

    private static final String TAG = AGOAuthWebViewDialog.class.getSimpleName();
    private static final String TITLE = "org.jboss.aerogear.android.authorize.AGOAuthWebViewDialog.TITLE";
    private static final String AUTHORIZE_URL = "org.jboss.aerogear.android.authorize.AGOAuthWebViewDialog.AUTHORIZE_URL";

    private WebView webView;
    private String authorizeUrl;
    private String title;
    private OAuthViewClient client = new OAuthViewClient();

    private static class OAuthViewClient extends WebViewClient {

        private OAuthReceiver receiver;

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            if (url.contains("code=")) {
                final String token = fetchToken(url);
                Log.d("TOKEN", token);
                if (receiver != null) {
                    final OAuthReceiver receiverRef = receiver;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            receiverRef.receiveOAuthCode(token);
                        }
                    });
                }
                return true;
            } else if (url.contains("error=")) {
                final String error = fetchError(url);                
                Log.d("ERROR", error);
                if (receiver != null) {
                    final OAuthReceiver receiverRef = receiver;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            receiverRef.receiveOAuthError(error);
                        }
                    });
                }
                return true;
            }

            return false;
        }

        private String fetchToken(String url) {
            return fetchURLParam(url, "code");
        }
        
        private String fetchError(String url) {
            return fetchURLParam(url, "error");
        }
        
        private String fetchURLParam(String url, String param) {
            Uri uri = Uri.parse(url);
            return uri.getQueryParameter(param);
        }
    }

    public static AGOAuthWebViewDialog newInstance(URL authorizeURL, String title) {
        AGOAuthWebViewDialog instance = new AGOAuthWebViewDialog();
        instance.authorizeUrl = authorizeURL.toString();
        instance.title = title;
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(AUTHORIZE_URL, instance.authorizeUrl);
        instance.setArguments(args);
        return instance;
    }


    @Override
    public void onViewCreated(View arg0, Bundle arg1) {
        super.onViewCreated(arg0, arg1);

        webView.loadUrl(authorizeUrl);
        webView.setWebViewClient(client);
        
        //activates JavaScript (just in case)
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.title = getArguments().getString(TITLE);
        this.authorizeUrl = getArguments().getString(AUTHORIZE_URL);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Retrieve the webview
        View v = inflater.inflate(R.layout.oauth_web_view, container, false);
        webView = (WebView) v.findViewById(R.id.web_oauth);
        webView.setScrollContainer(true);
        getDialog().setTitle(title);
        return v;
    }

    public void setReceiver(OAuthReceiver receiver) {
        client.receiver = receiver;
    }

    public void removeReceive() {
        client.receiver = null;
    }

    public interface OAuthReceiver {
        void receiveOAuthCode(String code);

        public void receiveOAuthError(String error);
    }


}
