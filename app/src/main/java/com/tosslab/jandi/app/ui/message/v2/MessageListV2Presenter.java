package com.tosslab.jandi.app.ui.message.v2;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;

import com.tosslab.jandi.app.lists.BotEntity;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.exception.ExceptionData;
import com.tosslab.jandi.app.network.models.ResAnnouncement;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.to.MessageState;
import com.tosslab.jandi.app.ui.message.to.StickerInfo;
import com.tosslab.jandi.app.ui.message.to.queue.MessageQueue;
import com.tosslab.jandi.app.ui.message.to.queue.NewMessageQueue;
import com.tosslab.jandi.app.ui.message.to.queue.OldMessageQueue;
import com.tosslab.jandi.app.ui.message.v2.model.AnnouncementModel;
import com.tosslab.jandi.app.ui.message.v2.model.MessageListModel;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.List;

import retrofit.RetrofitError;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by tee on 16. 2. 16..
 */
@EBean
public class MessageListV2Presenter {

    @Bean
    MessageListModel messageListModel;
    @Bean
    AnnouncementModel announcementModel;

    View view;

    private PublishSubject<MessageQueue> messageLoadPublishSubject;
    private Subscription messageLoadSubscription;
    private MessageState currentMessageState;

    public void setView(View view) {
        this.view = view;
    }

    public void onInitMessageState(long lastReadEntityId) {
        currentMessageState = new MessageState();
        currentMessageState.setFirstItemId(lastReadEntityId);
    }

    public void setEntityInfo(int entityType, long entityId) {
        messageListModel.setEntityInfo(entityType, entityId);
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

    public void onDetermineUserStatus(long entityId) {
        if (messageListModel.isEnabledIfUser(entityId)) {
            view.showDisabledUserLayer();
        }
    }

    @Background
    public void onInitAnnouncement(long teamId, long entityId) {
        ResAnnouncement announcement = announcementModel.getAnnouncement(teamId, entityId);
        view.dismissProgressWheel();
        if (announcement != null) {
            view.setAnnouncement(announcement, announcementModel.isAnnouncementOpened(entityId));
        }
    }

    @Background
    public void onRetrieveRoomId(long entityId, boolean withProgress) {
        if (withProgress) {
            view.showProgressView();
        }

        FormattedEntity entity = EntityManager.getInstance().getEntityById(entityId);
        boolean isInTopic = !entity.isUser() && !(entity instanceof BotEntity);
        if (isInTopic) {
            if (entityId <= 0) {
                if (withProgress) {
                    view.dismissProgressView();
                }
                view.showInvalidEntityToast();
                view.finish();
            } else {
                view.setRoomId(entityId, withProgress);
            }
            return;
        }

        long roomId = messageListModel.getRoomId();
        if (roomId <= 0) {
            if (withProgress) {
                view.dismissProgressView();
            }
            view.showInvalidEntityToast();
            view.finish();
        } else {
            view.setRoomId(entityId, withProgress);
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

            newMessageQueue.setLoadHistory(false);

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

        List<ResMessages.Link> newMessage = null;
        if (messageQueue.loadHistory()) {

            try {
                newMessage = messageListModel.getNewMessage(lastUpdateLinkId);
            } catch (RetrofitError e) {
                e.printStackTrace();

                if (e.getKind() == RetrofitError.Kind.NETWORK) {



                } else if (e.getKind() == RetrofitError.Kind.HTTP) {

                    try {
                        ExceptionData exceptionData = (ExceptionData) e.getBodyAs(ExceptionData.class);


                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {

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
            LogUtil.e("set marker failed", e);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("set marker failed", e);
        }
    }

    public void onRetrieveReadyMessage(long roomId, long entityId) {
        if (messageListModel.isUser(entityId)) {
            if (roomId <= 0) {
                // FIXME roomId 가 설정된 이후에 메소드가 호출 되어야 한다.
                return;
            }

            view.setReadyMessage(messageListModel.getReadyMessage(roomId));
        } else {
            view.setReadyMessage(messageListModel.getReadyMessage(entityId));
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

        void setUpOldMessage(List<ResMessages.Link> records, int currentItemCount, boolean isFirstMessage);

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

        void finish();
    }

}
