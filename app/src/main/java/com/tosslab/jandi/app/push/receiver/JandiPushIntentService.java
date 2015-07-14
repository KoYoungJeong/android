package com.tosslab.jandi.app.push.receiver;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.parse.ParsePush;
import com.tosslab.jandi.app.events.push.MessagePushEvent;
import com.tosslab.jandi.app.push.monitor.PushMonitor;
import com.tosslab.jandi.app.push.to.PushTO;

import de.greenrobot.event.EventBus;

/**
 * Created by tonyjs on 15. 7. 14..
 */
public class JandiPushIntentService extends IntentService {
    public static final String TAG = JandiPushIntentService.class.getSimpleName();

    private static final String JSON_VALUE_TYPE_PUSH = "push";
    private static final String JSON_VALUE_TYPE_SUBSCRIBE = "subscribe";
    private static final String JSON_VALUE_TYPE_UNSUBSCRIBE = "unsubscribe";

    private JandiPushReceiverModel jandiPushReceiverModel;

    public JandiPushIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        jandiPushReceiverModel = JandiPushReceiverModel_.getInstance_(getApplicationContext());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        PushTO pushTO = jandiPushReceiverModel.parsingPushTO(extras);
        if (pushTO == null) {
            return;
        }

        String type = pushTO.getType();
        PushTO.PushInfo pushTOInfo = pushTO.getInfo();
        Context context = getApplicationContext();

        if (type.equals(JSON_VALUE_TYPE_PUSH)) {
            PushTO.MessagePush messagePush = (PushTO.MessagePush) pushTOInfo;
            // writerId 가 본인 ID 면 작성자가 본인인 노티이기 때문에 무시한다.
            if (jandiPushReceiverModel.isMyEntityId(context, messagePush.getWriterId())) {
                return;
            }

            boolean hasEntityId = PushMonitor.getInstance().hasEntityId(messagePush.getChatId());
            if (!hasEntityId && jandiPushReceiverModel.isPushOn()) {
                jandiPushReceiverModel.sendNotificationWithProfile(context, messagePush);
            }

            EventBus eventBus = EventBus.getDefault();
            if (eventBus.hasSubscriberForEvent(MessagePushEvent.class)) {
                eventBus.post(
                        new MessagePushEvent(messagePush.getChatId(), messagePush.getChatType()));
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
    }

    private void subscribeTopic(String chatId) {
        ParsePush.subscribeInBackground(chatId);
    }

    private void unsubscribeTopic(String chatId) {
        ParsePush.unsubscribeInBackground(chatId);
    }
}
