package com.tosslab.jandi.app.push.queue;

import android.content.Context;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.events.messages.MentionToMeEvent;
import com.tosslab.jandi.app.events.push.MessagePushEvent;
import com.tosslab.jandi.app.local.orm.repositories.PushHistoryRepository;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.push.receiver.JandiPushReceiverModel_;
import com.tosslab.jandi.app.push.to.PushInfo;
import com.tosslab.jandi.app.push.to.PushRoomType;
import com.tosslab.jandi.app.utils.BadgeUtils;

import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.subjects.PublishSubject;

public class PushHandler {
    private static PushHandler instance;
    private final JandiPushReceiverModel_ jandiPushReceiverModel;

    private PublishSubject<PushInfo> pushSubject;


    public PushHandler() {
        this.pushSubject = PublishSubject.<PushInfo>create();
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
                .filter(pushInfo -> PushHistoryRepository.getRepository().isLatestPush(pushInfo.getMessageId()))
                .doOnNext(pushInfo -> PushHistoryRepository.getRepository().insertPushHistory(pushInfo.getMessageId()))
                .buffer(300, TimeUnit.MILLISECONDS)
                .filter(pushTOs -> pushTOs != null && !pushTOs.isEmpty())
                .subscribe(pushTOs -> {

                    Observable.from(pushTOs)
                            .reduce((prev, current) -> {
                                if (prev.getMessageId() > current.getMessageId()) {
                                    return prev;
                                } else {
                                    return current;
                                }
                            })
                            .subscribe(pushInfo -> {
                                BadgeUtils.setBadge(JandiApplication.getContext(), pushInfo.getBadgeCount());
                                notifyPush(JandiApplication.getContext(), pushInfo);
                            });


                }, Throwable::printStackTrace);
    }


    void notifyPush(Context context, PushInfo pushInfo) {
        long teamId = pushInfo.getTeamId();

        // LeftSideMenu 를 DB를 통해 불러오고 없다면 서버에서 받고 디비에 저장한다.
        ResLeftSideMenu leftSideMenu = jandiPushReceiverModel.getLeftSideMenuFromDB(teamId);
        if (leftSideMenu == null) {
            leftSideMenu = jandiPushReceiverModel.getLeftSideMenuFromServer(teamId);
            if (leftSideMenu != null) {
                jandiPushReceiverModel.upsertLeftSideMenu(leftSideMenu);
            }
        }

        if (leftSideMenu == null) {
            showNotification(context, pushInfo, false);
            postEvent(pushInfo.getRoomId(), pushInfo.getRoomType());
            return;
        }

        // 멘션 메시지인 경우 토픽별 푸쉬 on/off 상태는 무시된다.
        boolean isMentionMessageToMe =
                jandiPushReceiverModel.isMentionToMe(pushInfo.getMentions(), leftSideMenu);
        if (isMentionMessageToMe) {
            showNotification(context, pushInfo, true);

            EventBus.getDefault().post(new MentionToMeEvent(teamId));
        } else {
            if (PushRoomType.CHAT.getName().equals(pushInfo.getRoomType())) {
                showNotification(context, pushInfo, false);
            } else {
                boolean isTopicPushOn = jandiPushReceiverModel.isTopicPushOn(leftSideMenu, pushInfo.getRoomId());
                if (isTopicPushOn) {
                    showNotification(context, pushInfo, false);
                }
            }
        }

        postEvent(pushInfo.getRoomId(), pushInfo.getRoomType());
    }

    private void showNotification(Context context,
                                  PushInfo pushTOInfo, boolean isMentionMessage) {
        jandiPushReceiverModel.showNotification(context, pushTOInfo, isMentionMessage);
    }

    private void postEvent(long roomId, String roomType) {
        EventBus eventBus = EventBus.getDefault();
        if (eventBus.hasSubscriberForEvent(MessagePushEvent.class)) {
            eventBus.post(new MessagePushEvent(roomId, roomType));
        }
    }

    public void addPushQueue(PushInfo pushInfo) {
        pushSubject.onNext(pushInfo);
    }
}
