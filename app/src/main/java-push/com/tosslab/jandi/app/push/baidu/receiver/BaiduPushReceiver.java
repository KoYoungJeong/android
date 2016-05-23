package com.tosslab.jandi.app.push.baidu.receiver;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.text.TextUtils;

import com.baidu.android.pushservice.PushServiceReceiver;
import com.baidu.android.pushservice.message.PublicMsg;
import com.tosslab.jandi.app.push.receiver.JandiPushIntentService;
import com.tosslab.jandi.app.utils.logger.LogUtil;

public class BaiduPushReceiver extends PushServiceReceiver {

    private static final String TAG = "BaiduPushReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.d(TAG, "onReceive() called");
        if (TextUtils.equals(intent.getAction(), "com.baidu.android.pushservice.action.notification.SHOW")) {
            String pushServicePackageName = intent.getStringExtra("pushService_package_name");
            String serviceName = intent.getStringExtra("service_name");
            Parcelable publicMsgParcelable = intent.getParcelableExtra("public_msg");
            PublicMsg publicMsg = null;
            if (publicMsgParcelable != null && publicMsgParcelable instanceof PublicMsg) {
                publicMsg = (PublicMsg) publicMsgParcelable;
            }

            LogUtil.i(TAG, "pushServicePackageName = " + pushServicePackageName + " serviceName = "+ serviceName + " publicMsg = " + publicMsg);
            if (TextUtils.isEmpty(pushServicePackageName) || TextUtils.isEmpty(serviceName) || publicMsg == null) {
                return;
            }

            String notifyType = intent.getStringExtra("notify_type");
            LogUtil.d(TAG, "notifyType = " + notifyType);
            if ("private".equals(notifyType)) {
                sendNotificationService(context, publicMsg);

            } else if ("rich_media".equals(notifyType)) {
            }
        } else {
            super.onReceive(context, intent);
        }

        abortBroadcast();
    }

    private void sendNotificationService(Context context, PublicMsg publicMsg) {
        String mCustomContent = publicMsg.mCustomContent;
        if (!TextUtils.isEmpty(mCustomContent)) {
            LogUtil.d(TAG, "sendNotificationService");
            JandiPushIntentService.startService(context, mCustomContent);
        }
    }

}
