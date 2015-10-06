package com.tosslab.jandi.app.push.receiver;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.events.push.MessagePushEvent;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.push.monitor.PushMonitor;
import com.tosslab.jandi.app.push.to.PushTO;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.subjects.PublishSubject;

/**
 * Created by tonyjs on 15. 7. 14..
 */
public class JandiPushIntentService extends IntentService {
    public static final String TAG = "JANDI.JandiPushIntentService";
    private PublishSubject<PushTO> pushSubject;
    private JandiPushReceiverModel jandiPushReceiverModel;

    public JandiPushIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        jandiPushReceiverModel = JandiPushReceiverModel_.getInstance_(getApplicationContext());
        pushSubject = PublishSubject.create();
        pushSubject
                .onBackpressureBuffer()
                .buffer(300, TimeUnit.MILLISECONDS)
                .filter(pushTOs -> pushTOs != null && !pushTOs.isEmpty())
                .subscribe(pushTOs -> {
                    Collections.sort(pushTOs, (lhs, rhs) ->
                            ((int) (DateTransformator.getTimeFromISO(lhs.getInfo().getCreatedAt())
                                    - DateTransformator.getTimeFromISO(rhs.getInfo().getCreatedAt()))));

                    PushTO pushTO = pushTOs.get(pushTOs.size() - 1);
                    notifyPush(JandiApplication.getContext(), pushTO);

                }, Throwable::printStackTrace);
    }

    @Override
    public void onDestroy() {
        pushSubject.onCompleted();
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String accountId = AccountUtil.getAccountId(getApplicationContext());
        if (TextUtils.isEmpty(accountId)) {
            LogUtil.e(TAG, "Account Id is empty.");
            return;
        }

        Bundle extras = intent.getExtras();

        if (!jandiPushReceiverModel.isPushForMyAccountId(extras, accountId)) {
            LogUtil.e(TAG, "Push is not for me.");
            return;
        }

        PushTO pushTO = jandiPushReceiverModel.parsingPushTO(extras);
        if (pushTO == null) {
            LogUtil.e(TAG, "pushTO == null");
            return;
        }

        LogUtil.i(TAG, pushTO.toString());

        PushTO.PushInfo pushTOInfo = pushTO.getInfo();
        Context context = getApplicationContext();
        // writerId 가 본인 ID 면 작성자가 본인인 노티이기 때문에 무시한다.
        if (jandiPushReceiverModel.isMyEntityId(pushTOInfo.getWriterId())) {
            return;
        }

        int teamId = pushTOInfo.getTeamId();
        int roomId = pushTOInfo.getRoomId();

        boolean isShowingEntity = PushMonitor.getInstance().hasEntityId(roomId);
        boolean userWantsNotification = jandiPushReceiverModel.isPushOn();

        int badgeCount = jandiPushReceiverModel.getBadgeCount(teamId);
        jandiPushReceiverModel.updateBadgeCount(context, teamId, badgeCount + 1);

        // 해당 채팅방에 진입해 있거나 푸시 알림 설정 Off 였을 때
        if (isShowingEntity || !userWantsNotification) {
            postEvent(roomId, pushTOInfo.getRoomType());
            return;
        }

        pushSubject.onNext(pushTO);
    }

    private void notifyPush(Context context, PushTO pushTO) {
        PushTO.PushInfo pushTOInfo;
        pushTOInfo = pushTO.getInfo();
        ResLeftSideMenu leftSideMenu = jandiPushReceiverModel.getLeftSideMenu(pushTOInfo.getTeamId());

        if (leftSideMenu == null) {
            showNotification(context, pushTOInfo, false);
            postEvent(pushTOInfo.getRoomId(), pushTOInfo.getRoomType());
            return;
        }

        // 멘션 메시지인 경우 토픽별 푸쉬 on/off 상태는 무시된다.
        boolean isMentionMessageToMe =
                jandiPushReceiverModel.isMentionToMe(pushTOInfo.getMentions(), leftSideMenu);
        if (isMentionMessageToMe) {
            showNotification(context, pushTOInfo, true);
        } else {
            boolean isTopicPushOn = jandiPushReceiverModel.isTopicPushOn(leftSideMenu, pushTOInfo.getRoomId());
            if (isTopicPushOn) {
                showNotification(context, pushTOInfo, false);
            }
        }

        postEvent(pushTOInfo.getRoomId(), pushTOInfo.getRoomType());
    }

    private void showNotification(Context context,
                                  PushTO.PushInfo pushTOInfo, boolean isMentionMessage) {
        jandiPushReceiverModel.showNotification(context, pushTOInfo, isMentionMessage);
    }

    private void postEvent(int roomId, String roomType) {
        EventBus eventBus = EventBus.getDefault();
        if (eventBus.hasSubscriberForEvent(MessagePushEvent.class)) {
            eventBus.post(new MessagePushEvent(roomId, roomType));
        }
    }

}
