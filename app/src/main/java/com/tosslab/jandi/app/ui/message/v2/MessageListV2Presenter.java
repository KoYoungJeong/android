package com.tosslab.jandi.app.ui.message.v2;

import android.text.TextUtils;
import android.util.Pair;
import android.util.Log;

import com.tosslab.jandi.app.events.messages.StarredInfoChangeEvent;
import com.tosslab.jandi.app.lists.messages.MessageItem;
import com.tosslab.jandi.app.local.orm.domain.SendMessage;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.RoomMarkerRepository;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqSendMessageV3;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.network.models.start.Marker;
import com.tosslab.jandi.app.network.models.start.Topic;
import com.tosslab.jandi.app.network.socket.JandiSocketManager;
import com.tosslab.jandi.app.team.TeamInfoLoader;
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
import com.tosslab.jandi.app.utils.DateComparatorUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

@EBean
public class MessageListV2Presenter {
    public static final String TAG = MessageListV2Presenter.class.getSimpleName();

    @Bean
    MessageListModel messageListModel;
    @Bean
    AnnouncementModel announcementModel;

    View view;

    private PublishSubject<MessageContainer> messageRequestQueue;
    private PublishSubject<Object> markerRequestQueue;
    private Subscription messageLoadSubscription;
    private MessageState currentMessageState;
    private Room room;
    private MessagePointer messagePointer;
    private boolean isInitialized;
    private MessageListAdapterModel adapterModel;
    private MessageRepositoryModel messageRepositoryModel;

    public void setView(View view) {
        this.view = view;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public void setEntityInfo() {
        messageListModel.setEntityInfo(room.getEntityType(), room.getEntityId());
        messageRepositoryModel.setEntityInfo(room.getEntityType(), room.getEntityId());
    }

    public void setMessagePointer(MessagePointer messagePointer) {
        this.messagePointer = messagePointer;
    }

    @AfterInject
    void initObjects() {
        currentMessageState = new MessageState();
        messageRepositoryModel = new MessageRepositoryModel();

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
                                    .compose(this::composeNewMessage);
                        case NewFromLocal:
                            return messageObservable
                                    .compose(this::composeNewMessageFromLocal);
                        case Send:
                            return messageObservable
                                    .compose(this::composeSend);
                        case UpdateLinkPreview:

                            return messageObservable
                                    .compose(this::composeLinkPreview);
                    }

                    return messageObservable;
                })
                .subscribe(messageContainer -> {}, throwable -> {
                    LogUtil.e("Message Publish Fail!!");
                    throwable.printStackTrace();
                }, () -> {});

        markerRequestQueue = PublishSubject.create();
        markerRequestQueue.onBackpressureBuffer()
                .throttleLast(500, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .subscribe(o -> {
                    int position = adapterModel.getCount() - 1;
                    if (position >= 0) {

                        ResMessages.Link item = adapterModel.getItem(position);
                        try {
                            messageListModel.updateLastLinkId(item.id);
                        } catch (RetrofitException e) {
                            e.printStackTrace();
                        }
                    }
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
                    long messageId;
                    if (data.getStickerInfo() != null) {
                        messageId = messageListModel.sendStickerMessage(room.getTeamId(), room.getEntityId(),
                                data.getStickerInfo(), data.getLocalId());
                    } else {
                        List<MentionObject> mentions = data.getMentions();
                        messageId = messageListModel.sendMessage(data.getLocalId(), data.getMessage(), mentions);
                    }

                    int position = adapterModel.getDummyMessagePositionByLocalId(data.getLocalId());
                    if (position >= 0) {
                        adapterModel.getItem(position).messageId = messageId;
                    }

                    if (messageId > 0) {
                        if (!JandiSocketManager.getInstance().isConnectingOrConnected()) {
                            // 소켓이 안 붙어 있으면 임의로 갱신 요청
                            addNewMessageQueue(true);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(container -> view.refreshMessages());
    }

    private Observable<MessageContainer> composeNewMessageFromLocal(Observable<MessageContainer> observable) {
        return observable
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(container -> view.updateRecyclerViewInfo())
                .observeOn(Schedulers.io())
                .filter(container -> view != null || adapterModel != null)
                .map(container -> {
                    ResMessages.Link item = adapterModel.getItem(adapterModel.getCount() - 1 - adapterModel.getDummyMessageCount());

                    long minId = -1;
                    if (item != null) {
                        minId = item.id + 1;
                    }

                    List<ResMessages.Link> messages = MessageRepository.getRepository().getMessages(room.getRoomId(), minId, Integer.MAX_VALUE);
                    messageListModel.presetTextContent(messages);
                    return Pair.create(container, messages);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(pair -> addMessages(pair.second, currentMessageState.isFirstLoadNewMessage(), new ArrayList<>()))
                .observeOn(Schedulers.io())
                .doOnNext(pair -> {
                    if (pair.second.size() > 0) {
                        long lastLinkId = pair.second.get(pair.second.size() - 1).id;
                        messageListModel.upsertMyMarker(room.getRoomId(), lastLinkId);

                        addMarkerQueue();
                    }
                })
                .map(pair -> pair.first);
    }

    private Observable<NewMessageContainer> composeNewMessage(Observable<MessageContainer> observable) {
        return observable.cast(NewMessageContainer.class)
                .subscribeOn(Schedulers.io())
                .doOnNext(this::loadNewMessage);
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

                        if (pair.first.getData().isFirstLoadOldMessage()) {
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
                    messageListModel.presetTextContent(pair.second);

                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(pair -> {
                    if (pair.second.isEmpty()) {
                        return;
                    }
                    view.dismissProgressWheel();
                    view.dismissProgressView();
                    view.dismissOldLoadProgress();

                    if (!pair.first.getData().isFirstMessage()) {
                        view.updateRecyclerViewInfo();
                    }

                    if (adapterModel.getCount() - adapterModel.getDummyMessageCount() == 0) {
                        adapterModel.removeAllDummy();
                    }

                    boolean isFirstMessage = pair.second.size() < MessageRepositoryModel.MAX_COUNT;
                    adapterModel.addAll(0, pair.second);
                    view.refreshMessages();
                    if (!isFirstMessage) {
                        adapterModel.setOldLoadingComplete();
                    } else {
                        adapterModel.setOldNoMoreLoading();
                    }
                    view.setUpOldMessage(pair.first.getData().isFirstMessage());

                    currentMessageState.setIsFirstLoadOldMessage(false);
                    currentMessageState.setIsFirstMessage(isFirstMessage);
                })
                .doOnNext(pair -> {
                    if (pair.second.isEmpty()) {
                        return;
                    }
                    int count = pair.second.size() - 1;
                    ResMessages.Link lastLink = pair.second.get(count);
                    long myId = TeamInfoLoader.getInstance().getMyId();
                    Marker myMarker = RoomMarkerRepository.getInstance().getMarker(room.getRoomId(), myId);

                    if (myMarker.getReadLinkId() < lastLink.id) {
                        addMarkerQueue();
                        messageListModel.upsertMyMarker(room.getRoomId(), lastLink.id);
                    }
                })
                .doOnError(t -> {
                    view.dismissProgressWheel();
                    view.dismissProgressView();
                    view.dismissOldLoadProgress();

                    if (currentMessageState.isFirstMessage()) {
                        view.showEmptyView(true);
                    }
                })
                .map(pair -> pair.first);
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

    @Background
    public void onInitAnnouncement() {
        initAnnouncement();
    }

    // 같은 쓰레드에서 처리를 위함
    private void initAnnouncement() {
        if (announcementModel == null || room == null || view == null) {
            LogUtil.e(TAG, "announcementModel == null || room == null || view == null");
            return;
        }

        Topic.Announcement announcement = TeamInfoLoader.getInstance().getTopic(room.getRoomId())
                .getAnnouncement();
        view.dismissProgressWheel();
        if (announcement != null) {
            view.setAnnouncement(announcement);
        }
    }

    public void setAnnouncementActionFrom(boolean fromUser) {
        announcementModel.setActionFromUser(fromUser);
    }

    public void onUpdateAnnouncement(boolean isOpened) {
        announcementModel.updateAnnouncementStatus(room.getTeamId(), room.getRoomId(), isOpened);
    }

    public void onChangeAnnouncementOpenStatusAction(boolean shouldOpened) {
        if (!announcementModel.isActionFromUser()) {
            view.openAnnouncement(shouldOpened);
        }
    }

    @Background
    public void onCheckAnnouncementExistsAndCreate(long messageId) {
        Topic.Announcement announcement =
                announcementModel.getAnnouncement(room.getTeamId(), room.getRoomId());

        if (announcement == null) {
            createAnnouncement(messageId);
            return;
        }

        view.showAnnouncementCreateDialog(messageId);
    }

    // 같은 쓰레드에서 처리를 위함
    private void createAnnouncement(long messageId) {
        view.showProgressWheel();

        announcementModel.createAnnouncement(room.getTeamId(), room.getRoomId(), messageId);

        boolean isSocketConnected = JandiSocketManager.getInstance().isConnectingOrConnected();
        if (!isSocketConnected) {
            initAnnouncement();
        }
    }

    @Background
    public void onCreateAnnouncement(long messageId) {
        createAnnouncement(messageId);
    }

    @Background
    public void onDeleteAnnouncementAction() {
        view.showProgressWheel();

        announcementModel.deleteAnnouncement(room.getTeamId(), room.getRoomId());

        initAnnouncement();
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

        if (NetworkCheckUtil.isConnected()) {
            long roomId = messageListModel.getRoomId();
            if (roomId <= 0) {
                return Room.INVALID_ROOM_ID;
            } else {
                return roomId;
            }
        }

        return Room.INVALID_ROOM_ID;
    }

    @Background
    public void onInitMessages(boolean withProgress) {

        if (withProgress) {
            view.showProgressView();
        }

        long roomId = room.getRoomId();
        long teamId = room.getTeamId();

        LogUtil.i(TAG, "roomId = " + roomId);

        if (roomId <= 0) {
            roomId = getRoomId();
            if (roomId <= Room.INVALID_ROOM_ID) {
                view.showInvalidEntityToast();
                view.finish();
                return;
            }
        }

        room.setRoomId(roomId);

        String readyMessage = messageListModel.getReadyMessage(roomId);
        view.initRoomInfo(roomId, readyMessage);

        ResMessages.Link lastLinkMessage = MessageRepository.getRepository().getLastMessageWitoutDirty(roomId);

        // 1. 처음 접근 하는 토픽/DM 인 경우
        // 2. 오랜만에 접근 하는 토픽/DM 인 경우
        NewMessageFromLocalContainer newMessageQueue = new NewMessageFromLocalContainer(currentMessageState);

        OldMessageContainer oldMessageQueue = new OldMessageContainer(currentMessageState);
        oldMessageQueue.setCacheMode(true);

        if (lastLinkMessage == null
                || lastLinkMessage.id < 0
                || (lastLinkMessage.id > 0 && messageListModel.isBefore30Days(lastLinkMessage.time))) {
            messageListModel.clearLinks(teamId, roomId);

            currentMessageState.setLoadHistory(false);

            adapterModel.setMoreFromNew(true);
            adapterModel.setNewLoadingComplete();
        }

        long myId = TeamInfoLoader.getInstance().getMyId();

        long lastReadLinkId = messageListModel.getLastReadLinkId(roomId, myId);
        messagePointer.setLastReadLinkId(lastReadLinkId);
        messageListModel.setRoomId(roomId);

        isInitialized = true;

        addQueue(oldMessageQueue);
        addQueue(newMessageQueue);

    }

    private void addQueue(MessageContainer messageContainer) {
        if (!messageLoadSubscription.isUnsubscribed()) {
            messageRequestQueue.onNext(messageContainer);
        }
    }

    public void addNewMessageQueue(boolean cacheMode) {
        if (!isInitialized) {
            return;
        }
        NewMessageContainer messageQueue = new NewMessageContainer(currentMessageState);
        messageQueue.setCacheMode(cacheMode);
        addQueue(messageQueue);
    }

    public void addOldMessageQueue(boolean cacheMode) {
        OldMessageContainer messageQueue = new OldMessageContainer(currentMessageState);
        messageQueue.setCacheMode(cacheMode);
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

    private void checkInvalidTopic(int responseCode) {
        if (responseCode == 40300) {
            view.showInvalidEntityToast();
            view.finish();
        }
    }

    private void loadNewMessage(NewMessageContainer messageContainer) {
        long lastUpdateLinkId = MessageRepository.getRepository()
                .getLastMessageWitoutDirty(room.getRoomId()).id;

        long roomId = room.getRoomId();
        ResMessages.Link item = adapterModel.getItem(0);
        long firstCursorLinkId;
        if (item != null) {
            firstCursorLinkId = item.id;
        } else {
            firstCursorLinkId = -1;
        }
        int currentItemCount = adapterModel.getCount();

        List<ResMessages.Link> newMessages = getResUpdateMessages(lastUpdateLinkId);

        if (newMessages == null || newMessages.isEmpty()) {
            boolean hasMessages = firstCursorLinkId > 0 && hasMessages(currentItemCount);
            view.showEmptyView(!hasMessages);

            if (currentMessageState.isFirstLoadNewMessage()) {
                currentMessageState.setIsFirstLoadNewMessage(false);
                long lastReadLinkId = messagePointer.getLastReadLinkId();
                int markerPosition = adapterModel.indexOfLinkId(lastReadLinkId);
                if (markerPosition ==
                        adapterModel.getCount() - adapterModel.getDummyMessageCount() - 1) {
                    messagePointer.setLastReadLinkId(-1);
                }
            }

            return;
        }

        boolean cacheMode = messageContainer.isCacheMode();
        boolean firstLoadNewMessage = currentMessageState.isFirstLoadNewMessage();
        messageListModel.sortByTime(newMessages);

        view.updateRecyclerViewInfo();

        List<ResMessages.Link> archivedList = Observable.from(newMessages)
                .filter(link -> TextUtils.equals(link.status, "archived"))
                .collect((Func0<ArrayList<ResMessages.Link>>) ArrayList::new, ArrayList::add)
                .toBlocking()
                .firstOrDefault(new ArrayList<>());

        if (cacheMode) {
            messageListModel.upsertMessages(roomId, newMessages);

            if (firstLoadNewMessage) {
                int messagesCount = MessageRepository.getRepository()
                        .getMessagesCount(roomId, messagePointer.getLastReadLinkId());
                if (messagesCount <= 1) {
                    messagePointer.setLastReadLinkId(-1);
                }
            }

            long minLinkId = -1;
            ResMessages.Link lastItem = adapterModel.getItem(adapterModel.getCount() - adapterModel.getDummyMessageCount() - 1);
            if (lastItem != null) {
                minLinkId = lastItem.id + 1;
            }

            if (minLinkId > 0) {
                newMessages = MessageRepository.getRepository().getMessages(roomId, minLinkId, Integer.MAX_VALUE);
            } else {
                newMessages = new ArrayList<>();
            }

        }

        messageListModel.presetTextContent(newMessages);

        addMessages(newMessages, firstLoadNewMessage, archivedList);

        if (newMessages.size() > 0) {
            long lastLinkId = newMessages.get(newMessages.size() - 1).id;
            messageListModel.upsertMyMarker(roomId, lastLinkId);
        }
        addMarkerQueue();

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void addMessages(List<ResMessages.Link> newMessages, boolean firstLoadNewMessage,
                     List<ResMessages.Link> archivedList) {
        for (ResMessages.Link link : newMessages) {
            if (link.message instanceof ResMessages.StickerMessage
                    || link.message instanceof ResMessages.TextMessage) {
                if (TeamInfoLoader.getInstance().getMyId() == link.fromEntity) {
                    int idxOfMessageId = adapterModel.indexOfDummyMessageId(link.messageId);
                    if (idxOfMessageId >= 0) {
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

        int[] arrayForCounting = new int[1];

        int count = adapterModel.getCount();
        if (count <= 0) {
            return 0;
        }
        Observable.range(0, count - 1)
                .map(idx -> adapterModel.getItem(idx))
                .filter(link -> TextUtils.equals(link.status, "event"))
                .subscribe(link1 -> {
                    arrayForCounting[0]++;
                });

        return arrayForCounting[0];
    }

    private List<ResMessages.Link> getResUpdateMessages(final long linkId) {
        List<ResMessages.Link> messages = new ArrayList<>();


        Observable.create(new Observable.OnSubscribe<ResMessages>() {
            @Override
            public void call(Subscriber<? super ResMessages> subscriber) {

                // 300 개씩 요청함
                adapterModel.setMoreFromNew(false);

                ResMessages afterMarkerMessage = null;
                try {
                    afterMarkerMessage =
                            messageListModel.getAfterMarkerMessage(
                                    linkId, MessageManipulator.MAX_OF_MESSAGES);

                    int messageCount = afterMarkerMessage.records.size();
                    boolean isEndOfRequest = messageCount < MessageManipulator.MAX_OF_MESSAGES;
                    if (isEndOfRequest) {
                        ResMessages.Link lastItem;
                        if (messageCount == 0) {
                            // 기존 리스트에서 마지막 링크 정보 가져옴
                            lastItem = MessageRepository.getRepository()
                                    .getLastMessageWitoutDirty(room.getRoomId());
                        } else {
                            lastItem = afterMarkerMessage.records.get(messageCount - 1);
                            // 새로 불러온 정보에서 마지막 링크 정보 가져옴
                        }
                        if (lastItem != null) {
                            boolean before30Days = !DateComparatorUtil.isBefore30Days(lastItem.time);
                            currentMessageState.setLoadHistory(before30Days);
                        } else {
                            // 알 수 없는 경우에도 히스토리 로드 하지 않기
                            currentMessageState.setLoadHistory(false);
                        }

                        adapterModel.setNewNoMoreLoading();
                    } else {
                        adapterModel.setMoreFromNew(true);
                        adapterModel.setNewLoadingComplete();
                    }

                } catch (RetrofitException retrofitError) {
                    checkInvalidTopic(retrofitError.getResponseCode());
                    retrofitError.printStackTrace();
                    adapterModel.setMoreFromNew(true);
                    adapterModel.setNewLoadingComplete();
                }

                subscriber.onNext(afterMarkerMessage);
                subscriber.onCompleted();
            }
        })
                .collect(() -> messages, (resUpdateMessages, o) -> messages.addAll(o.records))
                .subscribe(resUpdateMessages -> {
                }, Throwable::printStackTrace);
        return messages;
    }

    private void updateLinkPreview(UpdateLinkPreviewMessageContainer messageContainer) {
        long messageId = messageContainer.getData();

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
        view.refreshMessages();
    }

    public void onInitializeEmptyLayout(long entityId) {
        boolean isTopic = messageListModel.isTopic(entityId);
        if (isTopic) {
            TopicRoom topic = TeamInfoLoader.getInstance().getTopic(entityId);
            int topicMemberCount = topic.getMemberCount();
            int teamMemberCount = TeamInfoLoader.getInstance().getUserList().size() - 1;

            if (teamMemberCount <= 0) {
                view.insertTeamMemberEmptyLayout();
            } else if (topicMemberCount <= 1) {
                view.insertTopicMemberEmptyLayout();
            } else {
                view.clearEmptyMessageLayout();
            }

        } else {
            view.insertMessageEmptyLayout();
        }
    }

    @Background(serial = "send_message")
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

    @Background(serial = "send_message")
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
            String readyMessage = messageListModel.getReadyMessage(room.getRoomId());
            view.initMentionControlViewModel(readyMessage);
        }

        if (NetworkCheckUtil.isConnected()) {
            view.dismissOfflineLayer();
        } else {
            view.showOfflineLayer();
        }
    }

    @Background
    public void onSaveTempMessageAction(String tempMessage) {
        if (TextUtils.isEmpty(tempMessage)) {
            messageListModel.deleteReadyMessage(room.getRoomId());
            return;
        }

        messageListModel.saveTempMessage(room.getRoomId(), tempMessage);
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

    @Background
    public void onDeletePollMessageAction(long messageId, long pollId) {
        view.showProgressWheel();

        try {
            messageListModel.deletePollMessage(room.getTeamId(), pollId);

            MessageRepository.getRepository().deleteLinkByMessageId(messageId);
            view.dismissProgressWheel();

            int position = adapterModel.indexByMessageId(messageId);
            if (position >= 0) {
                adapterModel.remove(position);
                view.refreshMessages();
            }
        } catch (RetrofitException e) {
            LogUtil.e(Log.getStackTraceString(e));
            view.dismissProgressWheel();
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
            view.dismissProgressWheel();
        }
    }

    @Background
    public void onDeleteDummyMessageAction(long localId) {
        messageListModel.deleteDummyMessageAtDatabase(localId);
        view.refreshMessages();
    }

    @Background
    public void onDeleteMessageAction(int messageType, long messageId) {
        view.showProgressWheel();
        try {
            if (messageType == MessageItem.TYPE_STRING) {
                messageListModel.deleteMessage(messageId);
            } else if (messageType == MessageItem.TYPE_STICKER
                    || messageType == MessageItem.TYPE_STICKER_COMMNET) {
                messageListModel.deleteSticker(messageId, messageType);
            }
            MessageRepository.getRepository().deleteLinkByMessageId(messageId);

            view.dismissProgressWheel();

            int position = adapterModel.indexByMessageId(messageId);
            if (position >= 0) {
                adapterModel.remove(position);
                view.refreshMessages();
            }


            messageListModel.trackMessageDeleteSuccess(messageId);

        } catch (RetrofitException e) {
            view.dismissProgressWheel();
            int errorCode = e.getStatusCode();
            messageListModel.trackMessageDeleteFail(errorCode);
        } catch (Exception e) {
            view.dismissProgressWheel();
            messageListModel.trackMessageDeleteFail(-1);
        }

    }

    @Background
    public void onDeleteTopicAction() {
        view.showProgressWheel();
        try {
            messageListModel.deleteTopic(room.getEntityId(), room.getEntityType());
            view.dismissProgressWheel();
            view.finish();
        } catch (RetrofitException e) {
            view.dismissProgressWheel();
            e.printStackTrace();
        } catch (Exception e) {
            view.dismissProgressWheel();
        }
    }

    public void onTeamLeaveEvent(long teamId, long memberId) {
        if (!messageListModel.isCurrentTeam(teamId)) {
            return;
        }

        if (memberId == room.getEntityId()) {
            String name = TeamInfoLoader.getInstance().getName(memberId);
            view.showLeavedMemberDialog(name);
            view.showDisabledUserLayer();
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

    @Background
    public void onMessageStarredAction(long messageId) {
        try {
            messageListModel.registStarredMessage(room.getTeamId(), messageId);

            view.showMessageStarSuccessToast();
            int position = adapterModel.indexByMessageId(messageId);
            if (position >= 0) {
                adapterModel.modifyStarredStateByPosition(position, true);
            }
            view.refreshMessages();
            EventBus.getDefault().post(new StarredInfoChangeEvent());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Background
    public void onMessageUnStarredAction(long messageId) {
        try {
            messageListModel.unregistStarredMessage(room.getTeamId(), messageId);

            view.showMessageUnStarSuccessToast();
            int position = adapterModel.indexByMessageId(messageId);
            if (position >= 0) {
                adapterModel.modifyStarredStateByPosition(position, false);
            }
            view.refreshMessages();
            EventBus.getDefault().post(new StarredInfoChangeEvent());
        } catch (RetrofitException e) {
            e.printStackTrace();
        }
    }

    @Background
    public void onRoomMarkerChange(long teamId, long roomId, long memberId, long lastLinkId) {
        // Presenter 에 객체들이 initialize 되기 전에 Event 가 호출 되면서 NullPointerException 이 나는 경우가 있음.
        // Presenter 전체적으로 개선해야 할 필요 있음.
        if (view != null) {
            view.refreshMessages();
        }
    }

    @Background
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

    public void updateStarredOfMessage(int messageId, boolean starred) {
        int index = adapterModel.indexByMessageId(messageId);
        adapterModel.modifyStarredStateByPosition(index, starred);
        view.refreshMessages();
    }

    public void onNetworkConnect() {
        if (adapterModel.getCount() <= 0) {
            // roomId 설정 후...
            onInitMessages(true);
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

    public void addNewMessageOfLocalQueue() {
        if (isInitialized) {
            NewMessageFromLocalContainer container = new NewMessageFromLocalContainer(currentMessageState);
            addQueue(container);
        }
    }

    public void upsertLink(ResMessages.Link link) {
        if (link == null) {
            return;
        }

        messageListModel.upsertMessage(link);
    }

    public interface View {
        void showDisabledUserLayer();

        void showInactivedUserLayer();

        void setAnnouncement(Topic.Announcement announcement);

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

        void modifyTitle(String name);

        void showDuplicatedTopicName();

        void showModifyEntityError();

        void openAnnouncement(boolean shouldOpenAnnouncement);

        void showAnnouncementCreateDialog(long messageId);

        void showStickerMessageMenuDialog(ResMessages.StickerMessage stickerMessage);

        void showTextMessageMenuDialog(ResMessages.TextMessage textMessage,
                                       boolean isDirectMessage, boolean isMyMessage);

        void showCommentMessageMenuDialog(ResMessages.CommentMessage message);

        void showLeavedMemberDialog(String name);

        void showMessageStarSuccessToast();

        void showMessageUnStarSuccessToast();

        void modifyStarredInfo(long messageId, boolean isStarred);

        void dismissUserStatusLayout();

        void refreshMessages();

        void updateRecyclerViewInfo();
    }

}
