package com.tosslab.jandi.app.push.queue;

import android.content.Context;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.events.push.MessagePushEvent;
import com.tosslab.jandi.app.local.orm.repositories.PushHistoryRepository;
import com.tosslab.jandi.app.push.queue.dagger.DaggerPushHandlerComponent;
import com.tosslab.jandi.app.push.receiver.JandiPushReceiverModel;
import com.tosslab.jandi.app.push.to.BaseMessagePushInfo;
import com.tosslab.jandi.app.utils.PushWakeLock;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import rx.Completable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
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
                .subscribeOn(Schedulers.immediate())
                .filter(pushInfo -> PushHistoryRepository.getRepository().isLatestPush(pushInfo.getMessageId()))
                .doOnNext(pushInfo ->
                        PushHistoryRepository.getRepository()
                                .insertPushHistory(pushInfo.getRoomId(), pushInfo.getMessageId()))
                .throttleLast(300, TimeUnit.MILLISECONDS)
                .subscribe(pushInfo -> {
                    notifyPush(JandiApplication.getContext(), pushInfo);
                }, Throwable::printStackTrace);
    }


    void notifyPush(Context context, BaseMessagePushInfo messagePushInfo) {

        Completable.complete()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    PushWakeLock.acquire(context, 100);
                });

        boolean isMentionMessageToMe =
                jandiPushReceiverModel.isMentionToMe(messagePushInfo.getMentioned());
        showNotification(context, messagePushInfo, isMentionMessageToMe);

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

    public void removeNotificationAll() {
        jandiPushReceiverModel.removeNotificationAll();
    }

}
