package com.tosslab.jandi.app.ui.message.v2.messagehandler;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.events.messages.RefreshNewMessageEvent;
import com.tosslab.jandi.app.lists.BotEntity;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.models.ReqSendMessageV3;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.socket.JandiSocketManager;
import com.tosslab.jandi.app.ui.message.to.MessageState;
import com.tosslab.jandi.app.ui.message.to.SendingMessage;
import com.tosslab.jandi.app.ui.message.to.StickerInfo;
import com.tosslab.jandi.app.ui.message.to.queue.MessageQueue;
import com.tosslab.jandi.app.ui.message.to.queue.NewMessageQueue;
import com.tosslab.jandi.app.ui.message.to.queue.OldMessageQueue;
import com.tosslab.jandi.app.ui.message.to.queue.SendingMessageQueue;
import com.tosslab.jandi.app.ui.message.to.queue.UpdateLinkPreviewMessageQueue;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Presenter;
import com.tosslab.jandi.app.ui.message.v2.loader.NewsMessageLoader;
import com.tosslab.jandi.app.ui.message.v2.loader.NormalNewMessageLoader;
import com.tosslab.jandi.app.ui.message.v2.loader.NormalNewMessageLoader_;
import com.tosslab.jandi.app.ui.message.v2.loader.NormalOldMessageLoader;
import com.tosslab.jandi.app.ui.message.v2.loader.NormalOldMessageLoader_;
import com.tosslab.jandi.app.ui.message.v2.loader.OldMessageLoader;
import com.tosslab.jandi.app.ui.message.v2.model.MessageListModel;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by tee on 16. 2. 16..
 */

public class MessageLoadHandler {

    private Subscription messageHandlingQueueSubscription;

    private MessageListModel model;
    private MessageListV2Presenter presenter;
    private MessageListV2Presenter.View view;

    private OldMessageLoader oldMessageLoader;
    private NewsMessageLoader newsMessageLoader;

    private long teamId;
    private long roomId;
    private long entityId;

    private long lastReadEntityId;
    private MessageState messageState;

    private boolean isRoomInit;
    private PublishSubject<MessageQueue> messageHandlingQueue;

    public MessageLoadHandler(long teamId, long roomId, long entityId, long lastReadEntityId,
                              MessageListV2Presenter.View view, MessageListV2Presenter presenter, MessageListModel model) {
        this.teamId = teamId;
        this.roomId = roomId;
        this.entityId = entityId;
        this.view = view;
        this.presenter = presenter;
        this.model = model;
        this.lastReadEntityId = lastReadEntityId;

        messageState = new MessageState();

        NormalNewMessageLoader newsMessageLoader = NormalNewMessageLoader_
                .getInstance_(JandiApplication.getContext());
        newsMessageLoader.setMessageListModel(model);
        newsMessageLoader.setView(view);
        newsMessageLoader.setPresenter(presenter);
        newsMessageLoader.setMessageState(messageState);

        NormalOldMessageLoader oldMessageLoader = NormalOldMessageLoader_
                .getInstance_(JandiApplication.getContext());
        oldMessageLoader.setMessageListModel(model);
        oldMessageLoader.setView(view);
        oldMessageLoader.setPresenter(presenter);
        oldMessageLoader.setMessageState(messageState);
        oldMessageLoader.setTeamId(teamId);

        this.newsMessageLoader = newsMessageLoader;
        this.oldMessageLoader = oldMessageLoader;

        messageState.setFirstItemId(lastReadEntityId);
    }

    public void initQueue() {
        messageHandlingQueue = PublishSubject.create();
        messageHandlingQueueSubscription = messageHandlingQueue
                .onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .subscribe(message -> {
                    switch (message.getQueueType()) {
                        case Old:
                            loadOldMessage(message);
                            break;
                        case New:
                            loadNewMessage(message);
                            break;
                        case Send:
                            sendMessage(message);
                            break;
                        case UpdateLinkPreview:
                            updateLinkPreview(message);
                            break;
                    }
                }, throwable -> {
                    LogUtil.e("Message Publish Fail!!", throwable);
                }, () -> {

                });
    }

    public void getInitMessage() {
        if (roomId <= 0) {
            FormattedEntity entityById = EntityManager.getInstance().getEntityById(entityId);
            boolean topic = !entityById.isUser() && !(entityById instanceof BotEntity);

            if (topic) {
                roomId = entityId;
            } else if (NetworkCheckUtil.isConnected()) {

                long roomId = initRoomId();

                if (roomId > 0) {
                    this.roomId = roomId;
                }
            }
        }

        if (this.roomId <= 0) {
//            view.showFailToast(JandiApplication.getContext().getString(R.string.err_messages_invaild_entity));
            view.finish();
            return;
        }

        long savedLastLinkId = model.getLastReadLinkId(roomId, model.getMyId());
        messageState.setFirstItemId(savedLastLinkId);

        ResMessages.Link lastMessage = MessageRepository.getRepository().getLastMessage(roomId);

        // 1. 처음 접근 하는 토픽/DM 인 경우
        // 2. 오랜만에 접근 하는 토픽/DM 인 경우
        if (lastMessage == null
                || lastMessage.id < 0
                || (lastMessage.id > 0 && model.isBefore30Days(lastMessage.time))) {
            MessageRepository.getRepository().clearLinks(teamId, roomId);
            if (newsMessageLoader instanceof NormalNewMessageLoader) {
                NormalNewMessageLoader newsMessageLoader = (NormalNewMessageLoader) this.newsMessageLoader;
                newsMessageLoader.setHistoryLoad(false);
            }
//            presenter.setMoreNewFromAdapter(true);
//            presenter.setNewLoadingComplete();
        }

        if (roomId > 0) {
//            presenter.setMarkerInfo(teamId, roomId);
            model.updateMarkerInfo(teamId, roomId);
            model.setRoomId(roomId);
//            presenter.setLastReadLinkId(model.getLastReadLinkId(roomId, model.getMyId()));
        } else {
//            presenter.setLastReadLinkId(lastReadEntityId);
        }

        messageHandlingPush(new OldMessageQueue(messageState));

//        if (view.isForeground()) {
            messageHandlingPush(new NewMessageQueue(messageState));
//        }

        isRoomInit = true;
    }

    private void messageHandlingPush(MessageQueue messageQueue) {
        if (!messageHandlingQueueSubscription.isUnsubscribed()) {
            messageHandlingQueue.onNext(messageQueue);
        }
    }

    private void loadOldMessage(MessageQueue messageQueue) {
        if (oldMessageLoader != null) {
            ResMessages resMessages = oldMessageLoader.load(roomId, ((MessageState) messageQueue
                    .getData()).getFirstItemId());

            if (resMessages != null && roomId <= 0) {
                roomId = resMessages.entityId;
//                presenter.setMarkerInfo(teamId, roomId);
                model.updateMarkerInfo(teamId, roomId);
                model.setRoomId(roomId);
            }
        }
    }

    private void loadNewMessage(MessageQueue messageQueue) {
        if (newsMessageLoader != null) {
            MessageState data = (MessageState) messageQueue.getData();
            long lastUpdateLinkId = data.getLastUpdateLinkId();

            if (lastUpdateLinkId < 0 && oldMessageLoader != null) {
                oldMessageLoader.load(roomId, lastUpdateLinkId);
            }

            newsMessageLoader.load(roomId, lastUpdateLinkId);
        }
    }

    private void sendMessage(MessageQueue messageQueue) {
        SendingMessage data = (SendingMessage) messageQueue.getData();
        long linkId;
        List mentions = data.getMentions();

        if (data.getStickerInfo() != null) {
            linkId = model.sendStickerMessage(teamId, entityId, data.getStickerInfo(), data.getLocalId());
        } else {
            linkId = model.sendMessage(data.getLocalId(), data.getMessage(), mentions);
        }

        if (linkId > 0) {
            if (!JandiSocketManager.getInstance().isConnectingOrConnected()) {
                // 소켓이 안 붙어 있으면 임의로 갱신 요청
                EventBus.getDefault().post(new RefreshNewMessageEvent());
            }
        }
        view.notifyDataSetChanged();
    }

    private void updateLinkPreview(MessageQueue messageQueue) {
        int messageId = (Integer) messageQueue.getData();
        ResMessages.TextMessage textMessage = MessageRepository.getRepository().getTextMessage(messageId);
        updateLinkPreviewMessage(textMessage);
        view.notifyDataSetChanged();
    }

    public void updateLinkPreviewMessage(ResMessages.TextMessage message) {
        long messageId = message.id;
//        int index = presenter.getPosition(messageId);

//        if (index < 0) {
//            return;
//        }

//        ResMessages.Link link = presenter.getItem(index);
//        if (!(link.message instanceof ResMessages.TextMessage)) {
//            return;
//        }
//        link.message = message;
    }

    public void refreshNewMessages() {
        if (isRoomInit) {
            messageHandlingPush(new NewMessageQueue(messageState));
        }
    }

    public void refreshOldMessages() {
        if (!messageState.isFirstMessage()) {
            messageHandlingPush(new OldMessageQueue(messageState));
        }
    }

    public void refreshLinkPreview(int messageId) {
        messageHandlingPush(new UpdateLinkPreviewMessageQueue(messageId));
    }

    public void sendTextMessage(long localId, ReqSendMessageV3 reqSendMessage) {
        messageHandlingPush(new SendingMessageQueue(new SendingMessage(localId, reqSendMessage)));
    }

    public void sendStickerMessage(long localId, StickerInfo stickerInfo) {
        messageHandlingPush(new SendingMessageQueue
                (new SendingMessage(localId, "", new StickerInfo(stickerInfo), new ArrayList<>())));
    }

    public void messageHandlingQueueUnsubscribe() {
        messageHandlingQueueSubscription.unsubscribe();
    }

    long initRoomId() {
        try {
            ResMessages oldMessage = model.getOldMessage(-1, 1);
            return oldMessage.entityId;
        } catch (RetrofitError e) {
            e.printStackTrace();
        }
        return -1;
    }

}