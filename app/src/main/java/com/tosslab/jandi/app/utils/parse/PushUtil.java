package com.tosslab.jandi.app.utils.parse;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstantsForFlavors;

/**
 * Created by Steve SeongUg Jung on 15. 6. 9..
 */
public class PushUtil {

    public static void registPush() {
        PushManager.startWork(JandiApplication.getContext(),
                PushConstants.LOGIN_TYPE_API_KEY,
                JandiConstantsForFlavors.Push.BAIDU_API_KEY);
    }

}
