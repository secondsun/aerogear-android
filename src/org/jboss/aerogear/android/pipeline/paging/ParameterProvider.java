/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.aerogear.android.pipeline.paging;

import java.net.URI;
import org.jboss.aerogear.android.ReadFilter;

/**
 *
 * @author summers
 */
public interface ParameterProvider {

    public URI getParameters(ReadFilter filter);
    
}
