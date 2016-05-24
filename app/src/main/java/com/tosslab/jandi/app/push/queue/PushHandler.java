package com.tosslab.jandi.app.push.queue;

import android.content.Context;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.events.messages.MentionToMeEvent;
import com.tosslab.jandi.app.events.push.MessagePushEvent;
import com.tosslab.jandi.app.local.orm.repositories.PushHistoryRepository;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.push.queue.dagger.DaggerPushHandlerComponent;
import com.tosslab.jandi.app.push.receiver.JandiPushReceiverModel;
import com.tosslab.jandi.app.push.to.BaseMessagePushInfo;
import com.tosslab.jandi.app.push.to.PushRoomType;
import com.tosslab.jandi.app.utils.BadgeUtils;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.subjects.PublishSubject;

public class PushHandler {
    private static PushHandler instance;
    @Inject
    JandiPushReceiverModel jandiPushReceiverModel;

    private PublishSubject<BaseMessagePushInfo> pushSubject;

    public PushHandler() {
        this.pushSubject = PublishSubject.<BaseMessagePushInfo>create();
        DaggerPushHandlerComponent
                .builder()
                .build()
                .inject(this);
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
                .doOnNext(pushInfo -> PushHistoryRepository.getRepository().insertPushHistory(
                        pushInfo.getRoomId(), pushInfo.getMessageId()))
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


    void notifyPush(Context context, BaseMessagePushInfo messagePushInfo) {
        long teamId = messagePushInfo.getTeamId();

        // LeftSideMenu 를 DB를 통해 불러오고 없다면 서버에서 받고 디비에 저장한다.
        ResLeftSideMenu leftSideMenu = jandiPushReceiverModel.getLeftSideMenuFromDB(teamId);
        if (leftSideMenu == null) {
            leftSideMenu = jandiPushReceiverModel.getLeftSideMenuFromServer(teamId);
            if (leftSideMenu != null) {
                jandiPushReceiverModel.upsertLeftSideMenu(leftSideMenu);
            }
        }

        if (leftSideMenu == null) {
            showNotification(context, messagePushInfo, false);
            postEvent(messagePushInfo.getRoomId(), messagePushInfo.getRoomType());
            return;
        }

        // 멘션 메시지인 경우 토픽별 푸쉬 on/off 상태는 무시된다.
        boolean isMentionMessageToMe =
                jandiPushReceiverModel.isMentionToMe(messagePushInfo.getMentions(), leftSideMenu);
        if (isMentionMessageToMe) {
            showNotification(context, messagePushInfo, true);

            EventBus.getDefault().post(new MentionToMeEvent(teamId));
        } else {
            if (PushRoomType.CHAT.getName().equals(messagePushInfo.getRoomType())) {
                showNotification(context, messagePushInfo, false);
            } else {
                boolean isTopicPushOn = jandiPushReceiverModel.isTopicPushOn(leftSideMenu, messagePushInfo.getRoomId());
                if (isTopicPushOn) {
                    showNotification(context, messagePushInfo, false);
                }
            }
        }

        postEvent(messagePushInfo.getRoomId(), messagePushInfo.getRoomType());
    }

    private void showNotification(Context context,
                                  BaseMessagePushInfo pushTOInfo, boolean isMentionMessage) {
        jandiPushReceiverModel.showNotification(context, pushTOInfo, isMentionMessage);
    }

    private void postEvent(long roomId, String roomType) {
        EventBus eventBus = EventBus.getDefault();
        if (eventBus.hasSubscriberForEvent(MessagePushEvent.class)) {
            eventBus.post(new MessagePushEvent(roomId, roomType));
        }
    }

    public void addPushQueue(BaseMessagePushInfo baseMessagePushInfo) {
        pushSubject.onNext(baseMessagePushInfo);
    }

    public void removeNotificationIfNeed(long roomId) {
        jandiPushReceiverModel.removeNotificationIfNeed(roomId);
    }
}
