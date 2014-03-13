
package org.jboss.aerogear.android.authorization;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AuthzConfig {
    private final URL baseURL;
    private final String name;
    private String authzEndpoint = "";
    private String redirectURL = "";
    private String accessTokenEndpoint = "";
    private List<String> scopes = new ArrayList<String>();
    private String clientId = "";
    private String clientSecret = "";
    private Long timeout = 60000l;
    private AuthzType type = AGAuthzType.OAUTH;
    
    public AuthzConfig(URL baseURL, String name) {
        this.baseURL = baseURL;
        this.name = name;
    }
    

    public URL getBaseURL() {
        return baseURL;
    }

    public String getAuthzEndpoint() {
        return authzEndpoint;
    }

    public void setAuthzEndpoint(String authzEndpoint) {
        this.authzEndpoint = authzEndpoint;
    }

    public String getRedirectURL() {
        return redirectURL;
    }

    public void setRedirectURL(String redirectURL) {
        this.redirectURL = redirectURL;
    }

    public String getAccessTokenEndpoint() {
        return accessTokenEndpoint;
    }

    public void setAccessTokenEndpoint(String accessTokenEndpoint) {
        this.accessTokenEndpoint = accessTokenEndpoint;
    }

    public List<String> getScopes() {
        return new ArrayList<>(scopes);
    }

    public void setScopes(List<String> scopes) {
        this.scopes = new ArrayList<String>(scopes);
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public String getName() {
        return name;
    }

    public AuthzType getType() {
        return type;
    }

    public void setType(AuthzType type) {
        this.type = type;
    }
    
    
    
}
