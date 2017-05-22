package com.tosslab.jandi.app.ui.message.v2;

import android.text.TextUtils;
import android.util.Pair;

import com.tosslab.jandi.app.events.messages.RoomMarkerEvent;
import com.tosslab.jandi.app.events.messages.StarredInfoChangeEvent;
import com.tosslab.jandi.app.lists.messages.MessageItem;
import com.tosslab.jandi.app.local.orm.domain.SendMessage;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.SendMessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.RoomMarkerRepository;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqSendMessageV3;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.network.models.messages.ReqStickerMessage;
import com.tosslab.jandi.app.network.models.messages.ReqTextMessage;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.network.models.start.Announcement;
import com.tosslab.jandi.app.network.models.start.Marker;
import com.tosslab.jandi.app.network.socket.JandiSocketManager;
import com.tosslab.jandi.app.push.queue.PushHandler;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.authority.Level;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.ui.message.to.MessageState;
import com.tosslab.jandi.app.ui.message.to.SendingMessage;
import com.tosslab.jandi.app.ui.message.to.StickerInfo;
import com.tosslab.jandi.app.ui.message.to.queue.MessageContainer;
import com.tosslab.jandi.app.ui.message.to.queue.NewMessageContainer;
import com.tosslab.jandi.app.ui.message.to.queue.NewMessageFromLocalContainer;
import com.tosslab.jandi.app.ui.message.to.queue.OldMessageContainer;
import com.tosslab.jandi.app.ui.message.to.queue.SendingMessageContainer;
import com.tosslab.jandi.app.ui.message.to.queue.UpdateLinkPreviewMessageContainer;
import com.tosslab.jandi.app.ui.message.v2.adapter.MessageListAdapterModel;
import com.tosslab.jandi.app.ui.message.v2.domain.MessagePointer;
import com.tosslab.jandi.app.ui.message.v2.domain.Room;
import com.tosslab.jandi.app.ui.message.v2.model.AnnouncementModel;
import com.tosslab.jandi.app.ui.message.v2.model.MessageListModel;
import com.tosslab.jandi.app.ui.message.v2.model.MessageRepositoryModel;
import com.tosslab.jandi.app.utils.SpeedEstimationUtil;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrMessageDelete;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import rx.Completable;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class MessageListV2Presenter {
    public static final String TAG = MessageListV2Presenter.class.getSimpleName();

    MessageListModel messageListModel;
    AnnouncementModel announcementModel;
    View view;
    private MessageRepositoryModel messageRepositoryModel;
    private PublishSubject<MessageContainer> messageRequestQueue;
    private PublishSubject<Object> markerRequestQueue;
    private Subscription messageLoadSubscription;
    private MessageState currentMessageState;
    private Room room;
    private MessagePointer messagePointer;
    private boolean isInitialized;
    private MessageListAdapterModel adapterModel;
    private boolean isViewResumed;

    @Inject
    public MessageListV2Presenter(MessageListModel messageListModel,
                                  AnnouncementModel announcementModel,
                                  MessageRepositoryModel messageRepositoryModel,
                                  View view, Room room, MessagePointer messagePointer) {
        this.messageListModel = messageListModel;
        this.announcementModel = announcementModel;
        this.messageRepositoryModel = messageRepositoryModel;
        this.view = view;
        this.room = room;
        this.messagePointer = messagePointer;

        initObjects();
    }

    public void setEntityInfo() {
        messageListModel.setEntityInfo(room.getEntityType(), room.getEntityId());
        messageRepositoryModel.setEntityInfo(room.getEntityType(), room.getEntityId());
    }

    void initObjects() {
        currentMessageState = new MessageState();

        initMessageLoadQueue();
    }

    void initMessageLoadQueue() {
        messageRequestQueue = PublishSubject.create();
        messageLoadSubscription = messageRequestQueue
                .onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .concatMap(messageContainer1 -> {
                    Observable<MessageContainer> messageObservable = Observable.just(messageContainer1);
                    switch (messageContainer1.getQueueType()) {
                        case Old:
                            return messageObservable
                                    .compose(this::composeOldMessage);
                        case New:
                            return messageObservable
                                    .compose(this::composeNewMessage)
                                    .concatMap(links -> Observable.just(new NewMessageContainer(currentMessageState)));
                        case NewFromLocal:
                            return messageObservable
                                    .compose(this::composeNewMessageFromLocal)
                                    .concatMap(links -> Observable.just(new NewMessageFromLocalContainer(null)));
                        case Send:
                            return messageObservable
                                    .compose(this::composeSend);
                        case UpdateLinkPreview:
                            return messageObservable
                                    .compose(this::composeLinkPreview);
                    }

                    return messageObservable;
                })
                .subscribe(messageContainer -> {
                }, throwable -> {
                    LogUtil.e("Message Publish Fail!!");
                    throwable.printStackTrace();
                }, () -> {
                });

        markerRequestQueue = PublishSubject.create();
        markerRequestQueue.onBackpressureBuffer()
                .throttleLast(200, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .filter(o1 -> isViewResumed
                        && room != null
                        && room.getRoomId() > 0
                        && adapterModel != null
                        && adapterModel.getCount() > 0)
                .map(o -> {
                    int position = adapterModel.getCount() - adapterModel.getDummyMessageCount() - 1;
                    return adapterModel.getItem(position);
                })
                .subscribe(item -> {
                    long lastLinkId = item.id;
                    if (lastLinkId > messageListModel.getLastReadLinkId(room.getRoomId())) {
                        try {
                            messageListModel.upsertMyMarker(room.getRoomId(), lastLinkId);
                            messageListModel.updateLastLinkId(item.id);
                            view.refreshMessages();
                        } catch (RetrofitException e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (messageListModel.getBadgeCount(room.getRoomId()) != 0) {
                            messageListModel.initBadge(room.getRoomId(), lastLinkId);
                            // 마커가 업데이트 된 roomId 와 마지막으로 받은 푸쉬 메세지의 roomId 가 같으면 노티를 지움.
                            PushHandler.getInstance().removeNotificationIfNeed(room.getRoomId());
                            EventBus.getDefault().post(new RoomMarkerEvent(room.getRoomId()));
                        }
                    }
                    SpeedEstimationUtil.sendAnalyticsMessageSendingEndIfStarted();
                }, Throwable::printStackTrace);
    }

    private Observable<UpdateLinkPreviewMessageContainer> composeLinkPreview(Observable<MessageContainer> observable) {
        return observable.cast(UpdateLinkPreviewMessageContainer.class)
                .observeOn(Schedulers.io())
                .doOnNext(container -> {
                    long messageId = container.getData();

                    ResMessages.TextMessage textMessage =
                            MessageRepository.getRepository().getTextMessage(messageId);

                    int index = adapterModel.indexByMessageId(messageId);
                    if (index < 0) {
                        return;
                    }

                    ResMessages.Link link = adapterModel.getItem(index);
                    if (!(link.message instanceof ResMessages.TextMessage)) {
                        return;
                    }
                    link.message = textMessage;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(it -> view.refreshMessages());
    }

    private Observable<SendingMessageContainer> composeSend(Observable<MessageContainer> observable) {
        return observable.cast(SendingMessageContainer.class)
                .observeOn(Schedulers.io())
                .doOnNext(container -> {
                    SendingMessage data = container.getData();
                    ResMessages.Link link;
                    StickerInfo stickerInfo = data.getStickerInfo();
                    if (stickerInfo != null) {
                        link = messageListModel.sendMessage(data.getLocalId(), room.getTeamId(), room.getRoomId(),
                                new ReqStickerMessage(stickerInfo.getStickerId(), stickerInfo.getStickerGroupId()));
                    } else {
                        List<MentionObject> mentions = data.getMentions();
                        link = messageListModel.sendMessage(data.getLocalId(), room.getTeamId(), room.getRoomId(),
                                new ReqTextMessage(data.getMessage(), mentions));
                    }

                    int position = adapterModel.getDummyMessagePositionByLocalId(data.getLocalId());
                    DummyMessageLink item = ((DummyMessageLink) adapterModel.getItem(position));
                    if (link != null) {
                        if (position >= 0) {
                            item.message = link.message;
                            item.id = link.id;
                            item.time = link.time;
                            item.setStatus(SendMessage.Status.COMPLETE.name());
                        }
                    } else {
                        item.setStatus(SendMessage.Status.FAIL.name());
                    }

                    if (link != null) {
                        if (!JandiSocketManager.getInstance().isConnectingOrConnected()) {
                            // 소켓이 안 붙어 있으면 임의로 갱신 요청
                            addNewMessageQueue();
                        } else {
                            addNewMessageOfLocalQueue(link);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(container -> {
                    SendingMessage data = container.getData();
                    // send message 가 중간에 있는 경우 맨 위로 올려 줌
                    int position = adapterModel.getDummyMessagePositionByLocalId(data.getLocalId());
                    if (position > 0) {
                        DummyMessageLink item = ((DummyMessageLink) adapterModel.getItem(position));
                        if (TextUtils.equals(item.status, SendMessage.Status.COMPLETE.name())) {
                            for (int idx = position - 1; idx >= 0; --idx) {
                                ResMessages.Link beforeItem = adapterModel.getItem(idx);
                                if (beforeItem instanceof DummyMessageLink) {
                                    if (TextUtils.equals(beforeItem.status, SendMessage.Status.COMPLETE.name())) {
                                        adapterModel.remove(position);
                                        adapterModel.add(idx + 1, item);
                                        break;
                                    }
                                } else {
                                    adapterModel.remove(position);
                                    adapterModel.add(idx + 1, item);
                                    break;
                                }
                            }
                        }

                    }
                    view.refreshMessages();
                });
    }

    private Observable<List<ResMessages.Link>> composeNewMessageFromLocal(Observable<MessageContainer> observable) {
        return observable
                .ofType(NewMessageFromLocalContainer.class)
                .doOnNext(it -> MessageRepository.getRepository().insertDirty(it.getData()))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(container -> view.updateRecyclerViewInfo())
                .observeOn(Schedulers.io())
                .filter(container -> view != null || adapterModel != null)
                .map(container -> {
                    int position = adapterModel.getCount() - adapterModel.getDummyMessageCount() - 1;
                    ResMessages.Link item = adapterModel.getItem(position);

                    long minId = -1;
                    if (item != null) {
                        minId = item.id + 1;
                    }

                    List<ResMessages.Link> messages = MessageRepository.getRepository().getMessages(room.getRoomId(), minId, Long.MAX_VALUE);
                    if (TeamInfoLoader.getInstance().isDefaultTopic(getRoomId())) {
                        for (int i = messages.size() - 1; i >= 0; i--) {
                            if (messages.get(i).info instanceof ResMessages.InviteEvent
                                    || messages.get(i).info instanceof ResMessages.LeaveEvent
                                    || messages.get(i).info instanceof ResMessages.JoinEvent) {
                                messages.remove(i);
                            }
                        }
                    }
                    return messages;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(messages -> addMessages(messages, currentMessageState.isFirstLoadNewMessage(), new ArrayList<>()))
                .observeOn(Schedulers.io())
                .doOnNext(messages -> {
                    if (messages.size() > 0) {
                        addMarkerQueue();
                    }
                });
    }

    private Observable<List<ResMessages.Link>> composeNewMessage(Observable<MessageContainer> observable) {
        return observable.cast(NewMessageContainer.class)
                .observeOn(Schedulers.io())
                .map(this::getNewMessages)
                .concatMap(this::checkNullNewMessage)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(it -> view.updateRecyclerViewInfo())
                .observeOn(Schedulers.io())
                .map(this::saveMessages)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(it -> {
                    addMessages(it, currentMessageState.isFirstLoadNewMessage(), new ArrayList<>());
                    addMarkerQueue();
                    currentMessageState.setIsFirstLoadNewMessage(false);
                });
    }

    private Observable<OldMessageContainer> composeOldMessage(Observable<MessageContainer> observable) {
        return observable.cast(OldMessageContainer.class)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(oldMessageContainer -> {
                    if (!oldMessageContainer.getData().isFirstLoadOldMessage()) {
                        view.showOldLoadProgress();
                    }
                })
                .observeOn(Schedulers.io())
                .map(oldMessageContainer1 -> {
                    long roomId = room.getRoomId();
                    ResMessages.Link item = adapterModel.getItem(0);
                    long linkId;
                    if (item != null) {
                        linkId = item.id;
                    } else {
                        linkId = -1;
                    }

                    return Pair.create(oldMessageContainer1, messageRepositoryModel.getMessages(roomId, linkId));
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(pair -> {
                    List<ResMessages.Link> messages = pair.second;
                    if (messages == null
                            || messages.isEmpty()) {
                        view.dismissProgressWheel();
                        view.dismissProgressView();
                        view.dismissOldLoadProgress();
                        adapterModel.setOldNoMoreLoading();

                        if (pair.first.getData().isFirstLoadOldMessage() && adapterModel.getCount() == 0) {
                            view.showEmptyView(true);
                        }

                    }
                })
                .observeOn(Schedulers.io())
                .doOnNext(pair -> {
                    if (pair.second.isEmpty()) {
                        return;
                    }
                    messageListModel.sortByTime(pair.second);
//                    messageListModel.presetTextContent(pair.second);

                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(pair -> {
                    if (pair.second.isEmpty()) {
                        return;
                    }
                    view.dismissProgressWheel();
                    view.dismissProgressView();
                    view.dismissOldLoadProgress();

                    boolean isFirstLoad = adapterModel.getCount() <= 0;

                    if (!isFirstLoad) {
                        view.updateRecyclerViewInfo();
                    }

                    if (adapterModel.getCount() - adapterModel.getDummyMessageCount() == 0) {
                        adapterModel.removeAllDummy();
                    }

                    adapterModel.addAll(0, pair.second);
                    view.refreshMessages();
                    currentMessageState.setIsFirstLoadOldMessage(false);
                    view.setUpOldMessage(isFirstLoad);
                })
                .doOnNext(pair -> {
                    if (pair.second.isEmpty()) {
                        return;
                    }
                    int count = pair.second.size() - 1;
                    ResMessages.Link lastLink = pair.second.get(count);
                    long myId = TeamInfoLoader.getInstance().getMyId();
                    Marker myMarker = RoomMarkerRepository.getInstance().getMarker(room.getRoomId(), myId);

                    if (myMarker == null || myMarker.getReadLinkId() < lastLink.id) {
                        addMarkerQueue();
                    }
                    SpeedEstimationUtil.sendAnalyticsTopicEnteredEndIfStarted();
                    SpeedEstimationUtil.sendAnalyticsPushEnteredEndIfStarted();
                })
                .doOnError(t -> {
                    view.dismissProgressWheel();
                    view.dismissProgressView();
                    view.dismissOldLoadProgress();

                    if (currentMessageState.isFirstMessage()) {
                        view.showEmptyView(true);
                    }
                }).doOnCompleted(() -> {
                    // 메세지 리스트가 모두 갱신 된 후에 more loading 플래그를 변경해야 시간차로 인한 오류가 없다.
                    Completable.complete()
                            .delay(300, TimeUnit.MILLISECONDS)
                            .subscribe(() -> {
                                adapterModel.setOldLoadingComplete();
                            });
                })
                .map(pair -> pair.first);
    }

    public void resetUnreadCnt() {

        if (adapterModel.getCount() <= 0) {
            return;
        }

        List<Marker> markers = RoomMarkerRepository.getInstance().getRoomMarkers(getRoomId());

        if (markers == null || markers.isEmpty()) {
            return;
        }

        // direct room 일 경우 파일 댓글에 제 3자가 끼어들면 marker에도 그 제 3자가 나타나는 문제점이 있다.
        // 그것을 해결하기 위한 코드
        if (TeamInfoLoader.getInstance().isChat(getRoomId())) {
            long compainonReadLink = 0;

            for (Marker marker : markers) {
                if (TeamInfoLoader.getInstance().getChat(getRoomId()).getCompanionId() == marker.getMemberId()) {
                    compainonReadLink = marker.getReadLinkId();
                }
            }

            for (int i = 0; i < adapterModel.getCount(); i++) {
                if (adapterModel.getItem(i).id <= compainonReadLink) {
                    adapterModel.getItem(i).unreadCnt = 0;
                } else {
                    adapterModel.getItem(i).unreadCnt = 1;
                }
            }
            return;
        }

        List<Long> memberLastReadLinks = new ArrayList<>();

        for (Marker marker : markers) {
            memberLastReadLinks.add(marker.getReadLinkId());
        }

        if (memberLastReadLinks.size() <= 0) {
            return;
        }

        Collections.sort(memberLastReadLinks);

        int unreadCnt = 0;

        while ((unreadCnt < memberLastReadLinks.size() - 1) && memberLastReadLinks.get(unreadCnt) <= 0) {
            unreadCnt++;
        }

        long lastLinkCursor = memberLastReadLinks.get(unreadCnt);

        for (int j = 0; j < adapterModel.getCount(); j++) {
            if (adapterModel.getItem(j).id <= lastLinkCursor) {
                adapterModel.getItem(j).unreadCnt = unreadCnt;
            } else {
                while (unreadCnt < memberLastReadLinks.size() - 1
                        && adapterModel.getItem(j).id > lastLinkCursor) {
                    unreadCnt++;
                    lastLinkCursor = memberLastReadLinks.get(unreadCnt);
                }
                adapterModel.getItem(j).unreadCnt = unreadCnt;
            }
        }
    }

    public void onDetermineUserStatus() {

        if (messageListModel.isInactiveUser(room.getEntityId())) {
            view.showInactivedUserLayer();
        } else if (!messageListModel.isEnabledIfUser(room.getEntityId())) {
            view.showDisabledUserLayer();
        } else {
            view.dismissUserStatusLayout();
        }
    }

    public void onInitAnnouncement() {
        Completable.fromAction(this::initAnnouncement)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    // 같은 쓰레드에서 처리를 위함
    private void initAnnouncement() {
        if (announcementModel == null || room == null || view == null) {
            LogUtil.e(TAG, "announcementModel == null || room == null || view == null");
            return;
        }
        view.dismissProgressWheel();

        TopicRoom topic = TeamInfoLoader.getInstance().getTopic(room.getRoomId());
        if (topic != null) {

            Announcement announcement = topic.getAnnouncement();
            if (announcement != null) {
                view.setAnnouncement(announcement);
            }
        }
    }

    public void setAnnouncementActionFrom(boolean fromUser) {
        announcementModel.setActionFromUser(fromUser);
    }

    public void onUpdateAnnouncement(boolean isOpened) {
        Completable.fromAction(() -> {
            announcementModel.updateAnnouncementStatus(room.getTeamId(), room.getRoomId(), isOpened);
        }).subscribeOn(Schedulers.newThread()).subscribe();
    }

    public void onChangeAnnouncementOpenStatusAction(boolean shouldOpened) {
        if (!announcementModel.isActionFromUser()) {
            view.openAnnouncement(shouldOpened);
        }
    }

    public void onCheckAnnouncementExistsAndCreate(long messageId) {
        Observable.defer(() -> {

            Announcement announcement =
                    announcementModel.getAnnouncement(room.getTeamId(), room.getRoomId());
            if (announcement == null) {
                createAnnouncement(messageId);
                return Observable.empty();
            } else {
                return Observable.just(announcement);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((it) -> view.showAnnouncementCreateDialog(messageId));


    }

    // 같은 쓰레드에서 처리를 위함
    private void createAnnouncement(long messageId) {
        Observable.just(messageId)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(id -> {
                    view.showProgressWheel();
                })
                .observeOn(Schedulers.io())
                .doOnNext(id -> {
                    announcementModel.createAnnouncement(room.getTeamId(), room.getRoomId(), messageId);
                })
                .filter(id -> !JandiSocketManager.getInstance().isConnectingOrConnected())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> initAnnouncement(), Throwable::printStackTrace);

    }

    public void onCreateAnnouncement(long messageId) {
        createAnnouncement(messageId);
    }

    public void onDeleteAnnouncementAction() {
        Observable.just(true)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(aBoolean -> {
                    view.showProgressWheel();
                })
                .observeOn(Schedulers.io())
                .doOnNext(it -> {
                    announcementModel.deleteAnnouncement(room.getTeamId(), room.getRoomId());
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {
                    initAnnouncement();
                });
    }

    private long getRoomId() {
        long entityId = room.getEntityId();

        boolean isInTopic = !TeamInfoLoader.getInstance().isUser(entityId);
        if (isInTopic) {
            if (entityId <= 0) {
                return Room.INVALID_ROOM_ID;
            } else {
                return entityId;
            }
        }

        long roomId = room.getRoomId();
        if (roomId > 0) {
            return roomId;
        }

        if (NetworkCheckUtil.isConnected()) {
            roomId = messageListModel.createChat(entityId);
            if (roomId <= 0) {
                return Room.INVALID_ROOM_ID;
            } else {
                room.setRoomId(roomId);
                return roomId;
            }
        }

        return Room.INVALID_ROOM_ID;
    }

    public void onInitMessages(boolean withProgress) {

        if (withProgress) {
            Completable.fromAction(() -> {
                view.showProgressView();
            }).subscribeOn(AndroidSchedulers.mainThread()).subscribe();
        }

        long roomId = room.getRoomId();

        LogUtil.i(TAG, "roomId = " + roomId);

        if (roomId <= 0) {
            roomId = Observable.just(roomId)
                    .observeOn(Schedulers.io())
                    .map(it -> getRoomId())
                    .firstOrDefault(Room.INVALID_ROOM_ID)
                    .toBlocking().first();
            if (roomId <= Room.INVALID_ROOM_ID) {
                Completable.fromAction(() -> {
                    view.showInvalidEntityToast();
                    view.finish();
                }).subscribeOn(AndroidSchedulers.mainThread()).subscribe();
                return;
            }
        }

        Observable.just(roomId)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(roomid -> {
                    room.setRoomId(roomid);
                    String readyMessage = messageListModel.getReadyMessage(roomid);
                    view.initRoomInfo(roomid, readyMessage);
                    Level myLevel = TeamInfoLoader.getInstance().getMyLevel();
                    view.showReadOnly(TeamInfoLoader.getInstance().getRoom(roomid).isReadOnly()
                            && myLevel != Level.Owner
                            && myLevel != Level.Admin
                            && !messageListModel.isTopicOwner(roomid));
                })
                .observeOn(Schedulers.io())
                .subscribe(roomId1 -> {
                    messageListModel.refreshRoomMarker(roomId1);
                    SendMessageRepository.getRepository().deleteCompletedMessageOfRoom(roomId1);

                    long lastReadLinkId = messageListModel.getLastReadLinkId(roomId1);
                    messagePointer.setLastReadLinkId(lastReadLinkId);
                    messageListModel.setRoomId(roomId1);
                    isInitialized = true;

                    OldMessageContainer oldMessageQueue = new OldMessageContainer(currentMessageState);
                    addQueue(oldMessageQueue);

                    NewMessageContainer newMessageQueue = new NewMessageContainer(currentMessageState);
                    addQueue(newMessageQueue);
                }, Throwable::printStackTrace);


    }

    private void addQueue(MessageContainer messageContainer) {
        if (!messageLoadSubscription.isUnsubscribed()) {
            messageRequestQueue.onNext(messageContainer);
        }
    }

    public void addNewMessageQueue() {
        if (!isInitialized) {
            return;
        }
        NewMessageContainer messageQueue = new NewMessageContainer(currentMessageState);
        addQueue(messageQueue);
    }

    public void addOldMessageQueue() {
        OldMessageContainer messageQueue = new OldMessageContainer(currentMessageState);
        addQueue(messageQueue);
    }

    public void addUpdateLinkPreviewMessageQueue(long messageId) {
        UpdateLinkPreviewMessageContainer messageQueue = new UpdateLinkPreviewMessageContainer(messageId);
        addQueue(messageQueue);
    }

    public void addSendingMessageQueue(long localId,
                                       String body, StickerInfo stickerInfo,
                                       List<MentionObject> mentionObjects) {
        SendingMessage sendingMessage =
                stickerInfo != null ?
                        new SendingMessage(localId, body, stickerInfo, mentionObjects)
                        : new SendingMessage(localId, new ReqSendMessageV3((body), mentionObjects));
        SendingMessageContainer sendingMessageQueue = new SendingMessageContainer(sendingMessage);
        addQueue(sendingMessageQueue);
    }

    private List<ResMessages.Link> getNewMessages(NewMessageContainer messageContainer) {
        long lastUpdateLinkId = MessageRepository.getRepository().getLastMessage(room.getRoomId()).id;
        return messageRepositoryModel.getAfterMessages(lastUpdateLinkId, room.getRoomId());

    }

    private Observable<List<ResMessages.Link>> checkNullNewMessage(List<ResMessages.Link> links) {
        if (links == null || links.isEmpty()) {
            boolean hasMessages = hasMessages(adapterModel.getCount());
            Completable.fromAction(() -> {
                view.showEmptyView(!hasMessages);
            }).subscribeOn(AndroidSchedulers.mainThread()).subscribe();

            if (currentMessageState.isFirstLoadNewMessage()) {
                currentMessageState.setIsFirstLoadNewMessage(false);
                long lastReadLinkId = messagePointer.getLastReadLinkId();
                int markerPosition = adapterModel.indexOfLinkId(lastReadLinkId);
                if (markerPosition ==
                        adapterModel.getCount() - adapterModel.getDummyMessageCount() - 1) {
                    messagePointer.setLastReadLinkId(-1);
                }
            }
            return Observable.empty();
        } else {

            if (currentMessageState.isFirstLoadNewMessage()) {
                currentMessageState.setIsFirstLoadNewMessage(false);
                long lastReadLinkId = messagePointer.getLastReadLinkId();
                if (links.get(links.size() - 1).id <= lastReadLinkId) {
                    messagePointer.setLastReadLinkId(-1);
                }
            }

            return Observable.just(links);
        }
    }

    private List<ResMessages.Link> saveMessages(List<ResMessages.Link> links) {
        messageListModel.upsertMessages(room.getRoomId(), links);
        Observable.from(links)
                .map(it -> it.id)
                .collect((Func0<ArrayList<Long>>) ArrayList::new, ArrayList::add)
                .subscribe(its -> {
                    SendMessageRepository.getRepository().deleteCompletedMessages(its);
                });

        messageListModel.sortByTime(links);

        return links;
    }

    void addMessages(List<ResMessages.Link> newMessages, boolean firstLoadNewMessage,
                     List<ResMessages.Link> archivedList) {
        for (ResMessages.Link link : newMessages) {
            if (link.message instanceof ResMessages.StickerMessage
                    || link.message instanceof ResMessages.TextMessage) {
                if (TeamInfoLoader.getInstance().getMyId() == link.fromEntity) {
                    int idxOfMessageId = adapterModel.indexOfDummyLinkId(link.id);
                    if (idxOfMessageId >= 0) {
                        // 더미 삭제
                        adapterModel.remove(idxOfMessageId);
                    }
                }
            }
        }

        adapterModel.addAll(adapterModel.getCount(), archivedList);
        adapterModel.addAll(adapterModel.getCount(), newMessages);
        view.refreshMessages();

        view.setUpNewMessage(newMessages, messageListModel.getMyId(), firstLoadNewMessage);

        currentMessageState.setIsFirstLoadNewMessage(false);

        boolean hasMessages = hasMessages(adapterModel.getCount());
        view.showEmptyView(!hasMessages);
    }

    private boolean hasMessages(int currentItemCount) {
        int eventMessageCount = getEventMessageCount();
        // create 이벤트외에 다른 이벤트가 생성된 경우
        return eventMessageCount > 1 || (currentItemCount > 0
                // event 메세지 외에 다른 메시지들이 있는 경우
                && (currentItemCount - eventMessageCount > 0));
    }

    private int getEventMessageCount() {
        if (adapterModel == null) {
            return 0;
        }
        int[] arrayForCounting = new int[1];

        int count = adapterModel.getCount();
        if (count <= 0) {
            return 0;
        }
        Observable.range(0, count)
                .map(idx -> adapterModel.getItem(idx))
                .filter(link -> TextUtils.equals(link.status, "event"))
                .subscribe(link1 -> {
                    arrayForCounting[0]++;
                });

        return arrayForCounting[0];
    }

    public void onInitializeEmptyLayout(long entityId) {
        boolean isTopic = messageListModel.isTopic(entityId);
        if (isTopic) {
            Observable.from(TeamInfoLoader.getInstance().getUserList())
                    .filter(User::isEnabled)
                    .count()
                    .map(it -> it - 2)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(it -> {
                        TopicRoom topic = TeamInfoLoader.getInstance().getTopic(entityId);
                        int topicMemberCount = topic.getMemberCount();
                        if (it <= 0) {
                            view.insertTeamMemberEmptyLayout();
                        } else if (topicMemberCount <= 1) {
                            view.insertTopicMemberEmptyLayout();
                        } else {
                            view.clearEmptyMessageLayout();
                        }
                    }, t -> {
                        t.printStackTrace();
                    });


        } else {
            for (long memberId : TeamInfoLoader.getInstance().getRoom(entityId).getMembers()) {
                if (memberId != TeamInfoLoader.getInstance().getMyId()) {
                    if (TeamInfoLoader.getInstance().getUser(memberId).isDisabled()) {
                        view.insertNeverMessageLayout();
                        return;
                    }
                }
            }

            view.insertMessageEmptyLayout();
        }
    }

    public void sendStickerMessage(StickerInfo stickerInfo) {
        long entityId = room.getEntityId();
        long roomId = room.getRoomId();
        long localId = messageListModel.insertSendingMessageIfCan(entityId, roomId, stickerInfo);
        if (localId > 0) {
            view.showEmptyView(false);
            ResMessages.Link dummyMessage = messageListModel.getDummyMessage(localId);
            adapterModel.add(dummyMessage);
            view.refreshMessages();
            view.moveLastPage();

            StickerInfo reqStickerInfo = new StickerInfo(stickerInfo);
            SendingMessage sendingMessage = new SendingMessage(localId, "", reqStickerInfo, new ArrayList<>());
            SendingMessageContainer messageQueue = new SendingMessageContainer(sendingMessage);

            addQueue(messageQueue);
        }
    }

    public void sendTextMessage(String message, List<MentionObject> mentions,
                                ReqSendMessageV3 reqSendMessage) {
        long entityId = room.getEntityId();
        long roomId = room.getRoomId();
        long localId = messageListModel.insertSendingMessageIfCan(entityId, roomId, message, mentions);

        if (localId > 0) {
            // insert to ui
            view.showEmptyView(false);
            ResMessages.Link dummyMessage = messageListModel.getDummyMessage(localId);
            adapterModel.add(dummyMessage);
            view.refreshMessages();
            view.moveLastPage();

            SendingMessage sendingMessage = new SendingMessage(localId, reqSendMessage);
            SendingMessageContainer messageQueue = new SendingMessageContainer(sendingMessage);
            addQueue(messageQueue);
        }
    }

    public void restoreStatus() {
        if (room.getRoomId() > 0) {
            messageListModel.removeNotificationSameEntityId(room.getRoomId());
        }

        if (NetworkCheckUtil.isConnected()) {
            view.dismissOfflineLayer();
        } else {
            view.showOfflineLayer();
        }
    }

    public void onSaveTempMessageAction(String tempMessage) {
        Observable.just(new Object())
                .observeOn(Schedulers.io())
                .subscribe(o -> {

                    if (TextUtils.isEmpty(tempMessage)) {
                        messageListModel.deleteReadyMessage(room.getRoomId());
                        return;
                    }

                    messageListModel.saveTempMessage(room.getRoomId(), tempMessage);
                });
    }

    public void onDetermineMessageMenuDialog(ResMessages.OriginalMessage message) {

        if (message instanceof ResMessages.TextMessage) {
            ResMessages.TextMessage textMessage = (ResMessages.TextMessage) message;
            boolean isDirectMessage = messageListModel.isDirectMessage(room.getEntityType());
            boolean isOwner = messageListModel.isTeamOwner();
            boolean isMyMessage = (messageListModel.isMyMessage(textMessage.writerId) || isOwner);

            view.showTextMessageMenuDialog(textMessage, isDirectMessage, isMyMessage);

        } else if (message instanceof ResMessages.CommentMessage) {

            view.showCommentMessageMenuDialog(((ResMessages.CommentMessage) message));

        } else if (message instanceof ResMessages.StickerMessage) {
            ResMessages.StickerMessage stickerMessage = (ResMessages.StickerMessage) message;
            boolean isOwner = messageListModel.isTeamOwner();
            boolean isMyMessage = (messageListModel.isMyMessage(stickerMessage.writerId) || isOwner);

            if (!isMyMessage) {
                return;
            }

            view.showStickerMessageMenuDialog(stickerMessage);
        } else if (message instanceof ResMessages.PollMessage) {
        }
    }

    public void onDeleteDummyMessageAction(long localId) {
        Observable.just(localId)
                .doOnNext(it -> {
                    messageListModel.deleteDummyMessageAtDatabase(it);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> {
                    int position = adapterModel.getDummyMessagePositionByLocalId(it);
                    adapterModel.remove(position);
                    view.refreshMessages();
                });
    }

    public void onDeleteMessageAction(int messageType, long messageId) {
        view.showProgressWheel();
        Completable.fromCallable(() -> {
            if (messageType == MessageItem.TYPE_STRING) {
                messageListModel.deleteMessage(messageId);
            } else if (messageType == MessageItem.TYPE_STICKER) {
                messageListModel.deleteSticker(messageId);
            } else if (messageType == MessageItem.TYPE_STICKER_COMMNET) {
                long feedbackId = MessageRepository.getRepository().getMessage(messageId).feedbackId;
                messageListModel.deleteStickerComment(feedbackId, messageId);
            }
            MessageRepository.getRepository().deleteMessageOfMessageId(messageId);
            SprinklrMessageDelete.sendLog(messageId);
            return true;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnUnsubscribe(() -> view.dismissProgressWheel())
                .subscribe(() -> {
                    int position = adapterModel.indexByMessageId(messageId);
                    if (position >= 0) {
                        adapterModel.remove(position);
                        view.refreshMessages();
                    }
                }, t -> {
                    if (t instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) t;
                        SprinklrMessageDelete.sendFailLog(e.getResponseCode());
                    } else {
                        SprinklrMessageDelete.sendFailLog(-1);
                    }
                });
    }

    public void onTeamLeaveEvent(long teamId, long memberId) {
        if (!messageListModel.isCurrentTeam(teamId)) {
            return;
        }

        if (memberId == room.getEntityId()) {
            Observable.just(TeamInfoLoader.getInstance().getName(memberId))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(name -> {
                        view.showLeavedMemberDialog(name);
                        view.showDisabledUserLayer();
                    });
        }
    }


    public void unSubscribeMessageQueue() {
        if (messageLoadSubscription != null) {
            messageLoadSubscription.unsubscribe();
        }
        if (markerRequestQueue != null) {
            markerRequestQueue.onCompleted();
        }
    }

    public void onMessageStarredAction(long messageId) {
        Observable.defer(() -> {

            try {
                messageListModel.registStarredMessage(room.getTeamId(), messageId);
                return Observable.just(true);
            } catch (RetrofitException e) {
                return Observable.error(e);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> {
                    view.showMessageStarSuccessToast();
                    int position = adapterModel.indexByMessageId(messageId);
                    if (position >= 0) {
                        adapterModel.modifyStarredStateByPosition(position, true);
                    }
                    view.refreshMessages();
                    EventBus.getDefault().post(new StarredInfoChangeEvent());
                }, t -> {
                });
    }

    public void onMessageUnStarredAction(long messageId) {

        Completable.fromCallable(() -> {
            messageListModel.unregistStarredMessage(room.getTeamId(), messageId);
            return true;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    view.showMessageUnStarSuccessToast();
                    int position = adapterModel.indexByMessageId(messageId);
                    if (position >= 0) {
                        adapterModel.modifyStarredStateByPosition(position, false);
                    }
                    view.refreshMessages();
                    EventBus.getDefault().post(new StarredInfoChangeEvent());
                }, t -> {
                });

    }

    public void deleteReadyMessage() {
        messageListModel.deleteReadyMessage(room.getRoomId());
    }

    public void setAdapterModel(MessageListAdapterModel adapterModel) {
        this.adapterModel = adapterModel;
    }

    public void updateCachedTypeOfMessageId(long fileId) {
        int indexOfUnsharedFile = adapterModel.indexByMessageId(fileId);
        adapterModel.updateCachedType(indexOfUnsharedFile);
        view.refreshMessages();
    }

    public void retryToSendDummyMessage(long localId) {

        int dummyPosition = adapterModel.getDummyMessagePositionByLocalId(localId);
        DummyMessageLink dummyMessage = ((DummyMessageLink) adapterModel.getItem(dummyPosition));
        dummyMessage.setStatus(SendMessage.Status.SENDING.name());

        view.refreshMessages();

        if (dummyMessage.message instanceof ResMessages.TextMessage) {

            ResMessages.TextMessage dummyMessageContent = (ResMessages.TextMessage) dummyMessage.message;
            List<MentionObject> mentionObjects = new ArrayList<>();

            if (dummyMessageContent.mentions != null) {
                Observable.from(dummyMessageContent.mentions)
                        .subscribe(mentionObjects::add);
            }

            addSendingMessageQueue(
                    localId, dummyMessageContent.content.body, null, mentionObjects);
        } else if (dummyMessage.message instanceof ResMessages.StickerMessage) {
            ResMessages.StickerMessage stickerMessage = (ResMessages.StickerMessage) dummyMessage.message;

            StickerInfo stickerInfo1 = new StickerInfo();
            stickerInfo1.setStickerGroupId(stickerMessage.content.groupId);
            stickerInfo1.setStickerId(stickerMessage.content.stickerId);

            addSendingMessageQueue(localId, "", stickerInfo1, new ArrayList<>());
        }

    }

    public void changeLinkStatusToArchive(long fileMessageId) {
        int position = adapterModel.indexByMessageId(fileMessageId);
        String archivedStatus = "archived";
        if (position > 0) {
            ResMessages.Link item = adapterModel.getItem(position);
            item.message.status = archivedStatus;
            item.message.createTime = new Date();
            adapterModel.updateCachedType(position);

        }

        List<Integer> commentIndexes = adapterModel.indexByFeedbackId(fileMessageId);

        for (int commentIndex : commentIndexes) {
            ResMessages.Link item = adapterModel.getItem(commentIndex);
            item.feedback.status = archivedStatus;
            item.feedback.createTime = new Date();
            adapterModel.updateCachedType(commentIndex);
        }

        if (position >= 0 || commentIndexes.size() > 0) {
            view.refreshMessages();
        }

    }

    public void changePollData(Poll poll) {
        List<Integer> indexes = adapterModel.getIndexListByPollId(poll.getId());
        if (indexes.size() <= 0) {
            return;
        }

        for (int index : indexes) {
            ResMessages.Link item = adapterModel.getItem(index);
            item.poll = poll;
            adapterModel.updateCachedType(index);
        }

        view.refreshMessages();
    }

    public void updateStarredOfMessage(long messageId, boolean starred) {
        int index = adapterModel.indexByMessageId(messageId);
        adapterModel.modifyStarredStateByPosition(index, starred);
        view.refreshMessages();
    }

    public void onNetworkConnect() {
        if (adapterModel.getCount() <= 0) {
            // roomId 설정 후...
            onInitMessages(true);
        } else {
            addNewMessageQueue();
        }

    }

    private void addMarkerQueue() {
        if (!markerRequestQueue.hasCompleted()) {
            markerRequestQueue.onNext(new Object());
        }
    }

    public void removeOfMessageId(long commentId) {
        if (view == null || adapterModel == null) {
            return;
        }
        int position = adapterModel.indexByMessageId(commentId);
        if (position >= 0) {
            adapterModel.remove(position);
            view.refreshMessages();
        }
    }

    public void addNewMessageOfLocalQueue(ResMessages.Link link) {
        if (isInitialized) {
            if (link != null) {
                MessageRepository.getRepository().insertMessage(link);
                NewMessageFromLocalContainer container = new NewMessageFromLocalContainer(link);
                addQueue(container);
            }
        }
    }

    public void onPauseOfView() {
        isViewResumed = false;
    }

    public void onResumeOfView() {
        isViewResumed = true;
        addMarkerQueue();
    }

    public void onTopicInfoUpdate() {
        Completable.fromAction(() -> {
            TopicRoom topic = TeamInfoLoader.getInstance().getTopic(room.getRoomId());
            String topicName = topic.getName();
            view.setTopicTitle(topicName);
            Level myLevel = TeamInfoLoader.getInstance().getMyLevel();
            view.showReadOnly(topic.isReadOnly()
                    && myLevel != Level.Owner
                    && myLevel != Level.Admin
                    && !messageListModel.isTopicOwner(topic.getId()));
        }).subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                }, Throwable::printStackTrace);
    }

    public interface View {
        void showDisabledUserLayer();

        void showInactivedUserLayer();

        void setAnnouncement(Announcement announcement);

        void showProgressWheel();

        void dismissProgressWheel();

        void showProgressView();

        void dismissProgressView();

        void initRoomInfo(long roomId, String readyMessage);

        void showInvalidEntityToast();

        void setUpOldMessage(boolean isFirstLoad);

        void setUpNewMessage(List<ResMessages.Link> records,
                             long myId, boolean isFirstLoad);

        void showOldLoadProgress();

        void showEmptyView(boolean show);

        void dismissOfflineLayer();

        void showOfflineLayer();

        void moveLastPage();

        void dismissOldLoadProgress();

        void finish();

        void insertTeamMemberEmptyLayout();

        void insertTopicMemberEmptyLayout();

        void clearEmptyMessageLayout();

        void insertMessageEmptyLayout();

        void initMentionControlViewModel(String readyMessage);

        void setTopicTitle(String name);

        void openAnnouncement(boolean shouldOpenAnnouncement);

        void showAnnouncementCreateDialog(long messageId);

        void showStickerMessageMenuDialog(ResMessages.StickerMessage stickerMessage);

        void showTextMessageMenuDialog(ResMessages.TextMessage textMessage,
                                       boolean isDirectMessage, boolean isMyMessage);

        void showCommentMessageMenuDialog(ResMessages.CommentMessage message);

        void showLeavedMemberDialog(String name);

        void showMessageStarSuccessToast();

        void showMessageUnStarSuccessToast();

        void dismissUserStatusLayout();

        void refreshMessages();

        void updateRecyclerViewInfo();

        void showReadOnly(boolean readOnly);

        void insertNeverMessageLayout();
    }

}
