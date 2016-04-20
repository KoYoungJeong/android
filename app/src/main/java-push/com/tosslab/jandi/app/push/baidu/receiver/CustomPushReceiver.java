package com.tosslab.jandi.app.push.baidu.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.android.pushservice.PushServiceReceiver;
import com.baidu.android.pushservice.message.PublicMsg;
import com.baidu.android.pushservice.util.h;
import com.tosslab.jandi.app.push.receiver.JandiPushIntentService;

public class CustomPushReceiver extends PushServiceReceiver {

    private static final String TAG = "CustomPushReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (TextUtils.equals(intent.getAction(), "com.baidu.android.pushservice.action.notification.SHOW")) {
            String pushServicePackageName = intent.getStringExtra("pushService_package_name");
            String serviceName = intent.getStringExtra("service_name");
            Parcelable publicMsgParcelable = intent.getParcelableExtra("public_msg");
            PublicMsg publicMsg = null;
            if (publicMsgParcelable != null && publicMsgParcelable instanceof PublicMsg) {
                publicMsg = (PublicMsg) publicMsgParcelable;
            }

            if (TextUtils.isEmpty(pushServicePackageName) || TextUtils.isEmpty(serviceName) || publicMsg == null) {
                com.baidu.a.a.b.a.a.c("PushServiceReceiver", "Extra not valid, servicePkgName=" + pushServicePackageName + " serviceName=" + serviceName + " pMsg==null - " + (publicMsg == null));
                return;
            }

            String notifyType = intent.getStringExtra("notify_type");
            if ("private".equals(notifyType)) {
                String messageId = intent.getStringExtra("message_id");
                String appId = intent.getStringExtra("app_id");
                showNotification(context, pushServicePackageName, serviceName, publicMsg, messageId, appId);
                insertReceivedPush(context, publicMsg);

            } else if ("rich_media".equals(notifyType)) {
            }
        } else {
            super.onReceive(context, intent);
        }

        abortBroadcast();
    }

    private void insertReceivedPush(Context context, PublicMsg publicMsg) {

        String mCustomContent = publicMsg.mCustomContent;

        Intent intent = new Intent(context, JandiPushIntentService.class);
        intent.putExtra("content", mCustomContent);
        context.startService(intent);
    }

    private void showNotification(Context context, String pushServicePackageName, String serviceName, PublicMsg publicMsg, String messageId, String appId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent contentIntent = new Intent();
        contentIntent.setClassName(pushServicePackageName, serviceName);
        contentIntent.setAction("com.baidu.android.pushservice.action.privatenotification.CLICK");
        contentIntent.setData(Uri.parse("content://" + messageId));
        contentIntent.putExtra("public_msg", publicMsg);
        contentIntent.putExtra("app_id", appId);
        contentIntent.putExtra("msg_id", messageId);
        PendingIntent contentPendingIntentn = PendingIntent.getService(context, 0, contentIntent, 0);
        Intent deletIntent = new Intent();
        deletIntent.setClassName(pushServicePackageName, serviceName);
        deletIntent.setAction("com.baidu.android.pushservice.action.privatenotification.DELETE");
        deletIntent.setData(Uri.parse("content://" + messageId));
        deletIntent.putExtra("public_msg", publicMsg);
        deletIntent.putExtra("app_id", appId);
        deletIntent.putExtra("msg_id", messageId);
        PendingIntent deletePendingIntent = PendingIntent.getService(context, 0, deletIntent, 0);
        Notification notification = null;
        boolean what = h.p(context, publicMsg.mPkgName);
        if (publicMsg.mNotificationBuilder == 0) {
            notification = com.baidu.android.pushservice.g.a(context, publicMsg.mNotificationBuilder, publicMsg.mNotificationBasicStyle, publicMsg.mTitle, publicMsg.mDescription, what);
        } else {
            notification = com.baidu.android.pushservice.g.a(context, publicMsg.mNotificationBuilder, publicMsg.mTitle, publicMsg.mDescription, what);
        }

        notification.contentIntent = contentPendingIntentn;
        notification.deleteIntent = deletePendingIntent;

        Log.d(TAG, String.format("Push-Baidu-Title : %s, Description : %s", publicMsg.mTitle, publicMsg.mDescription));
//        notificationManager.notify(messageId, 0, notification);
    }
}
