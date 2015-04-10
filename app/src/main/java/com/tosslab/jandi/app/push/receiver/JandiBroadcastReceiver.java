package com.tosslab.jandi.app.push.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.events.push.MessagePushEvent;
import com.tosslab.jandi.app.push.monitor.PushMonitor;
import com.tosslab.jandi.app.push.to.PushTO;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiPreference;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 14. 12. 3..
 */
public class JandiBroadcastReceiver extends BroadcastReceiver {
    public static final String JSON_KEY_DATA = "com.parse.Data";

    private static final String JSON_VALUE_TYPE_PUSH = "push";
    private static final String JSON_VALUE_TYPE_SUBSCRIBE = "subscribe";
    private static final String JSON_VALUE_TYPE_UNSUBSCRIBE = "unsubscribe";
    private JandiPushReceiverModel jandiPushReceiverModel;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (TextUtils.isEmpty(JandiPreference.getRefreshToken(context))) {
            // 이전에 JANDI 를 설치하고 삭제한 경우, 해당 디바이스 ID 가 남아있어 push 가 전송될 수 있다.
            // 새로 설치하고 아직 sign-in 을 하지 않은 경우 이전 사용자에 대한 push 가 전송됨으로 이를 무시한다.
            return;
        }

        jandiPushReceiverModel = new JandiPushReceiverModel();

        Bundle extras = intent.getExtras();
        PushTO pushTO = jandiPushReceiverModel.parsingPushTO(extras);
        if (pushTO == null) {
            return;
        }

        String type = pushTO.getType();
        PushTO.PushInfo pushTOInfo = pushTO.getInfo();

        // writerId 가 본인 ID 면 작성자가 본인인 노티이기 때문에 무시한다.
        if (type.equals(JSON_VALUE_TYPE_PUSH)) {
            PushTO.MessagePush messagePush = (PushTO.MessagePush) pushTOInfo;
            if (jandiPushReceiverModel.isMyEntityId(context, messagePush.getWriterId())) {
                return;
            }

            if (!PushMonitor.getInstance().hasEntityId(messagePush.getChatId())) {
                sendNotificationWithProfile(context, messagePush);
            }

            int count = jandiPushReceiverModel.recalculateBadgeCount(context);
            updateBadge(context, count);

            EventBus.getDefault().post(new MessagePushEvent(messagePush.getChatId(), messagePush.getChatType()));
        } else if (type.equals(JSON_VALUE_TYPE_SUBSCRIBE)) {
            PushTO.SubscribePush subscribePush = (PushTO.SubscribePush) pushTOInfo;
            subscribeTopic(subscribePush.getChatId());
        } else if (type.equals(JSON_VALUE_TYPE_UNSUBSCRIBE)) {
            PushTO.UnSubscribePush unSubscribePush = (PushTO.UnSubscribePush) pushTOInfo;
            unsubscribeTopic(unSubscribePush.getChatId());
        } else {
            // DO NOTHING
        }
        return;
    }

    private void updateBadge(Context context, int count) {
        JandiPreference.setBadgeCount(context, count);
        BadgeUtils.setBadge(context, count);
    }

    private void sendNotificationWithProfile(final Context context, final PushTO.MessagePush messagePush) {
        // 현재 디바이스 설정이 push off 라면 무시
        if (JandiConstants.PARSE_ACTIVATION_OFF.equals(
                ParseInstallation
                        .getCurrentInstallation()
                        .getString(JandiConstants.PARSE_ACTIVATION))) {
            return;
        }

        String writerProfile = messagePush.getWriterThumb();
        Log.d("Profile Url", JandiConstantsForFlavors.SERVICE_ROOT_URL + writerProfile);
        if (writerProfile != null) {
            Ion.with(context)
                    .load(JandiConstantsForFlavors.SERVICE_ROOT_URL + writerProfile)
                    .asBitmap()
                    .setCallback(new FutureCallback<Bitmap>() {
                        @Override
                        public void onCompleted(Exception e, Bitmap result) {

                            if (e != null || result == null) {
                                sendNotification(context, messagePush, null);
                            } else if (result != null) {
                                sendNotification(context, messagePush, result);
                            }
                        }
                    });
        }
    }

    private void sendNotification(final Context context, final PushTO.MessagePush messagePush, Bitmap writerProfile) {
        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = jandiPushReceiverModel.generateNotification(context, messagePush, writerProfile, this);
        if (notification != null) {
            nm.notify(JandiConstants.NOTIFICATION_ID, notification);
        }
    }

    private void subscribeTopic(String chatId) {
        ParsePush.subscribeInBackground(chatId);

    }

    private void unsubscribeTopic(String chatId) {
        ParsePush.unsubscribeInBackground(chatId);

    }
}
