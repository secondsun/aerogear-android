/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.aerogear.android.impl.pipeline;

import com.google.common.collect.ForwardingList;
import java.util.List;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.pipeline.PagedList;
import org.jboss.aerogear.android.pipeline.Pipe;

/**
 *
 * @author summers
 */
public class WrappingPagedList<T> extends ForwardingList<T> implements PagedList<T> {

    private final Pipe<T> pipe;
    private final List<T> data;
    private final ReadFilter nextFilter;
    private final ReadFilter previousFilter;

    public WrappingPagedList(Pipe<T> pipe, List<T> data, ReadFilter nextFilter, ReadFilter previousFilter) {
        this.pipe = pipe;
        this.data = data;
        this.nextFilter = nextFilter;
        this.previousFilter = previousFilter;
    }

    @Override
    protected List<T> delegate() {
        return data;
    }

    @Override
    public void next(Callback<List<T>> callback) {
        pipe.readWithFilter(nextFilter, callback);
    }

    @Override
    public void previous(Callback<List<T>> callback) {
        pipe.readWithFilter(previousFilter, callback);
    }

}
