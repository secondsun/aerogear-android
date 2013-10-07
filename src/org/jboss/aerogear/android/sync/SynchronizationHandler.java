package org.jboss.aerogear.android.sync;

import android.os.Bundle;

public interface SynchronizationHandler<T> {

    void doFullSync();
    
    void doDelete(T toDelete);
    
    void doUpdate(T toUpdate);
    
    void doAdd(T toAdd);
    
    void doResolve(T original, T... conflicts);
    
    void doError(T data, Bundle errorData);
}
