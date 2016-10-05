package com.tosslab.jandi.app.push.baidu.receiver;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.text.TextUtils;

import com.baidu.android.pushservice.PushServiceReceiver;
import com.baidu.android.pushservice.message.PublicMsg;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tosslab.jandi.app.push.receiver.JandiPushIntentService;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.Map;

public class BaiduPushReceiver extends PushServiceReceiver {

    public static final String TAG = "BaiduPushReceiver";
    public static final String KEY_PUSH_CONTENT = "content";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (TextUtils.equals(intent.getAction(), "com.baidu.android.pushservice.action.notification.SHOW")) {
            LogUtil.d(TAG, "onReceive()");
            String pushServicePackageName = intent.getStringExtra("pushService_package_name");
            String serviceName = intent.getStringExtra("service_name");
            Parcelable publicMsgParcelable = intent.getParcelableExtra("public_msg");
            PublicMsg publicMsg = null;
            if (publicMsgParcelable != null && publicMsgParcelable instanceof PublicMsg) {
                publicMsg = (PublicMsg) publicMsgParcelable;
            }

            if (TextUtils.isEmpty(pushServicePackageName) || TextUtils.isEmpty(serviceName) || publicMsg == null) {
                return;
            }

            String notifyType = intent.getStringExtra("notify_type");
            if ("private".equals(notifyType)) {
                sendNotificationService(context, publicMsg);
            }
        } else {
            super.onReceive(context, intent);
        }

        try {
            abortBroadcast();
        } catch (Exception e) {
        }
    }

    private void sendNotificationService(Context context, PublicMsg publicMsg) {
        String customContent = publicMsg.mCustomContent;
        if (!TextUtils.isEmpty(customContent)) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                Map<String, String> map =
                        objectMapper.readValue(customContent, new TypeReference<Map<String, String>>() {});
                if (map != null && map.containsKey(KEY_PUSH_CONTENT)) {
                    String content = map.get(KEY_PUSH_CONTENT);
                    JandiPushIntentService.startService(context, content);
                } else {
                    throw new NullPointerException("BaiduPushReceiver content is null");
                }
            } catch (Exception e) {
            }
        }
    }

}
