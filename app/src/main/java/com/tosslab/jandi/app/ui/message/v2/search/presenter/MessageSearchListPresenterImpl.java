package com.tosslab.jandi.app.ui.message.v2.search.presenter;

import android.support.v4.app.Fragment;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.StarredInfoChangeEvent;
import com.tosslab.jandi.app.lists.messages.MessageItem;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.start.Announcement;
import com.tosslab.jandi.app.network.socket.JandiSocketManager;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.authority.Level;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.ui.message.to.MessageState;
import com.tosslab.jandi.app.ui.message.to.queue.CheckAnnouncementContainer;
import com.tosslab.jandi.app.ui.message.to.queue.MessageContainer;
import com.tosslab.jandi.app.ui.message.to.queue.NewMessageContainer;
import com.tosslab.jandi.app.ui.message.to.queue.OldMessageContainer;
import com.tosslab.jandi.app.ui.message.v2.loader.MarkerNewMessageLoader;
import com.tosslab.jandi.app.ui.message.v2.loader.MarkerOldMessageLoader;
import com.tosslab.jandi.app.ui.message.v2.loader.NewsMessageLoader;
import com.tosslab.jandi.app.ui.message.v2.loader.OldMessageLoader;
import com.tosslab.jandi.app.ui.message.v2.model.AnnouncementModel;
import com.tosslab.jandi.app.ui.message.v2.model.MessageListModel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrMessageDelete;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import rx.Completable;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class MessageSearchListPresenterImpl implements MessageSearchListPresenter {
    MessageListModel messageListModel;
    AnnouncementModel announcementModel;
    private View view;

    private MessageState messageState;
    private OldMessageLoader oldMessageLoader;
    private NewsMessageLoader newsMessageLoader;
    private PublishSubject<MessageContainer> messagePublishSubject;
    private Subscription messageSubscription;
    private long teamId;
    private long roomId;
    private long entityId;
    private long lastMarker;
    private int entityType;

    @Inject
    public MessageSearchListPresenterImpl(MessageListModel messageListModel,
                                          AnnouncementModel announcementModel,
                                          View view) {
        this.messageListModel = messageListModel;
        this.announcementModel = announcementModel;
        this.view = view;
        initObject();
    }

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

        newsMessageLoader.setView(view);
        oldMessageLoader.setView(view);
    }

    private void loadOldMessage(MessageContainer messageContainer) {
        if (oldMessageLoader != null) {
            ResMessages resMessages = oldMessageLoader.load(roomId, ((MessageState) messageContainer
                    .getData()).getFirstItemId());

            if (resMessages != null && roomId <= 0) {
                roomId = resMessages.entityId;
                view.setRoomId(roomId);
                messageListModel.setRoomId(roomId);
            }

        }
    }

    private void loadNewMessage(MessageContainer messageContainer) {


        if (newsMessageLoader != null) {
            MessageState data = (MessageState) messageContainer.getData();
            long lastUpdateLinkId = data.getLastUpdateLinkId();

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
        sendMessagePublisherEvent(new NewMessageContainer(messageState));
    }

    @Override
    public void checkEnabledUser(long entityId) {
        if (messageListModel.isUser(entityId)) {
            if (messageListModel.isInactiveUser(entityId)) {
                view.setInavtiveUser();
            } else if (!messageListModel.isEnabledIfUser(entityId)) {
                view.setDisabledUser();
            } else {
                view.dismissUserStatusLayout();
            }
        }
    }

    @Override
    public void onInitRoomInfo() {

        Observable.just(roomId)
                .observeOn(Schedulers.io())
                .map(roomid -> {
                    if (roomId <= 0) {
                        boolean user = TeamInfoLoader.getInstance().isUser(entityId);
                        if (!user) {
                            roomId = entityId;
                            return roomId;
                        } else if (NetworkCheckUtil.isConnected()) {
                            roomId = messageListModel.createChat(entityId);
                            return roomId;
                        }
                    }
                    return roomid;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(roomid -> {
                    this.roomId = roomid;

                    if (roomid > 0) {
                        view.setRoomId(roomid);
                    }
                    messageListModel.setRoomId(roomid);
                    Level myLevel = TeamInfoLoader.getInstance().getMyLevel();
                    if (TeamInfoLoader.getInstance().isTopic(roomid)) {
                        view.showReadOnly(TeamInfoLoader.getInstance().getRoom(roomid).isReadOnly()
                                && myLevel != Level.Owner
                                && myLevel != Level.Admin);
                    }
                })
                .observeOn(Schedulers.io())
                .doOnNext(roomid -> {
                    sendMessagePublisherEvent(new CheckAnnouncementContainer());
                    sendMessagePublisherEvent(new OldMessageContainer(messageState));
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(roomid -> {
                    if (view.isForeground()) {
                        sendMessagePublisherEvent(new NewMessageContainer(messageState));
                    }
                    view.setRoomInit(true);

                });

    }

    @Override
    public void setDefaultInfos(long teamId, long roomId, long entityId, long lastMarker, int entityType) {
        this.teamId = teamId;
        this.roomId = roomId;
        this.entityId = entityId;
        this.lastMarker = lastMarker;
        this.entityType = entityType;

        messageListModel.setEntityInfo(entityType, entityId);
        messageState.setFirstItemId(lastMarker);

    }

    private void sendMessagePublisherEvent(MessageContainer messageContainer) {
        if (!messageSubscription.isUnsubscribed()) {
            messagePublishSubject.onNext(messageContainer);
        }
    }

    private void getAnnouncement() {
        Completable.fromAction(() -> {
            view.dismissProgressWheel();
        }).subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    if (TeamInfoLoader.getInstance().isTopic(roomId)) {
                        Announcement announcement = TeamInfoLoader.getInstance().getTopic(roomId).getAnnouncement();
                        view.setAnnouncement(announcement);
                    }
                });
    }

    @Override
    public void onAccouncementOpen() {
        Completable.fromAction(() -> {
            announcementModel.setActionFromUser(true);
            announcementModel.updateAnnouncementStatus(teamId, roomId, true);
        }).subscribeOn(Schedulers.newThread()).subscribe();
    }

    @Override
    public void onAnnouncementClose() {
        Completable.fromAction(() -> {
            announcementModel.setActionFromUser(true);
            announcementModel.updateAnnouncementStatus(teamId, roomId, false);
        }).subscribeOn(Schedulers.newThread()).subscribe();
    }

    @Override
    public void onCreatedAnnouncement(boolean isRoomInit) {
        if (isRoomInit) {
            Observable.just(TeamInfoLoader.getInstance().getTopic(roomId).getAnnouncement())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(announcement -> view.setAnnouncement(announcement));
        }
    }

    @Override
    public void onUpdateAnnouncement(boolean isForeground, boolean isRoomInit, boolean opened) {
        Completable.fromAction(() -> {

            if (!isForeground) {
                announcementModel.setActionFromUser(false);
                return;
            }
            if (!announcementModel.isActionFromUser()) {
                view.openAnnouncement(opened);
            }
            announcementModel.setActionFromUser(false);
        }).subscribeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    @Override
    public void onDeleteAnnouncement() {
        Observable.just(true)
                .subscribeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> {
                    view.showProgressWheel();
                })
                .observeOn(Schedulers.io())
                .doOnNext(it -> {
                    announcementModel.deleteAnnouncement(teamId, roomId);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> {
                    boolean isSocketConnected = JandiSocketManager.getInstance().isConnectingOrConnected();
                    if (!isSocketConnected) {
                        getAnnouncement();
                    }
                });

    }

    @Override
    public void onMessageItemClick(Fragment fragment, ResMessages.Link link, long entityId) {
        if (link instanceof DummyMessageLink) {
            DummyMessageLink dummyMessageLink = (DummyMessageLink) link;

            view.showDummyMessageDialog(dummyMessageLink.getLocalId());

            return;
        }

        AnalyticsValue.Screen screen = view.getScreen(entityId);

        if (link.message instanceof ResMessages.CommentMessage
                || link.message instanceof ResMessages.CommentStickerMessage) {

            if (ResMessages.FeedbackType.POLL.value().equals(link.feedbackType)) {

                view.movePollDetailActivity(link.poll.getId());

            } else {
                AnalyticsUtil.sendEvent(screen, AnalyticsValue.Action.FileView_ByComment);

                view.moveFileDetailActivity(fragment, link.message.feedbackId, roomId, link.messageId);
            }
        } else {
            if (link.message instanceof ResMessages.PollMessage) {

                view.movePollDetailActivity(link.poll.getId());

            } else if (link.message instanceof ResMessages.FileMessage) {
                view.moveFileDetailActivity(fragment, link.messageId, roomId, link.messageId);
                String type = ((ResMessages.FileMessage) link.message).content.type;
                if ((type.startsWith("image") && !type.contains("dwg"))) {
                    AnalyticsUtil.sendEvent(screen, AnalyticsValue.Action.FileView_ByPhoto);
                } else {
                    AnalyticsUtil.sendEvent(screen, AnalyticsValue.Action.FileView_ByFile);
                }
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

    @Override
    public void deleteMessage(int messageType, long messageId) {
        Completable.fromAction(() -> view.showProgressWheel())
                .subscribeOn(AndroidSchedulers.mainThread()).subscribe();

        Completable.fromCallable(() -> {
            if (messageType == MessageItem.TYPE_STRING) {
                messageListModel.deleteMessage(messageId);
                LogUtil.d("deleteMessageInBackground : succeed");
            } else if (messageType == MessageItem.TYPE_STICKER) {
                messageListModel.deleteSticker(messageId);
                LogUtil.d("deleteStickerInBackground : succeed");
            } else if (messageType == MessageItem.TYPE_STICKER_COMMNET) {
                long feedbackId = MessageRepository.getRepository().getMessage(messageId).feedbackId;
                messageListModel.deleteStickerComment(feedbackId, messageId);
                LogUtil.d("deleteStickerCommentInBackground : succeed");
            }
            MessageRepository.getRepository().deleteMessageOfMessageId(messageId);
            SprinklrMessageDelete.sendLog(messageId);
            return true;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnUnsubscribe(() -> view.dismissProgressWheel())
                .subscribe(() -> {
                    view.deleteLinkByMessageId(messageId);

                }, t -> {
                    LogUtil.e("deleteMessageInBackground : FAILED", t);
                    if (t instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) t;
                        int errorCode = e.getStatusCode();
                        SprinklrMessageDelete.sendFailLog(errorCode);
                    } else {
                        SprinklrMessageDelete.sendFailLog(-1);
                    }
                });

    }

    @Override
    public void registStarredMessage(long teamId, long messageId) {
        Completable.fromCallable(() -> {
            messageListModel.registStarredMessage(teamId, messageId);
            return true;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    view.showSuccessToast(JandiApplication.getContext().getString(R.string.jandi_message_starred));
                    view.modifyStarredInfo(messageId, true);
                    EventBus.getDefault().post(new StarredInfoChangeEvent());
                }, Throwable::printStackTrace);

    }

    @Override
    public void unregistStarredMessage(long teamId, long messageId) {
        Completable.fromCallable(() -> {
            messageListModel.unregistStarredMessage(teamId, messageId);
            return true;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    view.showSuccessToast(JandiApplication.getContext().getString(R.string.jandi_unpinned_message));
                    view.modifyStarredInfo(messageId, false);
                    EventBus.getDefault().post(new StarredInfoChangeEvent());
                }, Throwable::printStackTrace);
    }

    @Override
    public void onTeamLeave(long teamId, long memberId) {
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
            sendMessagePublisherEvent(new OldMessageContainer(messageState));
        }
    }

}
