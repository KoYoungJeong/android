package com.tosslab.jandi.app.push.receiver;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.tosslab.jandi.app.events.push.MessagePushEvent;
import com.tosslab.jandi.app.push.monitor.PushMonitor;
import com.tosslab.jandi.app.push.queue.PushHandler;
import com.tosslab.jandi.app.push.to.BasePushInfo;
import com.tosslab.jandi.app.push.to.MarkerPushInfo;
import com.tosslab.jandi.app.push.to.PushInfo;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import de.greenrobot.event.EventBus;

public class JandiPushIntentService extends IntentService {
    public static final String TAG = "JANDI.JandiPushIntentService";
    private static final String EXTRA_CONTENT = "content";
    private JandiPushReceiverModel jandiPushReceiverModel;
    private PushHandler pushHandler;

    public JandiPushIntentService() {
        super(TAG);
    }

    public static void startService(Context context, String customContent) {
        Intent intent = new Intent(context, JandiPushIntentService.class);
        intent.putExtra(EXTRA_CONTENT, customContent);

        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        jandiPushReceiverModel = JandiPushReceiverModel_.getInstance_(getApplicationContext());
        pushHandler = PushHandler.getInstance();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String accountId = AccountUtil.getAccountId(getApplicationContext());
        if (TextUtils.isEmpty(accountId)) {
            LogUtil.e(TAG, "Account Id is empty.");
            return;
        }

        String content = intent.getStringExtra(EXTRA_CONTENT);

        BasePushInfo basePushInfo = jandiPushReceiverModel.parsingPushTO(content);
        if (basePushInfo == null) {
            LogUtil.e(TAG, "pushInfo == null");
            return;
        }

        if (basePushInfo instanceof MarkerPushInfo) {
            LogUtil.e(TAG, "except MarkderPushInfo");
            return;
        }

        PushInfo pushInfo = (PushInfo) basePushInfo;

        if (!jandiPushReceiverModel.isPushForMyAccountId(pushInfo)) {
            LogUtil.e(TAG, "Push is not for me.");
            return;
        }

        Context context = getApplicationContext();

        jandiPushReceiverModel.convertPlainMarkdownContent(context, pushInfo);

        LogUtil.i(TAG, pushInfo.toString());

        // writerId 가 본인 ID 면 작성자가 본인인 노티이기 때문에 무시한다.
        if (jandiPushReceiverModel.isMyEntityId(pushInfo.getWriterId())) {
            return;
        }

        long roomId = pushInfo.getRoomId();

        boolean isShowingEntity = PushMonitor.getInstance().hasEntityId(roomId);
        boolean userWantsNotification = jandiPushReceiverModel.isPushOn();

        // 해당 채팅방에 진입해 있거나 푸시 알림 설정 Off 였을 때
        if (isShowingEntity || !userWantsNotification) {
            postEvent(roomId, pushInfo.getRoomType());
            return;
        }
        pushHandler.addPushQueue(pushInfo);
    }

    private void postEvent(long roomId, String roomType) {
        EventBus eventBus = EventBus.getDefault();
        if (eventBus.hasSubscriberForEvent(MessagePushEvent.class)) {
            eventBus.post(new MessagePushEvent(roomId, roomType));
        }
    }
}
