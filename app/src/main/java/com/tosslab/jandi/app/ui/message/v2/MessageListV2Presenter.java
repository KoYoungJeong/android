package com.tosslab.jandi.app.ui.message.v2;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

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

    public void setView(View view) {
        this.view = view;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public void setEntityInfo() {
        messageListModel.setEntityInfo(room.getEntityType(), room.getEntityId());
    }

    public void setMessagePointer(MessagePointer messagePointer) {
        this.messagePointer = messagePointer;
    }

    @AfterInject
    void initObjects() {
        currentMessageState = new MessageState();

        initMessageLoadQueue();
    }

    void initMessageLoadQueue() {
        messageRequestQueue = PublishSubject.create();
        messageLoadSubscription = messageRequestQueue
                .onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .subscribe(messageContainer -> {
                    switch (messageContainer.getQueueType()) {
                        case Old:
                            loadOldMessage((OldMessageContainer) messageContainer);
                            break;
                        case New:
                            loadNewMessage((NewMessageContainer) messageContainer);
                            break;
                        case NewFromLocal:
                            addNewMessageFromLocal();
                            break;
                        case Send:
                            sendMessage((SendingMessageContainer) messageContainer);
                            break;
                        case UpdateLinkPreview:
                            updateLinkPreview((UpdateLinkPreviewMessageContainer) messageContainer);
                            break;
                    }
                }, throwable -> {
                    LogUtil.e("Message Publish Fail!!");
                    throwable.printStackTrace();
                }, () -> {

                });

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

    private void loadOldMessage(long teamId, long roomId, long linkId, boolean isFirst,
                                boolean isCacheMode) {

        if (!isFirst) {
            view.showOldLoadProgress();
        }

        ResMessages resOldMessage;

        try {
            int messagesCount = linkId > 0
                    ? MessageRepository.getRepository().getMessagesCount(roomId, linkId)
                    : MessageManipulator.NUMBER_OF_MESSAGES;

            // 모든 요청은 dummy 가 아닌 실제 데이터 기준...
            int max = Math.max(MessageManipulator.NUMBER_OF_MESSAGES, messagesCount);
            int offset = Math.min(max, MessageManipulator.MAX_OF_MESSAGES);

            resOldMessage =
                    loadOldMessagesFromDatabase(roomId, linkId, isFirst, offset);

            if (resOldMessage == null) {
                resOldMessage = loadOldMessagesFromServer(teamId, linkId, isFirst, offset);

                if (isCacheMode && resOldMessage != null) {
                    messageListModel.upsertMessages(resOldMessage);
                }
            } else if (resOldMessage.records.size() < offset) {
                resOldMessage = loadMoreOldMessagesFromServer(resOldMessage, offset, isCacheMode);
            }

            if (resOldMessage == null
                    || resOldMessage.records == null
                    || resOldMessage.records.isEmpty()) {

                view.dismissProgressWheel();
                view.dismissProgressView();
                view.dismissOldLoadProgress();

                if (isFirst) {
                    view.showEmptyView(true);
                }

            } else {
                List<ResMessages.Link> records = resOldMessage.records;
                messageListModel.sortByTime(records);

                long firstLinkIdInMessage = records.get(0).id;
                currentMessageState.setIsFirstLoadOldMessage(false);
                boolean isFirstMessage = resOldMessage.firstLinkId == firstLinkIdInMessage;
                currentMessageState.setIsFirstMessage(isFirstMessage);

                view.dismissProgressWheel();
                view.dismissProgressView();
                view.dismissOldLoadProgress();

                if (!isFirst) {
                    view.updateRecyclerViewInfo();
                }

                messageListModel.presetTextContent(resOldMessage.records);
                adapterModel.addAll(0, resOldMessage.records);
                view.refreshMessages();
                if (!isFirstMessage) {
                    adapterModel.setOldLoadingComplete();
                } else {
                    adapterModel.setOldNoMoreLoading();
                }
                view.setUpOldMessage(isFirst);

                int count = resOldMessage.records.size() - 1;
                ResMessages.Link lastLink = resOldMessage.records.get(count);
                long myId = TeamInfoLoader.getInstance().getMyId();
                Marker myMarker = RoomMarkerRepository.getInstance().getMarker(room.getRoomId(), myId);

                if (myMarker.getReadLinkId() < lastLink.id) {
                    addMarkerQueue();
                    messageListModel.upsertMyMarker(room.getRoomId(), lastLink.id);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            view.dismissProgressWheel();
            view.dismissProgressView();
            view.dismissOldLoadProgress();

            if (isFirst) {
                view.showEmptyView(true);
            }
        }
    }

    private void loadOldMessage(OldMessageContainer messageContainer) {

        long teamId = room.getTeamId();
        long roomId = room.getRoomId();
        ResMessages.Link item = adapterModel.getItem(0);
        long linkId;
        if (item != null) {
            linkId = item.id;
        } else {
            linkId = -1;
        }
        boolean isFirstLoad = messageContainer.getData().isFirstLoadOldMessage();
        boolean isCacheMode = messageContainer.isCacheMode();

        loadOldMessage(teamId, roomId, linkId, isFirstLoad, isCacheMode);

    }

    @NonNull
    private ResMessages loadMoreOldMessagesFromServer(ResMessages resOldMessage,
                                                      int offset,
                                                      boolean isCacheMode)
            throws RetrofitException {
        try {
            // 캐시된 데이터가 부족한 경우
            ResMessages.Link firstLink =
                    resOldMessage.records.get(resOldMessage.records.size() - 1);
            ResMessages addOldMessage =
                    messageListModel.getOldMessage(firstLink.id, offset);

            if (isCacheMode) {
                messageListModel.upsertMessages(addOldMessage);
            }

            addOldMessage.records.addAll(resOldMessage.records);

            resOldMessage = addOldMessage;
        } catch (RetrofitException retrofitError) {
            checkInvalidTopic(retrofitError.getResponseCode());
            retrofitError.printStackTrace();
        }
        return resOldMessage;
    }

    private void checkInvalidTopic(int responseCode) {
        if (responseCode == 40300) {
            view.showInvalidEntityToast();
            view.finish();
        }
    }

    @Nullable
    private ResMessages loadOldMessagesFromServer(long teamId, long linkId,
                                                  boolean isFirst, int offset)
            throws RetrofitException {
        ResMessages resOldMessage = null;
        // 캐시가 없는 경우
        if (!isFirst) {
            // 요청한 링크 ID 이전 값 가져오기
            try {
                resOldMessage = messageListModel.getOldMessage(linkId, offset);
            } catch (RetrofitException retrofitError) {
                checkInvalidTopic(retrofitError.getResponseCode());
                retrofitError.printStackTrace();
            }
        } else {
            // 첫 요청이라 판단
            // 마커 기준 위아래 값 요청
            resOldMessage = messageListModel.getBeforeMarkerMessage(messagePointer.getLastReadLinkId());
            if (resOldMessage != null
                    && resOldMessage.records != null
                    && resOldMessage.records.size() > 0) {
                if (resOldMessage.records.get(resOldMessage.records.size() - 1).id == linkId) {
                    messagePointer.setLastReadLinkId(-1);
                }
                adapterModel.removeAllDummy();
                view.refreshMessages();
                messageListModel.deleteCompletedSendingMessage(resOldMessage.entityId);
            }
            // 첫 대화인 경우 해당 채팅방의 보내는 중인 메세지 캐시 데이터 삭제함
        }
        return resOldMessage;
    }

    private ResMessages loadOldMessagesFromDatabase(long roomId, long linkId,
                                                    boolean firstLoad, int offset) {
        ResMessages resOldMessage = null;

        List<ResMessages.Link> oldMessages = messageListModel.loadOldMessages(
                roomId,
                linkId,
                firstLoad,
                offset);

        if (oldMessages != null && !oldMessages.isEmpty()) {
            resOldMessage = new ResMessages();
            // 현재 챗의 첫 메세지가 아니라고 하기 위함
            resOldMessage.firstLinkId = -1;
            // 마커 업로드를 하지 않기 위함
            resOldMessage.lastLinkId = oldMessages.get(0).id;
            resOldMessage.entityId = roomId;
            resOldMessage.records = oldMessages;
        }

        return resOldMessage;
    }

    private void loadNewMessage(NewMessageContainer messageContainer) {
        MessageState data = messageContainer.getData();
        long lastUpdateLinkId = MessageRepository.getRepository()
                .getLastMessageWitoutDirty(room.getRoomId()).id;

        long teamId = room.getTeamId();
        long roomId = room.getRoomId();
        ResMessages.Link item = adapterModel.getItem(0);
        long firstCursorLinkId;
        if (item != null) {
            firstCursorLinkId = item.id;
        } else {
            firstCursorLinkId = -1;
        }
        int currentItemCount = adapterModel.getCount();

        if (lastUpdateLinkId < 0) {
            messageListModel.deleteAllDummyMessageAtDatabase(room.getRoomId());

            boolean firstLoadOldMessage = messageContainer.getData().isFirstLoadOldMessage();
            loadOldMessage(teamId, roomId, lastUpdateLinkId, firstLoadOldMessage, true);

            lastUpdateLinkId = MessageRepository.getRepository()
                    .getLastMessageWitoutDirty(room.getRoomId()).id;

            currentItemCount = MessageRepository.getRepository()
                    .getMessagesCount(roomId, firstCursorLinkId);

        }


        boolean moveToLink = data.isFirstLoadNewMessage();

        boolean loadHistory = currentMessageState.loadHistory();

        if (!loadHistory) {
            moveToLink = true;
        }

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

            int size = newMessages.size();
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
        }).collect(() -> messages, (resUpdateMessages, o) -> messages.addAll(o.records))
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

    private void sendMessage(SendingMessageContainer messageContainer) {
        SendingMessage data = messageContainer.getData();
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

        view.refreshMessages();
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

    public void addNewMessageFromLocal() {
        if (view == null || adapterModel == null) {
            return;
        }
        view.updateRecyclerViewInfo();

        ResMessages.Link item = adapterModel.getItem(adapterModel.getCount() - 1 - adapterModel.getDummyMessageCount());

        long minId = -1;
        if (item != null) {
            minId = item.id + 1;
        }

        List<ResMessages.Link> messages = MessageRepository.getRepository().getMessages(room.getRoomId(), minId, Integer.MAX_VALUE);
        messageListModel.presetTextContent(messages);
        addMessages(messages, currentMessageState.isFirstLoadNewMessage(), new ArrayList<>());
        if (messages.size() > 0) {
            long lastLinkId = messages.get(messages.size() - 1).id;
            messageListModel.upsertMyMarker(room.getRoomId(), lastLinkId);

            addMarkerQueue();
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
