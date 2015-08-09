package com.tosslab.jandi.app.utils.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tosslab.jandi.app.events.network.NetworkConnectEvent;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 8. 8..
 */
public class NetworkStateBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        boolean connected = NetworkCheckUtil.isConnected();
        EventBus.getDefault().post(new NetworkConnectEvent(connected));
    }
}
