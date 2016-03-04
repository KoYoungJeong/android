package com.tosslab.jandi.app.ui.message.v2;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.tosslab.jandi.app.lists.BotEntity;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.exception.ExceptionData;
import com.tosslab.jandi.app.network.models.ResAnnouncement;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.to.MessageState;
import com.tosslab.jandi.app.ui.message.to.queue.MessageQueue;
import com.tosslab.jandi.app.ui.message.to.queue.NewMessageQueue;
import com.tosslab.jandi.app.ui.message.to.queue.OldMessageQueue;
import com.tosslab.jandi.app.ui.message.v2.domain.Room;
import com.tosslab.jandi.app.ui.message.v2.model.AnnouncementModel;
import com.tosslab.jandi.app.ui.message.v2.model.MessageListModel;
import com.tosslab.jandi.app.utils.DateComparatorUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by tee on 16. 2. 16..
 */
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

    public void setView(View view) {
        this.view = view;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public void onInitMessageState(long lastReadEntityId) {
        currentMessageState = new MessageState();
        currentMessageState.setFirstItemId(lastReadEntityId);
    }

    public void setEntityInfo() {
        messageListModel.setEntityInfo(room.getEntityType(), room.getEntityId());
    }

    @AfterInject
    void initObjects() {
        initMessageLoader();

        initMessageLoadQueue();
    }

    void initMessageLoader() {

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
//                            sendMessage(messageQueue);
                            break;
                        case UpdateLinkPreview:
//                            updateLinkPreview(messageQueue);
                            break;
                    }
                }, throwable -> {
                    LogUtil.e("Message Publish Fail!! \n" + throwable);
                }, () -> {

                });
    }

    public void onDetermineUserStatus() {
        if (messageListModel.isEnabledIfUser(room.getEntityId())) {
            view.showDisabledUserLayer();
        }
    }

    @Background
    public void onInitAnnouncement() {
        ResAnnouncement announcement = announcementModel.getAnnouncement(room.getTeamId(), room.getEntityId());
        view.dismissProgressWheel();
        if (announcement != null) {
            view.setAnnouncement(announcement, announcementModel.isAnnouncementOpened(room.getEntityId()));
        }
    }

    @Background
    public void onRetrieveRoomId(long entityId, boolean withProgress) {
        if (withProgress) {
            view.showProgressView();
        }

        FormattedEntity entity = EntityManager.getInstance().getEntityById(room.getEntityId());
        boolean isInTopic = !entity.isUser() && !(entity instanceof BotEntity);
        if (isInTopic) {
            if (entityId <= 0) {
                if (withProgress) {
                    view.dismissProgressView();
                }
                view.showInvalidEntityToast();
                view.finish();
            } else {
                room.setRoomId(entityId);
                view.setRoomId(entityId, withProgress);
                onRetrieveReadyMessage();
            }
        } else {

            long roomId = messageListModel.getRoomId();
            if (roomId <= 0) {
                if (withProgress) {
                    view.dismissProgressView();
                }
                view.showInvalidEntityToast();
                view.finish();
            } else {
                room.setRoomId(roomId);
                view.setRoomId(roomId, withProgress);
                onRetrieveReadyMessage();
            }
        }

    }

    public void onInitMessages(long teamId, long roomId, long entityId,
                               int currentItemCountWithoutDummy,
                               boolean withProgress) {
        LogUtil.i("tony", "roomId = " + roomId);
        if (roomId <= 0) {
            view.retrieveRoomId(withProgress);
            return;
        }

        if (withProgress) {
            view.showProgressView();
        }

        long lastReadLinkId = messageListModel.getLastReadLinkId(roomId, messageListModel.getMyId());
        currentMessageState.setFirstItemId(lastReadLinkId);

        ResMessages.Link lastLinkMessage = messageListModel.getLastLinkMessage(roomId);

        // 1. 처음 접근 하는 토픽/DM 인 경우
        // 2. 오랜만에 접근 하는 토픽/DM 인 경우
        NewMessageQueue newMessageQueue = new NewMessageQueue(currentMessageState);
        newMessageQueue.setTeamId(teamId);
        newMessageQueue.setRoomId(roomId);
        newMessageQueue.setCacheMode(true);
        newMessageQueue.setCurrentItemCount(currentItemCountWithoutDummy);

        OldMessageQueue oldMessageQueue = new OldMessageQueue(currentMessageState);
        oldMessageQueue.setTeamId(teamId);
        oldMessageQueue.setRoomId(roomId);
        oldMessageQueue.setCurrentItemCount(currentItemCountWithoutDummy);
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
        view.setLastReadLinkId(messageListModel.getLastReadLinkId(teamId, roomId));

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

    public void addNewMessageQueue() {
        addQueue(new NewMessageQueue(currentMessageState));
    }

    private void loadOldMessage(long teamId, long roomId, long linkId, int currentItemCount,
                                boolean isCacheMode) {

        ResMessages resOldMessage = null;

        try {
            // 모든 요청은 dummy 가 아닌 실제 데이터 기준...
            int offset = Math.min(
                    Math.max(MessageManipulator.NUMBER_OF_MESSAGES, currentItemCount),
                    MessageManipulator.MAX_OF_MESSAGES);

            resOldMessage =
                    loadOldMessagesFromDatabase(roomId, linkId, currentItemCount, offset);

            if (resOldMessage == null) {
                resOldMessage = loadOldMessagesFromServer(
                        teamId, linkId, currentItemCount, offset);

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

                if (currentItemCount <= 0) {
                    view.setEmptyLayoutVisible(true);
                }

            } else {
                List<ResMessages.Link> records = resOldMessage.records;

                messageListModel.sortByTime(records);

                long firstLinkIdInMessage = records.get(0).id;
                currentMessageState.setFirstItemId(firstLinkIdInMessage);
                boolean isFirstMessage = resOldMessage.firstLinkId == firstLinkIdInMessage;
                currentMessageState.setIsFirstMessage(isFirstMessage);

                if (currentItemCount <= 0) {
                    // 처음인 경우 로드된 데이터의 마지막 것으로 설정 ( New Load 와 관련있음)
                    currentMessageState.setLastUpdateLinkId(records.get(records.size() - 1).id);
                }

                view.dismissProgressWheel();
                view.dismissProgressView();
                view.dismissOldLoadProgress();

                view.setUpOldMessage(resOldMessage.records, currentItemCount, isFirstMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
            view.dismissProgressWheel();
            view.dismissProgressView();
            view.dismissOldLoadProgress();

            if (currentItemCount <= 0) {
                view.setEmptyLayoutVisible(true);
            }
        }
    }

    private void loadOldMessage(OldMessageQueue messageQueue) {
        long teamId = messageQueue.getTeamId();
        long roomId = messageQueue.getRoomId();
        long linkId = ((MessageState) messageQueue.getData()).getFirstItemId();
        int currentItemCount = messageQueue.getCurrentItemCount();
        boolean isCacheMode = messageQueue.isCacheMode();

        loadOldMessage(teamId, roomId, linkId, currentItemCount, isCacheMode);
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
                                                  int currentItemCount, int offset)
            throws RetrofitError {
        ResMessages resOldMessage = null;
        // 캐시가 없는 경우
        if (currentItemCount != 0) {
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
                    view.setLastReadLinkId(-1);
                }
                updateMarker(teamId, resOldMessage.entityId, resOldMessage.lastLinkId);
                messageListModel.deleteCompletedSendingMessage(resOldMessage.entityId);
            }
            // 첫 대화인 경우 해당 채팅방의 보내는 중인 메세지 캐시 데이터 삭제함
        }
        return resOldMessage;
    }

    private ResMessages loadOldMessagesFromDatabase(long roomId, long linkId,
                                                    int currentItemCount, int offset) {
        ResMessages resOldMessage = null;

        List<ResMessages.Link> oldMessages = messageListModel.loadOldMessages(
                roomId,
                linkId,
                currentItemCount,
                offset);

        if (oldMessages != null && !oldMessages.isEmpty()) {
            long firstLinkId = oldMessages.get(oldMessages.size() - 1).id;
            currentMessageState.setFirstItemId(firstLinkId);

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
        long lastUpdateLinkId = data.getLastUpdateLinkId();

        long teamId = messageQueue.getTeamId();
        long roomId = messageQueue.getRoomId();
        int currentItemCount = messageQueue.getCurrentItemCount();

        if (lastUpdateLinkId < 0) {
            loadOldMessage(teamId, roomId, lastUpdateLinkId, currentItemCount, true);
        }

        List<ResMessages.Link> newMessages = null;

        boolean moveToLink = data.isFirstLoadNewMessage();

        if (currentMessageState.loadHistory()) {

            try {
                newMessages = messageListModel.getNewMessage(lastUpdateLinkId);
            } catch (RetrofitError e) {
                e.printStackTrace();

                if (e.getKind() == RetrofitError.Kind.NETWORK) {


                } else if (e.getKind() == RetrofitError.Kind.HTTP) {
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
            view.setEmptyLayoutVisible(true);

            if (currentMessageState.isFirstLoadNewMessage()) {
                view.setUpLastReadLinkIdIfPosition();
                view.moveLastReadLink();
                view.justRefresh();
                currentMessageState.setIsFirstLoadNewMessage(false);
            }

            return;
        }

        if (messageQueue.isCacheMode()) {
            messageListModel.upsertMessages(roomId, newMessages);
        }

        messageListModel.sortByTime(newMessages);

        long lastLinkId = newMessages.get(newMessages.size() - 1).id;
        currentMessageState.setLastUpdateLinkId(lastLinkId);

        messageListModel.upsertMyMarker(roomId, lastLinkId);
        updateMarker(messageQueue.getTeamId(), roomId, lastUpdateLinkId);

        view.setUpNewMessage(newMessages, messageListModel.getMyId(),
                currentMessageState.isFirstLoadNewMessage(), moveToLink);

        currentMessageState.setIsFirstLoadNewMessage(false);

        view.setEmptyLayoutVisible(true);
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
                    afterMarkerMessage = messageListModel.getAfterMarkerMessage(linkId, MessageManipulator.MAX_OF_MESSAGES);
                    int messageCount = afterMarkerMessage.records.size();
                    boolean isEndOfRequest = messageCount < MessageManipulator.MAX_OF_MESSAGES;
                    if (isEndOfRequest) {
                        ResMessages.Link lastItem;
                        if (messageCount == 0) {
                            // 기존 리스트에서 마지막 링크 정보 가져옴
                            lastItem = view.getLastItemFromAdapterWithoutDummy();
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
    public void updateRoomInfo(long teamId, long roomId, long entityId) {
        messageListModel.updateMarkerInfo(teamId, roomId);

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

        if (roomId > 0) {
            NewMessageQueue newMessageQueue = new NewMessageQueue(currentMessageState);
            addQueue(newMessageQueue);
        }

    }

    private void updateMarker(long teamId, long roomId, long lastUpdateLinkId) {
        if (currentMessageState.getLastUpdateLinkId() <= 0) {
            return;
        }

        try {
            if (lastUpdateLinkId > 0) {
                messageListModel.updateLastLinkId(lastUpdateLinkId);
                messageListModel.updateMarkerInfo(teamId, roomId);
            }
        } catch (RetrofitError e) {
            e.printStackTrace();
            LogUtil.e("set marker failed", e);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("set marker failed", e);
        }
    }

    public void updateMarker(long teamId, long roomId) {
        messageListModel.updateMarkerInfo(teamId, roomId);
    }

    public void onRetrieveReadyMessage() {
        if (messageListModel.isUser(room.getEntityId())) {
            if (room.getRoomId() <= 0) {
                // FIXME roomId 가 설정된 이후에 메소드가 호출 되어야 한다.
                return;
            }

            view.setReadyMessage(messageListModel.getReadyMessage(room.getRoomId()));
        } else {
            view.setReadyMessage(messageListModel.getReadyMessage(room.getEntityId()));
        }
    }

    public interface View {
        void showDisabledUserLayer();

        void setAnnouncement(ResAnnouncement announcement, boolean shouldOpenAnnouncement);

        void showProgressWheel();

        void dismissProgressWheel();

        void showProgressView();

        void dismissProgressView();

        void retrieveRoomId(boolean withProgress);

        void setRoomId(long roomId, boolean shouldShowProgress);

        void showInvalidEntityToast();

        void setMoreNewFromAdapter(boolean isMoreNew);

        void setNewLoadingComplete();

        void setMarkerInfo(long roomId);

        void setLastReadLinkId(long lastReadLinkId);

        void setUpOldMessage(List<ResMessages.Link> records, int currentItemCount,
                             boolean isFirstMessage);

        void setUpNewMessage(List<ResMessages.Link> records, long myId,
                             boolean isFirstLoad,
                             boolean moveToLinkId);

        void showOldLoadProgress();

        void setEmptyLayoutVisible(boolean visible);

        void justRefresh();

        void refreshAll();

        void dismissOfflineLayer();

        void showOfflineLayer();

        void clearMessages();

        void scrollToPositionWithOffset(int itemPosition, int firstVisibleItemTop);

        void scrollToPosition(int itemPosition);

        void moveLastPage();

        void setReadyMessage(String text);

        void dismissOldLoadProgress();

        void setNewNoMoreLoading();

        ResMessages.Link getLastItemFromAdapterWithoutDummy();

        void setUpLastReadLinkIdIfPosition();

        void finish();

        void moveLastReadLink();

        void insertTeamMemberEmptyLayout();

        void insertTopicMemberEmptyLayout();

        void clearEmptyMessageLayout();

        void insertMessageEmptyLayout();
    }

}
