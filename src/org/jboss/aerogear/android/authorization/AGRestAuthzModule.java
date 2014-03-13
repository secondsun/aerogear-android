package org.jboss.aerogear.android.authorization;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.http.HeaderAndBody;
import org.jboss.aerogear.android.impl.http.HttpRestProvider;
import org.jboss.aerogear.android.impl.util.UrlUtils;
import static org.jboss.aerogear.android.impl.util.UrlUtils.appendToBaseURL;

public class AGRestAuthzModule implements AuthzModule {

    private final URL baseURL;
    private final URL authzEndpoint;
    private final URL accessTokenEndpoint;
    private final URL redirectURL;
    private final List<String> scopes;
    private final String clientId;
    private final String clientSecret;

    public AGRestAuthzModule(AuthzConfig config) {
        this.baseURL = config.getBaseURL();
        this.authzEndpoint = appendToBaseURL(baseURL, config.getAuthzEndpoint());
        this.accessTokenEndpoint = appendToBaseURL(baseURL, config.getAccessTokenEndpoint());
        this.redirectURL = appendToBaseURL(baseURL, config.getRedirectURL());
        this.scopes = new ArrayList<>(config.getScopes());
        this.clientId = config.getClientId();
        this.clientSecret = config.getClientSecret();
    }

    public void requestAccess(Callback<Object> callback) {
        try {
            String query ="?scope=%s&redirect_uri=%s&client_id=%s&response_type=code";
            query = String.format(query, formatScopes(), 
                                         URLEncoder.encode(redirectURL.toString(), Charsets.UTF_8.name()),
                                         clientId);
            
            URL authURL = UrlUtils.appendQueryToBaseURL(authzEndpoint, query);
            
            HttpRestProvider provider = new HttpRestProvider(authURL);
            HeaderAndBody result = provider.post((byte[])null);
            
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(AGRestAuthzModule.class.getName()).log(Level.SEVERE, null, ex);
            callback.onFailure(ex);
        }
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
