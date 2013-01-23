/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.aerogear.android.impl.pipeline;

import android.util.Log;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
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
public class URIBodyPageParser implements PageResultExtractor<PageConfig> {
private final URI baseUri;
    
    private static final String TAG = URIPageHeaderParser.class.getSimpleName();
    
    public URIBodyPageParser(URI uri) {
        this.baseUri = uri;
    }
    
    public URIBodyPageParser(URL url) {
        try {
            this.baseUri = url.toURI();
        } catch (URISyntaxException ex) {
            Log.e(TAG, url + " could not become URI", ex);
            throw new RuntimeException(url + " could not become URI", ex);
        }
    }
    
    public URIBodyPageParser() {
        this.baseUri = null;
    }

    @Override
    public ReadFilter getNextFilter(HeaderAndBody result, PageConfig config) {
        ReadFilter filter = new ReadFilter();
        JsonParser parser =new JsonParser();
        JsonElement element = parser.parse(new String(result.getBody()));
        URI nextUri = URI.create(getFromJSON(element, config.getNextIdentifier()));
        filter.setLinkUri(baseUri.resolve(nextUri));
        return filter;
    }
    
    @Override
    public ReadFilter getPreviousFilter(HeaderAndBody result, PageConfig config) {
        ReadFilter filter = new ReadFilter();
        JsonParser parser =new JsonParser();
        JsonElement element = parser.parse(new String(result.getBody()));
        URI nextUri = URI.create(getFromJSON(element, config.getPreviousIdentifier()));
        filter.setLinkUri(baseUri.resolve(nextUri));
        return filter;
    }

    private String getFromJSON(JsonElement element, String nextIdentifier) {
        String[] identifiers = nextIdentifier.split("\\.");
        for( String identifier:identifiers) {
            element = element.getAsJsonObject().get(identifier);
        }
        
        if (element.isJsonNull()) {
            return null;
        } else {
            return element.getAsString();
        }
    }
    
}
