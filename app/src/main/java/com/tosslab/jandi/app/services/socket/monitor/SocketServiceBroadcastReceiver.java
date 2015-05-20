package com.tosslab.jandi.app.services.socket.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.tosslab.jandi.app.services.socket.JandiSocketService;

/**
 * Created by tonyjs on 15. 5. 12..
 */
public class SocketServiceBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = "SocketServiceRestarter";
    public static final String START_SOCKET_SERVICE = "com.tosslab.jandi.app.services.SOCKET_SERVICE_RESTART";
    public static final String ACTION_FROM_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    public static final String ACTION_FROM_CONNECTIVITY_CHANGED
            = "android.net.conn.CONNECTIVITY_CHANGE";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case ACTION_FROM_CONNECTIVITY_CHANGED:
//                boolean connected = isConnected(context);
//                Log.e(TAG, "Connectivity has changed - connected ? " + connected);
//                if (connected) {
//                    context.startService(new Intent(context, JandiSocketService.class));
//                } else {
//                    JandiSocketService.startSocketServiceIfNeed(context);
//                }
                break;
            case ACTION_FROM_BOOT_COMPLETED:
            case START_SOCKET_SERVICE:
                Log.i(TAG, "restart service from(" + action + ")");

                context.startService(new Intent(context, JandiSocketService.class));
                break;
        }
    }

    public boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }
}
