package com.tosslab.jandi.app.push.queue;

import android.content.Context;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.events.push.MessagePushEvent;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.push.receiver.JandiPushReceiverModel_;
import com.tosslab.jandi.app.push.to.PushTO;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.subjects.PublishSubject;

public class PushHandler {
    private static PushHandler instance;
    private final JandiPushReceiverModel_ jandiPushReceiverModel;

    private PublishSubject<PushTO> pushSubject;


    public PushHandler() {
        this.pushSubject = PublishSubject.create();
        this.jandiPushReceiverModel = JandiPushReceiverModel_.getInstance_(JandiApplication.getContext());
        initPushQueue();
    }

    synchronized public static PushHandler getInstance() {
        if (instance == null) {
            instance = new PushHandler();
        }
        return instance;
    }

    private void initPushQueue() {
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


    private void notifyPush(Context context, PushTO pushTO) {
        PushTO.PushInfo pushTOInfo;
        pushTOInfo = pushTO.getInfo();
        int teamId = pushTOInfo.getTeamId();

        // LeftSideMenu 를 DB를 통해 불러오고 없다면 서버에서 받고 디비에 저장한다.
        ResLeftSideMenu leftSideMenu = jandiPushReceiverModel.getLeftSideMenuFromDB(teamId);
        if (leftSideMenu == null) {
            leftSideMenu = jandiPushReceiverModel.getLeftSideMenuFromServer(teamId);
            if (leftSideMenu != null) {
                jandiPushReceiverModel.upsertLeftSideMenu(leftSideMenu);
            }
        }

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
            if (PushTO.RoomType.CHAT.getName().equals(pushTOInfo.getRoomType())) {
                LogUtil.e("tony", "roomType == CHAT");
                showNotification(context, pushTOInfo, false);
            } else {
                boolean isTopicPushOn = jandiPushReceiverModel.isTopicPushOn(leftSideMenu, pushTOInfo.getRoomId());
                if (isTopicPushOn) {
                    showNotification(context, pushTOInfo, false);
                }
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

    public void addPushQueue(PushTO pushTO) {
        pushSubject.onNext(pushTO);
    }
}
