package com.tosslab.jandi.app.utils.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tosslab.jandi.app.events.network.NetworkConnectEvent;
import com.tosslab.jandi.app.utils.JandiPreference;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 8. 8..
 */
public class NetworkStateBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        int lastNetworkConnect = JandiPreference.getLastNetworkConnect(context);
        boolean connected = NetworkCheckUtil.isConnected();

        switch (lastNetworkConnect) {
            case 0:
                if (!connected) {
                    return;
                }
                break;
            case 1:
                if (connected) {
                    return;
                }
                break;
            case -1:
            default:
                break;
        }

        JandiPreference.setLastNetworkConnect(context, connected ? 1 : 0);

        EventBus.getDefault().post(new NetworkConnectEvent(connected));

    }
}
