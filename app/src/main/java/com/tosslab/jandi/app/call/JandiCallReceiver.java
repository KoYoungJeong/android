package com.tosslab.jandi.app.call;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tosslab.jandi.app.utils.logger.LogUtil;


public class JandiCallReceiver extends BroadcastReceiver {
    private static final String TAG = "JandiCallReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.d(TAG, "JandiCallReceiver.onReceive()");
        JandiCallManager.initiate();
    }
}
