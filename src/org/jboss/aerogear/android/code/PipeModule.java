package org.jboss.aerogear.android.code;

import java.net.URI;
import org.jboss.aerogear.android.http.HttpException;

/**
 * A PipeModule allows special actions to be taken during certain phases of the
 * Pipe life cycle.
 *
 * @author summers
 */
public interface PipeModule {

    /**
     * When construction a HTTP request, the module can prepare several
     * parameters to be applied to the request body, query, and headers.
     *
     * @param relativeURI the URI the request will be made for.
     * @param httpMethod the HTTP method (GET, POST, PUT, DELETE) which will be
     * used
     * @param requestBody the body of the request, if known. May be empty may
     * not be null.
     *
     * @return moduleFields which
     */
    ModuleFields loadModule(URI relativeURI, String httpMethod, byte[] requestBody);

    /**
     * This will try to resolve an error.
     *
     * @param exception the exception to be resolved
     *
     * @return if the error resolution was successful and the attempt should be
     * retried.
     */
    public boolean handleError(HttpException exception);

}
