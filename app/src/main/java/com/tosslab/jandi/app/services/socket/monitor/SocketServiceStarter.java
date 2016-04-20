package com.tosslab.jandi.app.services.socket.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

/**
 * Created by tonyjs on 15. 5. 12..
 */
public class SocketServiceStarter extends BroadcastReceiver {

    public static final String TAG = "SocketServiceStarter";
    public static final String START_SOCKET_SERVICE =
            "com.tosslab.jandi.app.services.SOCKET_SERVICE_RESTART";
    public static final String ACTION_CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (JandiApplication.isApplicationDeactive()) {
            return;
        }

        String action = intent.getAction();
        Intent serviceIntent = new Intent(context, JandiSocketService.class);
        switch (action) {
            case START_SOCKET_SERVICE:
                LogUtil.i(TAG, "restart service from(" + action + ")");
                context.startService(serviceIntent);
                break;
            case ACTION_CONNECTIVITY_CHANGE:
                if (NetworkCheckUtil.isConnected()) {
                    if (!JandiSocketService.isServiceRunning(context)) {
                        context.startService(serviceIntent);
                    }
                }
                break;

        }
    }
}
