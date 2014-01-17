package org.jboss.aerogear.android.sync;

import android.content.Context;
import android.util.Log;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.View;
import com.couchbase.lite.replicator.Replication;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.jboss.aerogear.android.Callback;

public class AGSyncSynchronizer<T> implements Synchronizer<T>, Replication.ChangeListener, LiveQuery.ChangeListener {

    private static final String TAG = AGSyncSynchronizer.class.getSimpleName();

    private final List<SynchronizeEventListener<T>> listeners = new ArrayList<SynchronizeEventListener<T>>();
    private Manager manager;
    private String DATABASE_NAME = "device-sync";
    private Database database;
    private String SYNC_URL = "http://10.0.2.2:5984";

    @Override
    public void addListener(SynchronizeEventListener<T> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(SynchronizeEventListener<T> listener) {
        listeners.remove(listener);
    }

    @Override
    public void beginSync(Context appContext, Callback<Void> syncReadyCallback) {
        File filesDir = appContext.getFilesDir();
        try {
            manager = new Manager(filesDir, Manager.DEFAULT_OPTIONS);
            database = manager.getDatabase(DATABASE_NAME);
            View deviceView = database.getView("device-view");
            deviceView.setMap(new Mapper() {

                @Override
                public void map(Map<String, Object> document, Emitter emitter) {
                    emitter.emit(document.toString(), null);
                }
            }, "1.0");
            LiveQuery liveQuery = deviceView.createQuery().toLiveQuery();
            liveQuery.addChangeListener(this);
            liveQuery.start();
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }

        URL syncUrl;
        try {
            syncUrl = new URL(SYNC_URL + "/" + DATABASE_NAME);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        Replication pullReplication = database.createPullReplication(syncUrl);
        pullReplication.setContinuous(true);

        Replication pushReplication = database.createPushReplication(syncUrl);
        pushReplication.setContinuous(true);

        pullReplication.start();
        pushReplication.start();

        pullReplication.addChangeListener(this);
        pushReplication.addChangeListener(this);
    }

    @Override
    public void syncNoMore() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void loadRemoteChanges() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void sync() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void changed(Replication.ChangeEvent ce) {
    }

    @Override
    public void changed(LiveQuery.ChangeEvent ce) {
        ArrayList<QueryRow> rows = new ArrayList<QueryRow>(ce.getRows().getCount());
        QueryEnumerator enumerator = ce.getRows();
        QueryRow row;
        while (enumerator.hasNext()) {
            row = enumerator.next();
            Log.d(TAG, row.asJSONDictionary().toString());
            rows.add(row);
        }
        
        for (SynchronizeEventListener<T> listener : listeners) {
            listener.dataUpdated((Collection<T>) rows);
        }

    }

}
