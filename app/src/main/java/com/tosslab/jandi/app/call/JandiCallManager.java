package com.tosslab.jandi.app.call;


import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.utils.logger.LogUtil;

public class JandiCallManager {
    private static final String TAG = "JandiCallManager";
    private static JandiCallManager instance;

    private JandiCallManager() {
        TelephonyManager tm = (TelephonyManager) JandiApplication.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(new JandiPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);
    }

    synchronized public static JandiCallManager initiate() {
        if (instance == null) {
            instance = new JandiCallManager();
        }
        return instance;
    }

    private static class JandiPhoneStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);

            LogUtil.d(TAG, "onCallStateChanged() called with: state = [" + state + "], incomingNumber = [" + incomingNumber + "]");

            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    break;
            }

        }
    }
}
