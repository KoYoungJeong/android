package com.tosslab.jandi.app.services.socket.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.utils.logger.LogUtil;

/**
 * Created by tonyjs on 15. 5. 12..
 */
public class SocketServiceStarter extends BroadcastReceiver {

    public static final String TAG = "SocketServiceRestarter";
    public static final String START_SOCKET_SERVICE =
            "com.tosslab.jandi.app.services.SOCKET_SERVICE_RESTART";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!JandiApplication.isApplicationActive()) {
            return;
        }

        String action = intent.getAction();
        switch (action) {
            case START_SOCKET_SERVICE:
                LogUtil.i(TAG, "restart service from(" + action + ")");
                context.startService(new Intent(context, JandiSocketService.class));
                break;
        }
    }
}
