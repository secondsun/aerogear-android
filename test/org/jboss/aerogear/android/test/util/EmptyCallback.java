package org.jboss.aerogear.android.test.util;

import org.jboss.aerogear.android.Callback;

/**
 * This class is just a blank callback.
 */
public class EmptyCallback<T> implements Callback<T> {

    @Override
    public void onSuccess(T data) {
    }

    @Override
    public void onFailure(Exception e) {
    }
    
}
