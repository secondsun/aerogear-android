package org.jboss.aerogear.android.sync;

import java.net.MalformedURLException;
import java.net.URL;

public class Synchronizer {
    private final URL baseURL;

    public Synchronizer(URL baseURL) {
        this.baseURL = baseURL;
    }

    public Synchronizer(String baseURL) {
        try {
            this.baseURL = new URL(baseURL);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }

    }
 
    
    
    
}
