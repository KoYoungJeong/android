package com.tosslab.jandi.app.push.receiver;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.tosslab.jandi.app.events.push.MessagePushEvent;
import com.tosslab.jandi.app.push.monitor.PushMonitor;
import com.tosslab.jandi.app.push.to.PushTO;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import de.greenrobot.event.EventBus;

/**
 * Created by tonyjs on 15. 7. 14..
 */
public class JandiPushIntentService extends IntentService {
    public static final String TAG = "JANDI.JandiPushIntentService";

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

        LogUtil.i(TAG, pushTO.toString());

        PushTO.PushInfo pushTOInfo = pushTO.getInfo();
        Context context = getApplicationContext();
        // writerId 가 본인 ID 면 작성자가 본인인 노티이기 때문에 무시한다.
        if (jandiPushReceiverModel.isMyEntityId(context, pushTOInfo.getWriterId())) {
            return;
        }

        boolean hasEntityId = PushMonitor.getInstance().hasEntityId(pushTOInfo.getRoomId());

        // 설정 > 알림 on 일 때
        // 다른 플랫폼이 Deactive 일 때
        // 토픽 알림 on 일 때
        boolean isPushOn = jandiPushReceiverModel.isPushOn()
                && !pushTO.getAlarm().isPlatformActive()
                && pushTO.getAlarm().isTopicSubscription();

        if (!hasEntityId && isPushOn) {
            jandiPushReceiverModel.sendNotificationWithProfile(context, pushTOInfo);
        }

        EventBus eventBus = EventBus.getDefault();
        if (eventBus.hasSubscriberForEvent(MessagePushEvent.class)) {
            eventBus.post(
                    new MessagePushEvent(pushTOInfo.getRoomId(), pushTOInfo.getRoomType()));
        } else {
            jandiPushReceiverModel.updateEntityAndBadge(context, pushTOInfo.getBadge());
        }
    }
}
