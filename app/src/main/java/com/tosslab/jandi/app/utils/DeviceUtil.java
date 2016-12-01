package com.tosslab.jandi.app.utils;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.tosslab.jandi.app.JandiApplication;

/**
 * Created by tee on 2016. 11. 23..
 */

public class DeviceUtil {

    public static boolean isCallableDevice() {
        TelephonyManager tm = (TelephonyManager)
                JandiApplication.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (tm.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE) {
            return false;
        } else {
            return true;
        }
    }

}
