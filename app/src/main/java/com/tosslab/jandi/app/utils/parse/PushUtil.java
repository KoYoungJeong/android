package com.tosslab.jandi.app.utils.parse;

import android.util.Log;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.utils.logger.LogUtil;

/**
 * Created by Steve SeongUg Jung on 15. 6. 9..
 */
public class PushUtil {
    public static final String TAG = "JANDI.ParseUpdateUtils";

    public static final String PARSE_CHANNELS = "channels";

    public static void registPush() {
        PushManager.startWork(JandiApplication.getContext(),
                PushConstants.LOGIN_TYPE_API_KEY,
                JandiConstantsForFlavors.Push.BAIDU_API_KEY);
    }

    // legacy
    public static void unsubscribeParsePush() {
        ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();
        currentInstallation.remove(PARSE_CHANNELS);
        try {
            currentInstallation.save();
        } catch (ParseException e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
        currentInstallation.deleteInBackground();
    }

}
