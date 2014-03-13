
package org.jboss.aerogear.android.authorization;

import android.app.Activity;
import java.net.URI;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.authentication.AuthorizationFields;
import org.jboss.aerogear.android.http.HttpProvider;
import org.jboss.aerogear.android.pipeline.Pipe;

public interface AuthzModule {
    
    public boolean isAuthorized();
    
    public void requestAccess(String state, Activity activity, Callback<String> callback);
    
    
    /**
     * This method is called be {@link Pipe} implementations when they need
     * security applied to their {@link HttpProvider}. The headers/data/query
     * parameters returned should be applied to the Url and HttpProvider
     * directly before a call.
     * 
     * @param requestUri the Request-Line URI.
     * @param method the HTTP method being used
     * @param requestBody the body of the request.  This method promises to not 
     * modify the body.
     * 
     * @return the current AuthorizationFields for security
     * 
     */
    public AuthorizationFields getAuthorizationFields(URI requestUri, String method, byte[] requestBody);

    
}
