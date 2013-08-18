/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.aerogear.android.impl.unifiedpush;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.unifiedpush.MQTTMessageReceiver;
import org.jboss.aerogear.android.unifiedpush.PushConfig;
import org.jboss.aerogear.android.unifiedpush.PushRegistrar;

class MQTTPushRegistrar implements PushRegistrar, Listener {

    private static final String TAG = MQTTPushRegistrar.class.getSimpleName();
    private final PushConfig config;
    private final MQTT mqtt = new MQTT();
    private CallbackConnection connection;
    private Context context;
    private List<String> topics;
    
    public MQTTPushRegistrar(PushConfig config) {
        this.config = config;
        topics = new ArrayList<String>(config.senderIds);
    }

    @Override
    public void register(Context context, final Callback<Void> callback) {
        this.context = context.getApplicationContext();

        new AsyncTask<Object, Object, Exception>() {
            @Override
            protected Exception doInBackground(Object... params) {
                try {
                    CountDownLatch latch = new CountDownLatch((1));
                    ConnectionCallback conectionCallback = new ConnectionCallback(latch);
                    mqtt.setHost(config.getPushServerURI());
                    connection = mqtt.callbackConnection();
                    connection.listener(MQTTPushRegistrar.this);
                    connection.connect(conectionCallback);
                    latch.await();
                    return conectionCallback.exception;
                } catch (Throwable t) {
                    return new RuntimeException(t);
                }

            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result == null) {
                    callback.onSuccess(null);
                } else {
                    callback.onFailure(result);
                }
            }
        }.execute(null);

    }

    @Override
    public void unregister(Context context, final Callback<Void> callback) {
        new AsyncTask<Object, Object, Exception>() {
            @Override
            protected Exception doInBackground(Object... params) {
                try {
                    CountDownLatch latch = new CountDownLatch((1));
                    ConnectionCallback conectionCallback = new ConnectionCallback(latch);
                    connection.disconnect(conectionCallback);
                    latch.await();
                    return conectionCallback.exception;
                } catch (Throwable t) {
                    return new RuntimeException(t);
                }

            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result == null) {
                    callback.onSuccess(null);
                } else {
                    callback.onFailure(result);
                }
            }
        }.execute(null);
    }

    @Override
    public void onConnected() {
        // Subscribe to a topic
        Topic[] subscribeTo = (Topic[]) new ArrayList<Topic>(Collections2.transform(topics, new Function<String, Topic>() {

            @Override
            public Topic apply(String input) {
               return new Topic(input, QoS.AT_LEAST_ONCE);
            }
        })).toArray(new Topic[]{});
        
        connection.subscribe(subscribeTo, new org.fusesource.mqtt.client.Callback<byte[]>() {
            public void onSuccess(byte[] qoses) {
                // The result of the subcribe request.
            }

            public void onFailure(Throwable value) {
            }
        });
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onPublish(UTF8Buffer topic, Buffer body, Runnable ack) {
        Intent message = new Intent(MQTTMessageReceiver.NAME);
        message.putExtra(MQTTMessageReceiver.TOPIC, topic.ascii().toString());
        message.putExtra(MQTTMessageReceiver.PAYLOAD, body.toString());
        context.sendBroadcast(message);
        ack.run();
    }

    @Override
    public void onFailure(Throwable value) {
        Log.e(TAG, value.getMessage(), value);
        Intent message = new Intent(MQTTMessageReceiver.NAME);
        message.putExtra(MQTTMessageReceiver.ERROR, value);
        context.sendBroadcast(message);
    }

    private static class ConnectionCallback implements org.fusesource.mqtt.client.Callback<Void> {

        private final CountDownLatch latch;
        protected Exception exception;

        public ConnectionCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(Void value) {
            latch.countDown();
        }

        @Override
        public void onFailure(Throwable t) {
            this.exception = new RuntimeException(t);
            latch.countDown();
        }
    }
}
