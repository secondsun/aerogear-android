/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.aerogear.android.pipeline;

import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.http.HeaderAndBody;

/**
 *
 * @author summers
 */
public interface PageResultExtractor<T extends PageConfig> {
    public ReadFilter getNextFilter(HeaderAndBody header, T config);
    public ReadFilter getPreviousFilter(HeaderAndBody header, T config);
}
