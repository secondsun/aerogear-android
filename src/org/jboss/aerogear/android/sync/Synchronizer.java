/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.android.sync;

import android.content.Context;

import org.jboss.aerogear.android.Callback;

/**
 * Created by summers on 12/9/13.
 */
public interface Synchronizer<T> {

    /**
     * A listener listens for data on this synchronizer.
     *
     * @param listener
     */
    public void addListener(SynchronizeEventListener<T> listener);

    /**
     * A listener listens for data on this synchronizer.
     *
     * @param listener
     */
    public void removeListener(SynchronizeEventListener<T> listener);


    /**
     * Does what ever is necessary to start syncing
     */
    public void beginSync(Context appContext, Callback<Void> syncReadyCallback);

    /**
     * Sync no more
     */
    public void syncNoMore();


    /**
     * Pulls down the remote data and synchronizes
     */
    public void loadRemoteChanges();

    /**
     * Notify the synchronizer that local changes have been made and should be sent to the remote server for synchronization.
     */
    public void sync();

}