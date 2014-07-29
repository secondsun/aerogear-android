/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jboss.aerogear.android.pipeline;

import java.net.URL;
import org.jboss.aerogear.android.Config;
import org.jboss.aerogear.android.code.PipeModule;
import org.jboss.aerogear.android.impl.pipeline.RestAdapter;
import org.jboss.aerogear.android.impl.util.UrlUtils;
import org.jboss.aerogear.android.pipeline.paging.PageConfig;

public class RestfulPipeConfiguration extends PipeConfiguration<RestfulPipeConfiguration> implements Config<RestfulPipeConfiguration>{
    private URL url;
    private String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public RestfulPipeConfiguration setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public <DATA> Pipe<DATA> forClass(Class<DATA> aClass) {
        URL dataUrl = UrlUtils.appendToBaseURL(url, name);
        return new RestAdapter<DATA>(aClass, dataUrl);
    }

    @Override
    public RestfulPipeConfiguration withUrl(URL url) {
        this.url = url;
        return this;
    }

    @Override
    public RestfulPipeConfiguration module(PipeModule module) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RestfulPipeConfiguration pageConfig(PageConfig pageConfig) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RestfulPipeConfiguration requestBuilder(RequestBuilder multipartBuilder) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
}
