package com.tosslab.jandi.app.utils.parse;

import android.content.Intent;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.parse.ParseInstallation;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.push.gcm.register.RegistrationIntentService;
import com.tosslab.jandi.app.utils.JandiPreference;

/**
 * Created by Steve SeongUg Jung on 15. 6. 9..
 */
public class ParseUpdateUtil {
    public static final String TAG = "JANDI.ParseUpdateUtils";

    public static final String PARSE_ACTIVATION = "activate";
    public static final String PARSE_ACTIVATION_ON = "on";
    public static final String PARSE_ACTIVATION_OFF = "off";

    public static boolean isParseOff = false;

    public static void registPush() {
        Intent service = new Intent(JandiApplication.getContext(), RegistrationIntentService.class);
        JandiApplication.getContext().startService(service);

        PushManager.startWork(JandiApplication.getContext(),
                PushConstants.LOGIN_TYPE_API_KEY,
                JandiConstantsForFlavors.Push.BAIDU_API_KEY);
        deleteChannelOnServer();
        if (!JandiPreference.getRemovedParsePush()) {
            JandiPreference.setRemovedParsePush(true);
        }
    }

    public static void deleteChannelOnServer() {
        ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();
        currentInstallation.deleteInBackground();
    }

}
