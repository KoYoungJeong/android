package com.tosslab.jandi.app.ui.message.v2;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.events.messages.RefreshNewMessageEvent;
import com.tosslab.jandi.app.events.messages.StarredInfoChangeEvent;
import com.tosslab.jandi.app.lists.BotEntity;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.lists.messages.MessageItem;
import com.tosslab.jandi.app.local.orm.repositories.MarkerRepository;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.exception.ExceptionData;
import com.tosslab.jandi.app.network.models.ReqSendMessageV3;
import com.tosslab.jandi.app.network.models.ResAnnouncement;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.network.socket.JandiSocketManager;
import com.tosslab.jandi.app.ui.message.to.MessageState;
import com.tosslab.jandi.app.ui.message.to.SendingMessage;
import com.tosslab.jandi.app.ui.message.to.StickerInfo;
import com.tosslab.jandi.app.ui.message.to.queue.MessageQueue;
import com.tosslab.jandi.app.ui.message.to.queue.NewMessageQueue;
import com.tosslab.jandi.app.ui.message.to.queue.OldMessageQueue;
import com.tosslab.jandi.app.ui.message.to.queue.SendingMessageQueue;
import com.tosslab.jandi.app.ui.message.to.queue.UpdateLinkPreviewMessageQueue;
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

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
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

    private PublishSubject<MessageQueue> messageLoadPublishSubject;
    private Subscription messageLoadSubscription;
    private MessageState currentMessageState;
    private Room room;
    private MessagePointer messagePointer;

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
        messageLoadPublishSubject = PublishSubject.create();
        messageLoadSubscription = messageLoadPublishSubject
                .onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .subscribe(messageQueue -> {
                    switch (messageQueue.getQueueType()) {
                        case Old:
                            loadOldMessage((OldMessageQueue) messageQueue);
                            break;
                        case New:
                            loadNewMessage((NewMessageQueue) messageQueue);
                            break;
                        case Send:
                            sendMessage((SendingMessageQueue) messageQueue);
                            break;
                        case UpdateLinkPreview:
                            updateLinkPreview((UpdateLinkPreviewMessageQueue) messageQueue);
                            break;
                    }
                }, throwable -> {
                    LogUtil.e("Message Publish Fail!! \n" + throwable);
                }, () -> {

                });
    }

    public void onDetermineUserStatus() {
        if (!messageListModel.isEnabledIfUser(room.getEntityId())) {
            view.showDisabledUserLayer();
        }
    }

    @Background
    public void onInitAnnouncement() {
        initAnnouncement();
    }

    // 같은 쓰레드에서 처리를 위함
    private void initAnnouncement() {
        ResAnnouncement announcement = announcementModel.getAnnouncement(room.getTeamId(), room.getEntityId());
        view.dismissProgressWheel();
        if (announcement != null) {
            view.setAnnouncement(announcement, announcementModel.isAnnouncementOpened(room.getEntityId()));
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
        ResAnnouncement announcement =
                announcementModel.getAnnouncement(room.getTeamId(), room.getRoomId());

        if (announcement == null || announcement.isEmpty()) {
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

        boolean isSocketConnected = JandiSocketManager.getInstance().isConnectingOrConnected();
        if (!isSocketConnected) {
            initAnnouncement();
        }
    }

    private long getRoomId() {
        long entityId = room.getEntityId();

        FormattedEntity entity = EntityManager.getInstance().getEntityById(entityId);
        boolean isInTopic = !entity.isUser() && !(entity instanceof BotEntity);
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

        ResMessages.Link lastLinkMessage = messageListModel.getLastLinkMessage(roomId);

        // 1. 처음 접근 하는 토픽/DM 인 경우
        // 2. 오랜만에 접근 하는 토픽/DM 인 경우
        NewMessageQueue newMessageQueue = new NewMessageQueue(currentMessageState);
        newMessageQueue.setCacheMode(true);

        OldMessageQueue oldMessageQueue = new OldMessageQueue(currentMessageState);
        oldMessageQueue.setCacheMode(true);

        if (lastLinkMessage == null
                || lastLinkMessage.id < 0
                || (lastLinkMessage.id > 0 && messageListModel.isBefore30Days(lastLinkMessage.time))) {
            messageListModel.clearLinks(teamId, roomId);

            currentMessageState.setLoadHistory(false);

            view.setMoreNewFromAdapter(true);
            view.setNewLoadingComplete();
        }

        view.setMarkerInfo(roomId);
        messagePointer.setLastReadLinkId(messageListModel.getLastReadLinkId(teamId, roomId));

        messageListModel.updateMarkerInfo(teamId, roomId);
        messageListModel.setRoomId(roomId);

        addQueue(oldMessageQueue);
        addQueue(newMessageQueue);
    }

    private void addQueue(MessageQueue messageQueue) {
        if (!messageLoadSubscription.isUnsubscribed()) {
            messageLoadPublishSubject.onNext(messageQueue);
        }
    }

    public void addNewMessageQueue(boolean cacheMode) {
        NewMessageQueue messageQueue = new NewMessageQueue(currentMessageState);
        messageQueue.setCacheMode(cacheMode);
        addQueue(messageQueue);
    }

    public void addOldMessageQueue(boolean cacheMode) {
        OldMessageQueue messageQueue = new OldMessageQueue(currentMessageState);
        messageQueue.setCacheMode(cacheMode);
        addQueue(messageQueue);
    }

    public void addUpdateLinkPreviewMessageQueue(long messageId) {
        UpdateLinkPreviewMessageQueue messageQueue = new UpdateLinkPreviewMessageQueue(messageId);
        addQueue(messageQueue);
    }

    public void addSendingMessageQueue(long localId,
                                       String body, StickerInfo stickerInfo,
                                       List<MentionObject> mentionObjects) {
        SendingMessage sendingMessage =
                stickerInfo != null ?
                        new SendingMessage(localId, body, stickerInfo, mentionObjects)
                        : new SendingMessage(localId, new ReqSendMessageV3((body), mentionObjects));
        SendingMessageQueue sendingMessageQueue = new SendingMessageQueue(sendingMessage);
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
                resOldMessage = loadMoreOldMessagesFromServer(resOldMessage, offset);

                if (isCacheMode) {
                    messageListModel.upsertMessages(resOldMessage);
                }
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

                Observable.from(resOldMessage.records)
                        .first()
                        .subscribe(link -> {
                            long firstCursorLinkId = messagePointer.getFirstCursorLinkId();
                            if (firstCursorLinkId < 0) {
                                firstCursorLinkId = Math.max(firstCursorLinkId, link.id);
                            } else {
                                firstCursorLinkId = Math.min(firstCursorLinkId, link.id);
                            }
                            messagePointer.setFirstCursorLinkId(firstCursorLinkId);
                            view.setUpOldMessage(isFirst, isFirstMessage);
                        }, t -> {
                        });
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

    private void loadOldMessage(OldMessageQueue messageQueue) {
        long teamId = room.getTeamId();
        long roomId = room.getRoomId();
        long linkId = messagePointer.getFirstCursorLinkId();
        boolean isFirstLoad = ((MessageState) messageQueue.getData()).isFirstLoadOldMessage();
        boolean isCacheMode = messageQueue.isCacheMode();

        loadOldMessage(teamId, roomId, linkId, isFirstLoad, isCacheMode);
    }

    @NonNull
    private ResMessages loadMoreOldMessagesFromServer(ResMessages resOldMessage, int offset)
            throws RetrofitError {
        try {
            // 캐시된 데이터가 부족한 경우
            ResMessages.Link firstLink =
                    resOldMessage.records.get(resOldMessage.records.size() - 1);
            ResMessages addOldMessage =
                    messageListModel.getOldMessage(firstLink.id, offset);

            addOldMessage.records.addAll(resOldMessage.records);

            resOldMessage = addOldMessage;
        } catch (RetrofitError retrofitError) {
            retrofitError.printStackTrace();
        }
        return resOldMessage;
    }

    @Nullable
    private ResMessages loadOldMessagesFromServer(long teamId, long linkId,
                                                  boolean isFirst, int offset)
            throws RetrofitError {
        ResMessages resOldMessage = null;
        // 캐시가 없는 경우
        if (!isFirst) {
            // 요청한 링크 ID 이전 값 가져오기
            try {
                resOldMessage = messageListModel.getOldMessage(linkId, offset);
            } catch (RetrofitError retrofitError) {
                retrofitError.printStackTrace();
            }
        } else {
            // 첫 요청이라 판단
            // 마커 기준 위아래 값 요청
            resOldMessage = messageListModel.getBeforeMarkerMessage(linkId);
            if (resOldMessage != null
                    && resOldMessage.records != null
                    && resOldMessage.records.size() > 0) {
                if (resOldMessage.records.get(resOldMessage.records.size() - 1).id == linkId) {
                    messagePointer.setLastReadLinkId(-1);
                }
                updateMarker(teamId, resOldMessage.entityId, resOldMessage.lastLinkId);
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
            long firstLinkId = oldMessages.get(oldMessages.size() - 1).id;
            messagePointer.setFirstCursorLinkId(firstLinkId);

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

    private void loadNewMessage(NewMessageQueue messageQueue) {
        MessageState data = (MessageState) messageQueue.getData();
        final long lastUpdateLinkId = MessageRepository.getRepository()
                .getLastMessage(room.getRoomId()).id;

        long teamId = room.getTeamId();
        long roomId = room.getRoomId();
        long firstCursorLinkId = messagePointer.getFirstCursorLinkId();
        int currentItemCount = MessageRepository.getRepository()
                .getMessagesCount(roomId, firstCursorLinkId);

        if (lastUpdateLinkId < 0) {
            boolean firstLoadOldMessage = ((MessageState) messageQueue.getData()).isFirstLoadOldMessage();
            loadOldMessage(teamId, roomId, lastUpdateLinkId, firstLoadOldMessage, true);
        }

        List<ResMessages.Link> newMessages = null;

        boolean moveToLink = data.isFirstLoadNewMessage();

        boolean loadHistory = currentMessageState.loadHistory();

        if (loadHistory) {

            try {
                newMessages = messageListModel.getNewMessage(lastUpdateLinkId);
            } catch (RetrofitError e) {
                e.printStackTrace();

                if (e.getKind() == RetrofitError.Kind.HTTP) {
                    try {
                        ExceptionData exceptionData = (ExceptionData) e.getBodyAs(ExceptionData.class);
                        LogUtil.e(TAG, "errorCode = " + exceptionData.getCode());
                        if (exceptionData.getCode() == 40017 || exceptionData.getCode() == 40018) {
                            moveToLink = true;
                            newMessages = getResUpdateMessages(lastUpdateLinkId);
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            moveToLink = true;
            newMessages = getResUpdateMessages(lastUpdateLinkId);
        }

        if (newMessages == null || newMessages.isEmpty()) {
            boolean hasMessages = hasMessages(firstCursorLinkId, currentItemCount);
            view.showEmptyView(!hasMessages);

            if (currentMessageState.isFirstLoadNewMessage()) {
                currentMessageState.setIsFirstLoadNewMessage(false);
                view.setUpLastReadLinkIdIfPosition();
                view.notifyDataSetChanged();
                view.moveLastReadLink();
            }

            return;
        }

        boolean cacheMode = messageQueue.isCacheMode();
        if (cacheMode) {
            messageListModel.upsertMessages(roomId, newMessages);
        }

        messageListModel.sortByTime(newMessages);

        boolean firstLoadNewMessage = currentMessageState.isFirstLoadNewMessage();
        if (firstLoadNewMessage) {
            int messagesCount = MessageRepository.getRepository()
                    .getMessagesCount(roomId, messagePointer.getLastReadLinkId());
            if (messagesCount <= 1) {
                messagePointer.setLastReadLinkId(-1);
            }
        }

        view.setUpNewMessage(
                newMessages, messageListModel.getMyId(), firstLoadNewMessage, moveToLink);

        currentMessageState.setIsFirstLoadNewMessage(false);

        currentItemCount = MessageRepository.getRepository()
                .getMessagesCount(roomId, messagePointer.getFirstCursorLinkId());

        boolean hasMessages = hasMessages(firstCursorLinkId, currentItemCount);
        view.showEmptyView(!hasMessages);

        long lastLinkId = newMessages.get(newMessages.size() - 1).id;
        messageListModel.upsertMyMarker(roomId, lastLinkId);
        messageListModel.updateLastLinkId(lastUpdateLinkId);
        messageListModel.updateMarkerInfo(teamId, roomId);
    }

    private boolean hasMessages(long firstCursorLinkId, int currentItemCount) {
        int eventMessageCount = getEventMessageCount(firstCursorLinkId);
        return // event 메세지 외에 다른 메시지들이 있는 경우
                (currentItemCount > 0
                        && (currentItemCount - eventMessageCount > 0))
                        // create 이벤트외에 다른 이벤트가 생성된 경우
                        || eventMessageCount > 1;
    }

    private int getEventMessageCount(long firstCursorLinkId) {
        List<ResMessages.Link> messages = MessageRepository.getRepository()
                .getMessages(room.getRoomId(), firstCursorLinkId, Integer.MAX_VALUE);

        int[] arrayForCounting = new int[1];

        Observable.from(messages)
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
                view.setMoreNewFromAdapter(false);

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
                                    .getLastMessage(room.getRoomId());
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

                        view.setNewNoMoreLoading();
                    } else {
                        view.setMoreNewFromAdapter(true);
                        view.setNewLoadingComplete();
                    }
                } catch (RetrofitError retrofitError) {
                    retrofitError.printStackTrace();
                    view.setMoreNewFromAdapter(true);
                    view.setNewLoadingComplete();
                }

                subscriber.onNext(afterMarkerMessage);
                subscriber.onCompleted();
            }
        }).collect(() -> messages, (resUpdateMessages, o) -> messages.addAll(o.records))
                .subscribe(resUpdateMessages -> {
                }, Throwable::printStackTrace);
        return messages;
    }

    @Background
    public void updateRoomInfo(boolean cacheMode) {
        long roomId = room.getRoomId();
        long teamId = room.getTeamId();

        messageListModel.updateMarkerInfo(teamId, roomId);

        if (roomId > 0) {
            NewMessageQueue newMessageQueue = new NewMessageQueue(currentMessageState);
            newMessageQueue.setCacheMode(cacheMode);
            addQueue(newMessageQueue);
        }

    }

    private void updateLinkPreview(UpdateLinkPreviewMessageQueue messageQueue) {
        long messageId = messageQueue.getData();

        ResMessages.TextMessage textMessage =
                MessageRepository.getRepository().getTextMessage(messageId);

        view.updateLinkPreviewMessage(textMessage);
        view.notifyDataSetChanged();
    }

    public void onInitializeEmptyLayout(long entityId) {
        EntityManager entityManager = EntityManager.getInstance();
        FormattedEntity entity = entityManager.getEntityById(entityId);
        boolean isTopic = messageListModel.isTopic(entity);
        if (isTopic) {
            int topicMemberCount = entity.getMemberCount();
            int teamMemberCount = entityManager.getFormattedUsersWithoutMe().size();

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

    private void updateMarker(long teamId, long roomId, long lastUpdateLinkId) {

        try {
            if (lastUpdateLinkId > 0) {
                messageListModel.updateLastLinkId(lastUpdateLinkId);
                messageListModel.updateMarkerInfo(teamId, roomId);
            }
        } catch (RetrofitError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateMarker() {
        long roomId = room.getRoomId();
        long teamId = room.getTeamId();
        messageListModel.updateMarkerInfo(teamId, roomId);
    }

    @Background
    public void onModifyEntityAction(int entityType, long entityId, String name) {
        view.showProgressWheel();

        try {
            messageListModel.modifyTopicName(entityType, entityId, name);

            view.modifyTitle(name);

            messageListModel.trackChangingEntityName(entityType);

            EntityManager.getInstance().getEntityById(entityId).getEntity().name = name;

        } catch (RetrofitError e) {
            view.dismissProgressWheel();
            if (e.getResponse() != null &&
                    e.getResponse().getStatus() == JandiConstants.NetworkError.DUPLICATED_NAME) {
                view.showDuplicatedTopicName();
            } else {
                view.showModifyEntityError();
            }
        } catch (Exception e) {
            view.dismissProgressWheel();
            view.showModifyEntityError();
        }
    }

    @Background
    public void sendStickerMessage(StickerInfo stickerInfo) {
        long entityId = room.getEntityId();
        long roomId = room.getRoomId();
        long localId = messageListModel.insertSendingMessageIfCan(entityId, roomId, stickerInfo);
        if (localId > 0) {
            view.notifyDataSetChanged();
            view.moveLastPage();

            StickerInfo reqStickerInfo = new StickerInfo(stickerInfo);
            ArrayList<MentionObject> mentions = new ArrayList<>();
            SendingMessage sendingMessage = new SendingMessage(localId, "", reqStickerInfo, mentions);

            SendingMessageQueue messageQueue = new SendingMessageQueue(sendingMessage);

            addQueue(messageQueue);
        }
    }

    @Background
    public void sendTextMessage(String message, List<MentionObject> mentions,
                                ReqSendMessageV3 reqSendMessage) {
        long entityId = room.getEntityId();
        long roomId = room.getRoomId();
        long localId = messageListModel.insertSendingMessageIfCan(entityId, roomId, message, mentions);

        if (localId > 0) {
            // insert to ui
            view.notifyDataSetChanged();
            view.moveLastPage();
            // networking...
            SendingMessage sendingMessage = new SendingMessage(localId, reqSendMessage);

            SendingMessageQueue messageQueue = new SendingMessageQueue(sendingMessage);

            addQueue(messageQueue);
        }
    }

    private void sendMessage(SendingMessageQueue messageQueue) {
        SendingMessage data = (SendingMessage) messageQueue.getData();
        long linkId;
        if (data.getStickerInfo() != null) {
            linkId = messageListModel.sendStickerMessage(room.getTeamId(), room.getEntityId(),
                    data.getStickerInfo(), data.getLocalId());
        } else {
            List<MentionObject> mentions = data.getMentions();
            linkId = messageListModel.sendMessage(data.getLocalId(), data.getMessage(), mentions);
        }
        if (linkId > 0) {
            if (!JandiSocketManager.getInstance().isConnectingOrConnected()) {
                // 소켓이 안 붙어 있으면 임의로 갱신 요청
                EventBus.getDefault().post(new RefreshNewMessageEvent());
            }
        }

        view.notifyDataSetChanged();
    }

    public void onResume() {
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
        view.notifyDataSetChanged();
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

            view.deleteLinkByMessageId(messageId);

            messageListModel.trackMessageDeleteSuccess(messageId);

        } catch (RetrofitError e) {
            view.dismissProgressWheel();
            int errorCode = e.getResponse() != null ? e.getResponse().getStatus() : -1;
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
            messageListModel.trackDeletingEntity(room.getEntityType());
            view.dismissProgressWheel();
            view.finish();
        } catch (RetrofitError e) {
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
            String name = EntityManager.getInstance().getEntityNameById(memberId);
            view.showLeavedMemberDialog(name);
            view.showDisabledUserLayer();
        }
    }


    public void unSubscribeMessageQueue() {
        if (messageLoadSubscription != null) {
            messageLoadSubscription.unsubscribe();
        }
    }

    @Background
    public void onMessageStarredAction(long messageId) {
        try {
            messageListModel.registStarredMessage(room.getTeamId(), messageId);

            view.showMessageStarSuccessToast();
            view.modifyStarredInfo(messageId, true);
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
            view.modifyStarredInfo(messageId, false);
            EventBus.getDefault().post(new StarredInfoChangeEvent());
        } catch (RetrofitError e) {
            e.printStackTrace();
        }
    }

    @Background
    public void onRoomMarkerChange(long memberId, long lastLinkId) {
        MarkerRepository.getRepository().upsertRoomMarker(
                room.getTeamId(), room.getRoomId(), memberId, lastLinkId);
        view.notifyDataSetChanged();
    }

    public interface View {
        void showDisabledUserLayer();

        void setAnnouncement(ResAnnouncement announcement, boolean shouldOpenAnnouncement);

        void showProgressWheel();

        void dismissProgressWheel();

        void showProgressView();

        void dismissProgressView();

        void initRoomInfo(long roomId, String readyMessage);

        void showInvalidEntityToast();

        void setMoreNewFromAdapter(boolean isMoreNew);

        void setNewLoadingComplete();

        void setMarkerInfo(long roomId);

        void setUpOldMessage(boolean isFirstLoad, boolean isFirstMessage);

        void setUpNewMessage(List<ResMessages.Link> records,
                             long myId, boolean isFirstLoad, boolean moveToLinkId);

        void showOldLoadProgress();

        void showEmptyView(boolean show);

        void notifyDataSetChanged();

        void dismissOfflineLayer();

        void showOfflineLayer();

        void clearMessages();

        void moveLastPage();

        void dismissOldLoadProgress();

        void setNewNoMoreLoading();

        void setUpLastReadLinkIdIfPosition();

        void finish();

        void moveLastReadLink();

        void updateLinkPreviewMessage(ResMessages.TextMessage message);

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

        void deleteLinkByMessageId(long messageId);

        void showLeavedMemberDialog(String name);

        void showMessageStarSuccessToast();

        void showMessageUnStarSuccessToast();

        void modifyStarredInfo(long messageId, boolean isStarred);

    }

}
