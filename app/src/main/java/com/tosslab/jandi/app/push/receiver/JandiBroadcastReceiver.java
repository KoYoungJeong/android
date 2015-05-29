package com.tosslab.jandi.app.push.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.parse.ParsePush;
import com.tosslab.jandi.app.events.push.MessagePushEvent;
import com.tosslab.jandi.app.push.monitor.PushMonitor;
import com.tosslab.jandi.app.push.to.PushTO;
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

        jandiPushReceiverModel = JandiPushReceiverModel_.getInstance_(context);

        Bundle extras = intent.getExtras();
        PushTO pushTO = jandiPushReceiverModel.parsingPushTO(extras);
        if (pushTO == null) {
            return;
        }

        String type = pushTO.getType();
        PushTO.PushInfo pushTOInfo = pushTO.getInfo();

        if (type.equals(JSON_VALUE_TYPE_PUSH)) {
            PushTO.MessagePush messagePush = (PushTO.MessagePush) pushTOInfo;
            // writerId 가 본인 ID 면 작성자가 본인인 노티이기 때문에 무시한다.
            if (jandiPushReceiverModel.isMyEntityId(context, messagePush.getWriterId())) {
                return;
            }

            if (!PushMonitor.getInstance().hasEntityId(messagePush.getChatId()) && jandiPushReceiverModel.isPushOn()) {
                jandiPushReceiverModel.sendNotificationWithProfile(context, messagePush);
            }

            EventBus eventBus = EventBus.getDefault();
            if (eventBus.hasSubscriberForEvent(MessagePushEvent.class)) {
                Log.e(JandiBroadcastReceiver.class.getSimpleName(), "Event has subscribe");
                eventBus.post(new MessagePushEvent(messagePush.getChatId(), messagePush.getChatType()));
            } else {
                jandiPushReceiverModel.updateEntityAndBadge(context);
            }
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

    private void subscribeTopic(String chatId) {
        ParsePush.subscribeInBackground(chatId);

    }

    private void unsubscribeTopic(String chatId) {
        ParsePush.unsubscribeInBackground(chatId);

    }
}
