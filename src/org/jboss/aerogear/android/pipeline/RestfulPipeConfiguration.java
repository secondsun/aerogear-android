/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jboss.aerogear.android.pipeline;

import java.net.URL;
import org.jboss.aerogear.android.Config;
import org.jboss.aerogear.android.code.PipeModule;
import org.jboss.aerogear.android.pipeline.paging.PageConfig;

public class RestfulPipeConfiguration extends PipeConfiguration<RestfulPipeConfiguration> implements Config<RestfulPipeConfiguration>{

    @Override
    <DATA> Pipe<DATA> forClass(Class<DATA> aClass) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    RestfulPipeConfiguration withUrl(URL url) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    RestfulPipeConfiguration module(PipeModule module) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    RestfulPipeConfiguration pageConfig(PageConfig pageConfig) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    RestfulPipeConfiguration requestBuilder(RequestBuilder multipartBuilder) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RestfulPipeConfiguration setName(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
}
