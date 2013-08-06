/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.aerogear.android.unifiedpush;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MQTTMessageReceiver extends BroadcastReceiver implements PushConstants {
    public static final String NAME = "org.jboss.aerogear.android.unifiedpush.MQTTMessageReceiver";
    public static final String TOPIC = "org.jboss.aerogear.android.unifiedpush.MQTTMessageReceiver.TOPIC";
    public static final String PAYLOAD = "org.jboss.aerogear.android.unifiedpush.MQTTMessageReceiver.PAYLOAD";
    @Override
    public void onReceive(Context context, Intent intent) {
        // notity all attached MessageHandler implementations:
        
        intent.putExtra(MESSAGE, true);
        Registrations.notifyHandlers(context, intent, null);
    }
    
}
