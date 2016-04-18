package com.tosslab.jandi.app.dummy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tosslab.jandi.app.utils.logger.LogUtil;

/**
 * Created by tee on 15. 12. 17..
 */

// 프로세스가 죽을 경우 푸시를 받기 위한 더미 리시버
public class JandiDummyReceiver extends BroadcastReceiver {
    private static final String TAG = "JandiDummyReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        LogUtil.d(TAG, "onReceive: " + this.getClass().getName());

        if (Intent.ACTION_POWER_CONNECTED.equals(intent.getAction())
                || Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction())
                || Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {

            intent.setClass(context, JandiProcessStartService.class);
            context.startService(intent);
        }
    }
}