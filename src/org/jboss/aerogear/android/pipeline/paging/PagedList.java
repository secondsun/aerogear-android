/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.aerogear.android.pipeline.paging;

import java.util.List;
import org.jboss.aerogear.android.Callback;

/**
 *
 * @author summers
 */
public interface PagedList<T> extends List<T> {
    public void next(Callback<List<T>> callback);

    public void previous(Callback<List<T>> callback);
}
