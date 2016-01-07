package com.tosslab.jandi.app.ui.message.v2.search.presenter;

import android.support.v4.app.Fragment;
import android.view.MenuItem;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.StarredInfoChangeEvent;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.lists.messages.MessageItem;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.models.ResAnnouncement;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.socket.JandiSocketManager;
import com.tosslab.jandi.app.services.socket.to.SocketAnnouncementEvent;
import com.tosslab.jandi.app.ui.message.model.menus.MenuCommand;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.ui.message.to.MessageState;
import com.tosslab.jandi.app.ui.message.to.queue.CheckAnnouncementQueue;
import com.tosslab.jandi.app.ui.message.to.queue.MessageQueue;
import com.tosslab.jandi.app.ui.message.to.queue.NewMessageQueue;
import com.tosslab.jandi.app.ui.message.to.queue.OldMessageQueue;
import com.tosslab.jandi.app.ui.message.v2.loader.MarkerNewMessageLoader;
import com.tosslab.jandi.app.ui.message.v2.loader.MarkerOldMessageLoader;
import com.tosslab.jandi.app.ui.message.v2.loader.NewsMessageLoader;
import com.tosslab.jandi.app.ui.message.v2.loader.OldMessageLoader;
import com.tosslab.jandi.app.ui.message.v2.model.AnnouncementModel;
import com.tosslab.jandi.app.ui.message.v2.model.MessageListModel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

@EBean
public class MessageSearchListPresenterImpl implements MessageSearchListPresenter {
    @Bean
    MessageListModel messageListModel;
    @Bean
    AnnouncementModel announcementModel;

    private View view;
    private MessageState messageState;
    private OldMessageLoader oldMessageLoader;
    private NewsMessageLoader newsMessageLoader;
    private PublishSubject<MessageQueue> messagePublishSubject;
    private Subscription messageSubscription;
    private int teamId;
    private int roomId;
    private int entityId;
    private int lastMarker;
    private int entityType;


    @AfterInject
    void initObject() {
        messageState = new MessageState();

        messagePublishSubject = PublishSubject.create();

        messageSubscription = messagePublishSubject
                .onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .subscribe(messageQueue -> {
                    switch (messageQueue.getQueueType()) {
                        case Old:
                            loadOldMessage(messageQueue);
                            break;
                        case New:
                            loadNewMessage(messageQueue);
                            break;
                        case Send:
                        case CheckAnnouncement:
                            getAnnouncement();
                            break;
                    }
                }, throwable -> LogUtil.e("Message Publish Fail!!", throwable), () -> {
                });

        MarkerNewMessageLoader newsMessageLoader = new MarkerNewMessageLoader();
        newsMessageLoader.setMessageListModel(messageListModel);
        newsMessageLoader.setMessageState(messageState);

        MarkerOldMessageLoader oldMessageLoader = new MarkerOldMessageLoader();
        oldMessageLoader.setMessageListModel(messageListModel);
        oldMessageLoader.setMessageState(messageState);

        this.newsMessageLoader = newsMessageLoader;
        this.oldMessageLoader = oldMessageLoader;

    }

    @Override
    public void setView(View view) {

        this.view = view;
        ((MarkerNewMessageLoader) newsMessageLoader).setView(view);
        ((MarkerOldMessageLoader) oldMessageLoader).setView(view);

    }

    private void loadOldMessage(MessageQueue messageQueue) {
        if (oldMessageLoader != null) {
            ResMessages resMessages = oldMessageLoader.load(roomId, ((MessageState) messageQueue
                    .getData()).getFirstItemId());

            if (resMessages != null && roomId <= 0) {
                roomId = resMessages.entityId;
                view.setRoomId(roomId);
                messageListModel.updateMarkerInfo(teamId, roomId);
                messageListModel.setRoomId(roomId);
            }

        }
    }

    private void loadNewMessage(MessageQueue messageQueue) {


        if (newsMessageLoader != null) {
            MessageState data = (MessageState) messageQueue.getData();
            int lastUpdateLinkId = data.getLastUpdateLinkId();

            if (lastUpdateLinkId < 0 && oldMessageLoader != null) {
                oldMessageLoader.load(roomId, lastUpdateLinkId);
            }

            newsMessageLoader.load(roomId, lastUpdateLinkId);
        }
    }

    @Override
    public void onDestory() {
        messageSubscription.unsubscribe();
    }

    @Override
    public void onRequestNewMessage() {
        sendMessagePublisherEvent(new NewMessageQueue(messageState));
    }

    @Override
    public void checkEnabledUser(int entityId) {
        if (!messageListModel.isEnabledIfUser(entityId)) {
            view.setDisabledUser();
        }
    }

    @Override
    public boolean onOptionItemSelected(Fragment fragment, MenuItem item, int teamId, int entityId) {
        MenuCommand menuCommand = messageListModel.getMenuCommand(fragment,
                teamId, entityId, item);

        if (menuCommand != null) {
            menuCommand.execute(item);
            return true;
        }
        return false;
    }

    @Background
    @Override
    public void onInitRoomInfo() {
        if (roomId <= 0) {
            boolean user = EntityManager.getInstance().getEntityById(entityId).isUser();

            if (!user) {
                roomId = entityId;
            } else if (NetworkCheckUtil.isConnected()) {

                roomId = messageListModel.initRoomId();


            }
        }

        int savedLastLinkId = messageListModel.getLastReadLinkId(roomId, messageListModel.getMyId());
        int realLastLinkId = Math.max(savedLastLinkId, lastMarker);

        if (roomId > 0) {
            view.setRoomId(roomId);
        }
        view.setLastReadLinkId(realLastLinkId);
        messageListModel.updateMarkerInfo(teamId, roomId);
        messageListModel.setRoomId(roomId);


        sendMessagePublisherEvent(new CheckAnnouncementQueue());
        sendMessagePublisherEvent(new OldMessageQueue(messageState));


        if (view.isForeground()) {
            sendMessagePublisherEvent(new NewMessageQueue(messageState));
        }
        view.setRoomInit(true);
    }

    @Override
    public void setDefaultInfos(int teamId, int roomId, int entityId, int lastMarker, int entityType) {
        this.teamId = teamId;
        this.roomId = roomId;
        this.entityId = entityId;
        this.lastMarker = lastMarker;
        this.entityType = entityType;

        messageListModel.setEntityInfo(entityType, entityId);
        messageState.setFirstItemId(lastMarker);

    }

    private void sendMessagePublisherEvent(MessageQueue messageQueue) {
        if (!messageSubscription.isUnsubscribed()) {
            messagePublishSubject.onNext(messageQueue);
        }
    }

    private void getAnnouncement() {
        ResAnnouncement announcement = announcementModel.getAnnouncement(teamId, roomId);
        view.dismissProgressWheel();
        view.setAnnouncement(announcement, announcementModel.isAnnouncementOpened(entityId));

    }

    @Override
    public void onAccouncementOpen() {
        announcementModel.setActionFromUser(true);
        announcementModel.updateAnnouncementStatus(teamId, roomId, true);
    }

    @Override
    public void onAnnouncementClose() {
        announcementModel.setActionFromUser(true);
        announcementModel.updateAnnouncementStatus(teamId, roomId, false);
    }

    @Background
    @Override
    public void onCreatedAnnouncement(boolean isForeground, boolean isRoomInit) {
        if (!isForeground) {
            messageListModel.updateMarkerInfo(teamId, roomId);
        } else if (isRoomInit) {
            sendMessagePublisherEvent(new NewMessageQueue(messageState));
            sendMessagePublisherEvent(new CheckAnnouncementQueue());
        }
    }

    @Background
    @Override
    public void onUpdateAnnouncement(boolean isForeground, boolean isRoomInit, SocketAnnouncementEvent.Data data) {
        if (!isForeground) {
            announcementModel.setActionFromUser(false);
            messageListModel.updateMarkerInfo(teamId, roomId);
            return;
        }
        if (data != null) {
            if (!announcementModel.isActionFromUser()) {
                view.openAnnouncement(data.isOpened());
            }
        }
        announcementModel.setActionFromUser(false);
    }

    @Background
    @Override
    public void checkAnnouncementExistsAndCreate(int messageId) {
        ResAnnouncement announcement = announcementModel.getAnnouncement(teamId, roomId);

        if (announcement == null || announcement.isEmpty()) {
            createAnnouncement(messageId);
            return;
        }

        view.showCreateAlertDialog((dialog, which) -> createAnnouncement(messageId));
    }

    @Background
    void createAnnouncement(int messageId) {

        view.showProgressWheel();
        announcementModel.createAnnouncement(teamId, roomId, messageId);

        boolean isSocketConnected = JandiSocketManager.getInstance().isConnectingOrConnected();
        if (!isSocketConnected) {
            getAnnouncement();
        }
    }

    @Background
    @Override
    public void onDeleteAnnouncement() {
        view.showProgressWheel();
        announcementModel.deleteAnnouncement(teamId, roomId);

        boolean isSocketConnected = JandiSocketManager.getInstance().isConnectingOrConnected();
        if (!isSocketConnected) {
            getAnnouncement();
        }
    }

    @Override
    public void onMessageItemClick(Fragment fragment, ResMessages.Link link, int entityId) {
        if (link instanceof DummyMessageLink) {
            DummyMessageLink dummyMessageLink = (DummyMessageLink) link;

            view.showDummyMessageDialog(dummyMessageLink.getLocalId());

        } else {
            AnalyticsValue.Screen screen = view.getScreen(entityId);

            if (messageListModel.isFileType(link.message)) {
                view.moveFileDetailActivity(fragment, link.messageId, roomId, link.messageId);
                if (((ResMessages.FileMessage) link.message).content.type.startsWith("image")) {
                    AnalyticsUtil.sendEvent(screen, AnalyticsValue.Action.FileView_ByPhoto);
                } else {
                    AnalyticsUtil.sendEvent(screen, AnalyticsValue.Action.FileView_ByFile);
                }
            } else if (messageListModel.isCommentType(link.message)) {
                view.moveFileDetailActivity(fragment, link.message.feedbackId, roomId, link.messageId);
                AnalyticsUtil.sendEvent(screen, AnalyticsValue.Action.FileView_ByComment);
            } else if (messageListModel.isStickerCommentType(link.message)) {
                view.moveFileDetailActivity(fragment, link.message
                        .feedbackId, roomId, link.messageId);
                AnalyticsUtil.sendEvent(screen, AnalyticsValue.Action.FileView_ByComment);
            }
        }


    }

    @Override
    public void onMessageItemLongClick(ResMessages.Link link) {

        if (link instanceof DummyMessageLink) {
            DummyMessageLink dummyMessageLink = (DummyMessageLink) link;

            if (messageListModel.isFailedDummyMessage(dummyMessageLink)) {
                view.showDummyMessageDialog(dummyMessageLink.getLocalId());
            }

        } else if (link.message instanceof ResMessages.TextMessage) {
            ResMessages.TextMessage textMessage = (ResMessages.TextMessage) link.message;
            boolean isDirectMessage = messageListModel.isDirectMessage(entityType);
            view.showMessageMenuDialog(isDirectMessage, false, textMessage);
        } else if (messageListModel.isCommentType(link.message)) {
            view.showMessageMenuDialog(((ResMessages.CommentMessage) link.message));
        }
    }

    @Background
    @Override
    public void deleteMessage(int messageType, int messageId) {
        view.showProgressWheel();
        try {
            if (messageType == MessageItem.TYPE_STRING) {
                messageListModel.deleteMessage(messageId);
                LogUtil.d("deleteMessageInBackground : succeed");
            } else if (messageType == MessageItem.TYPE_STICKER
                    || messageType == MessageItem.TYPE_STICKER_COMMNET) {
                messageListModel.deleteSticker(messageId, messageType);
                LogUtil.d("deleteStickerInBackground : succeed");
            }
            MessageRepository.getRepository().deleteLinkByMessageId(messageId);
            view.deleteLinkByMessageId(messageId);

            messageListModel.trackMessageDeleteSuccess(messageId);

        } catch (RetrofitError e) {
            LogUtil.e("deleteMessageInBackground : FAILED", e);
            int errorCode = e.getResponse() != null ? e.getResponse().getStatus() : -1;
            messageListModel.trackMessageDeleteFail(errorCode);
        } catch (Exception e) {
            LogUtil.e("deleteMessageInBackground : FAILED", e);
            messageListModel.trackMessageDeleteFail(-1);
        }
        view.dismissProgressWheel();
    }

    @Background
    @Override
    public void onModifyEntity(String topicName) {
        view.showProgressWheel();
        try {
            messageListModel.modifyTopicName(entityType, entityId, topicName);
            view.modifyEntitySucceed(topicName);
            messageListModel.trackChangingEntityName(entityType);
            EntityManager.getInstance().getEntityById(entityId).getEntity().name = topicName;
        } catch (RetrofitError e) {
            if (e.getResponse() != null && e.getResponse().getStatus() == JandiConstants.NetworkError.DUPLICATED_NAME) {
                view.showFailToast(JandiApplication.getContext().getString(R.string.err_entity_duplicated_name));
            } else {
                view.showFailToast(JandiApplication.getContext().getString(R.string.err_entity_modify));
            }
        } catch (Exception e) {
            view.showFailToast(JandiApplication.getContext().getString(R.string.err_entity_modify));
        } finally {
            view.dismissProgressWheel();
        }
    }

    @Background
    @Override
    public void registStarredMessage(int teamId, int messageId) {
        try {
            messageListModel.registStarredMessage(teamId, messageId);
            view.showSuccessToast(JandiApplication.getContext().getString(R.string.jandi_message_starred));
            view.modifyStarredInfo(messageId, true);
            EventBus.getDefault().post(new StarredInfoChangeEvent());
        } catch (RetrofitError e) {
            e.printStackTrace();
        }

    }

    @Background
    @Override
    public void unregistStarredMessage(int teamId, int messageId) {
        try {
            messageListModel.unregistStarredMessage(teamId, messageId);
            view.showSuccessToast(JandiApplication.getContext().getString(R.string.jandi_unpinned_message));
            view.modifyStarredInfo(messageId, false);
            EventBus.getDefault().post(new StarredInfoChangeEvent());
        } catch (RetrofitError e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTeamLeave(int teamId, int memberId) {
        if (!messageListModel.isCurrentTeam(teamId)) {
            return;
        }


        if (memberId == entityId) {
            view.showLeavedMemberDialog(entityId);
            view.setDisabledUser();
        }
    }

    @Override
    public void onRequestOldMessage() {
        if (!messageState.isFirstMessage()) {
            sendMessagePublisherEvent(new OldMessageQueue(messageState));
        }
    }


}
