package com.tosslab.jandi.app.push.receiver;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.tosslab.jandi.app.events.push.MessagePushEvent;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.push.monitor.PushMonitor;
import com.tosslab.jandi.app.push.to.PushTO;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiPreference;
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
            LogUtil.e(TAG, "pushTO == null");
            return;
        }

        LogUtil.i(TAG, pushTO.toString());

        PushTO.PushInfo pushTOInfo = pushTO.getInfo();
        Context context = getApplicationContext();
        // writerId 가 본인 ID 면 작성자가 본인인 노티이기 때문에 무시한다.
        if (jandiPushReceiverModel.isMyEntityId(context, pushTOInfo.getWriterId())) {
            return;
        }

        int teamId = pushTOInfo.getTeamId();
        int roomId = pushTOInfo.getRoomId();
        int badgeCount = JandiPreference.getBadgeCount(context);

        boolean isShowingEntity = PushMonitor.getInstance().hasEntityId(roomId);
        boolean userWantsNotification = jandiPushReceiverModel.isPushOn();

        // Badge count, Topic Push 등의 정보를 위해 LeftSideMenu 를 조회한다.
        ResLeftSideMenu leftSideMenu = jandiPushReceiverModel.getTeamInfo(teamId);

        // LeftSideMenu 조회에 실패한 경우
        if (leftSideMenu == null) {
            if (!isShowingEntity && userWantsNotification) {
                jandiPushReceiverModel.showNotification(context, pushTOInfo, badgeCount);
            }

            postEvent(roomId, pushTOInfo.getRoomType());
            return;
        }

        int newBadgeCount = BadgeUtils.getTotalUnreadCount(leftSideMenu);
        if (jandiPushReceiverModel.isPushFromSelectedTeam(context, teamId)) {
            badgeCount = newBadgeCount;
        } else {
            badgeCount = badgeCount + newBadgeCount;
        }

        boolean isMentionMessage = pushTOInfo.hasMentions();
        // 멘션 메시지인 경우 토픽별 푸쉬 on/off 상태는 무시된다.
        if (isMentionMessage) {
            if (!isShowingEntity && userWantsNotification) {
                jandiPushReceiverModel.showNotification(context, pushTOInfo, badgeCount);
            }
        } else {
            boolean isTopicPushOn = jandiPushReceiverModel.isTopicPushOn(leftSideMenu, roomId);

            if (!isShowingEntity && userWantsNotification && isTopicPushOn) {
                jandiPushReceiverModel.showNotification(context, pushTOInfo, badgeCount);
            }
        }

        if (!postEvent(roomId, pushTOInfo.getRoomType())) {
            jandiPushReceiverModel.updateBadgeCount(context, badgeCount);
        }
    }

    private boolean postEvent(int roomId, String roomType) {
        EventBus eventBus = EventBus.getDefault();
        if (eventBus.hasSubscriberForEvent(MessagePushEvent.class)) {
            eventBus.post(new MessagePushEvent(roomId, roomType));
            return true;
        }
        return false;
    }

}
