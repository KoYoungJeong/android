package com.tosslab.jandi.app.call;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.tosslab.jandi.app.utils.logger.LogUtil;


public class JandiCallReceiver extends BroadcastReceiver {
    private static final String TAG = "JandiCallReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        String inComingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        String stateName = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

        LogUtil.d(TAG, String.format("InComingNumber : %s , State : %s", String.valueOf(inComingNumber), String.valueOf(stateName)));

        int state ;

        if (TextUtils.equals(stateName, TelephonyManager.EXTRA_STATE_RINGING)) {
            state = TelephonyManager.CALL_STATE_RINGING;
        } else if (TextUtils.equals(stateName, TelephonyManager.EXTRA_STATE_OFFHOOK)) {
            state = TelephonyManager.CALL_STATE_OFFHOOK;
        } else {
            state = TelephonyManager.CALL_STATE_IDLE;
        }


        JandiCallManager.getInstance().onCall(inComingNumber, state);
    }
}
