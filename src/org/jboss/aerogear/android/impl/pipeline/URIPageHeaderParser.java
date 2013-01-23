/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.aerogear.android.impl.pipeline;

import android.util.Log;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.http.HeaderAndBody;
import org.jboss.aerogear.android.pipeline.PageConfig;
import org.jboss.aerogear.android.pipeline.PageResultExtractor;

/**
 *
 * @author summers
 */
public class URIPageHeaderParser implements PageResultExtractor{
    private final URI baseUri;
    
    private static final String TAG = URIPageHeaderParser.class.getSimpleName();
    public URIPageHeaderParser(URI uri) {
        this.baseUri = uri;
    }
    
    public URIPageHeaderParser(URL url) {
        try {
            this.baseUri = url.toURI();
        } catch (URISyntaxException ex) {
            Log.e(TAG, url + " could not become URI", ex);
            throw new RuntimeException(url + " could not become URI", ex);
        }
    }
    
    public URIPageHeaderParser() {
        this.baseUri = null;
    }

    @Override
    public ReadFilter getNextFilter(HeaderAndBody result, PageConfig config) {
        ReadFilter filter = new ReadFilter();
        URI nextUri = URI.create(result.getHeader(config.getNextIdentifier()).toString());
        filter.setLinkUri(baseUri.resolve(nextUri));
        return filter;
    }
    
    @Override
    public ReadFilter getPreviousFilter(HeaderAndBody result, PageConfig config) {
        ReadFilter filter = new ReadFilter();
        URI nextUri = URI.create(result.getHeader(config.getPreviousIdentifier()).toString());
        filter.setLinkUri(baseUri.resolve(nextUri));
        return filter;
    }
    
}
