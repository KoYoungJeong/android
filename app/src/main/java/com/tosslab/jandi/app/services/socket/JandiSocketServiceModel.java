package com.tosslab.jandi.app.services.socket;

import android.content.Intent;
import android.text.TextUtils;

import com.jakewharton.rxrelay.PublishRelay;
import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.MemberRankUpdatedEvent;
import com.tosslab.jandi.app.events.RefreshMentionBadgeCountEvent;
import com.tosslab.jandi.app.events.entities.ChatListRefreshEvent;
import com.tosslab.jandi.app.events.entities.MemberStarredEvent;
import com.tosslab.jandi.app.events.entities.ProfileChangeEvent;
import com.tosslab.jandi.app.events.entities.RefreshConnectBotEvent;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.events.entities.TopicDeleteEvent;
import com.tosslab.jandi.app.events.entities.TopicFolderRefreshEvent;
import com.tosslab.jandi.app.events.entities.TopicInfoUpdateEvent;
import com.tosslab.jandi.app.events.entities.TopicJoinEvent;
import com.tosslab.jandi.app.events.entities.TopicKickedoutEvent;
import com.tosslab.jandi.app.events.entities.TopicLeftEvent;
import com.tosslab.jandi.app.events.files.DeleteFileEvent;
import com.tosslab.jandi.app.events.files.FileCommentRefreshEvent;
import com.tosslab.jandi.app.events.files.FileCreatedEvent;
import com.tosslab.jandi.app.events.files.ShareFileEvent;
import com.tosslab.jandi.app.events.files.UnshareFileEvent;
import com.tosslab.jandi.app.events.messages.AnnouncementUpdatedEvent;
import com.tosslab.jandi.app.events.messages.LinkPreviewUpdateEvent;
import com.tosslab.jandi.app.events.messages.MentionMessageEvent;
import com.tosslab.jandi.app.events.messages.MessageStarEvent;
import com.tosslab.jandi.app.events.messages.RoomMarkerEvent;
import com.tosslab.jandi.app.events.messages.SocketPollEvent;
import com.tosslab.jandi.app.events.poll.RequestRefreshPollBadgeCountEvent;
import com.tosslab.jandi.app.events.team.MemberOnlineStatusChangeEvent;
import com.tosslab.jandi.app.events.team.TeamBadgeUpdateEvent;
import com.tosslab.jandi.app.events.team.TeamDeletedEvent;
import com.tosslab.jandi.app.events.team.TeamInfoChangeEvent;
import com.tosslab.jandi.app.events.team.TeamJoinEvent;
import com.tosslab.jandi.app.events.team.TeamLeaveEvent;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.PollRepository;
import com.tosslab.jandi.app.local.orm.repositories.SendMessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.BotRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.ChatRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.FolderRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.HumanRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialMentionInfoRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialPollInfoRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.RoomMarkerRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.TeamRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.TopicRepository;
import com.tosslab.jandi.app.local.orm.repositories.socket.SocketEventRepository;
import com.tosslab.jandi.app.network.client.direct.message.DirectMessageApi;
import com.tosslab.jandi.app.network.client.main.LoginApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.EventHistoryInfo;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResOnlineStatus;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.network.models.start.Chat;
import com.tosslab.jandi.app.network.models.start.Folder;
import com.tosslab.jandi.app.network.models.start.FolderItem;
import com.tosslab.jandi.app.network.models.start.Human;
import com.tosslab.jandi.app.network.models.start.Mention;
import com.tosslab.jandi.app.network.models.start.TeamUsage;
import com.tosslab.jandi.app.network.models.start.Topic;
import com.tosslab.jandi.app.network.socket.domain.SocketStart;
import com.tosslab.jandi.app.services.socket.model.SocketEventHistoryUpdator;
import com.tosslab.jandi.app.services.socket.model.SocketModelExtractor;
import com.tosslab.jandi.app.services.socket.to.MessageReadEvent;
import com.tosslab.jandi.app.services.socket.to.SocketAnnouncementCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketAnnouncementDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketAnnouncementUpdatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketChatCloseEvent;
import com.tosslab.jandi.app.services.socket.to.SocketChatCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketConnectBotCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketConnectBotDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketConnectBotUpdatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileCommentCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileCommentDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileCreated;
import com.tosslab.jandi.app.services.socket.to.SocketFileDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileShareEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileUnsharedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketLinkPreviewMessageEvent;
import com.tosslab.jandi.app.services.socket.to.SocketLinkPreviewThumbnailEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMemberOnlineStatusChangeEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMemberRankUpdatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMemberStarredEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMemberUnstarredEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMemberUpdatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMentionMarkerUpdatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageStarredEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageUnstarredEvent;
import com.tosslab.jandi.app.services.socket.to.SocketPollCommentCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketPollCommentDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketPollCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketPollDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketPollFinishedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketPollVotedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketRoomMarkerEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTeamCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTeamDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTeamInvitationCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTeamJoinEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTeamLeaveEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTeamPlanUpdatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTeamUpdatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTeamUsageUpdatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicFolderCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicFolderDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicFolderItemCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicFolderItemDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicFolderUpdatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicInvitedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicJoinedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicKickedoutEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicLeftEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicPushEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicStarredEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicUnstarredEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicUpdatedEvent;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.intro.IntroActivity;
import com.tosslab.jandi.app.ui.restart.RankResetActivity;
import com.tosslab.jandi.app.ui.restart.TeamPlanResetActivity;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.UserAgentUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.Lazy;
import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class JandiSocketServiceModel {
    public static final String TAG = JandiSocketServiceModel.class.getSimpleName();

    private final Lazy<LoginApi> loginApi;
    private final Lazy<DirectMessageApi> directMessageApi;
    PublishRelay<Object> eventPublisher;
    private PublishSubject<SocketRoomMarkerEvent> accountRefreshSubject;
    private Subscription accountRefreshSubscribe;
    private Subscription eventSubscribe;

    private SocketEventHistoryUpdator historyUpdator;

    @Inject
    JandiSocketServiceModel(Lazy<LoginApi> loginApi,
                            SocketEventHistoryUpdator historyUpdator,
                            Lazy<DirectMessageApi> directMessageApi) {
        this.loginApi = loginApi;
        this.historyUpdator = historyUpdator;
        this.directMessageApi = directMessageApi;
        historyUpdator.putAllEventActor(initEventActor());

        initEventPublisher();
    }

    private void initEventPublisher() {
        eventPublisher = PublishRelay.create();
        eventSubscribe = eventPublisher
                .onBackpressureBuffer()
                .subscribe(event -> {

                    try {
                        EventBus eventBus = EventBus.getDefault();
                        if (eventBus.hasSubscriberForEvent(event.getClass())) {
                            eventBus.post(event);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    public void stopEventPublisher() {
        if (eventSubscribe != null && !eventSubscribe.isUnsubscribed()) {
            eventSubscribe.unsubscribe();
        }
    }

    public void startMarkerObserver() {
        accountRefreshSubject = PublishSubject.create();
        accountRefreshSubscribe = accountRefreshSubject.onBackpressureBuffer()
                .throttleWithTimeout(500, TimeUnit.MILLISECONDS)
                .subscribe(event -> {

                    postEvent(new MessageReadEvent(event.getTeamId()));
                    postEvent(new RetrieveTopicListEvent());

                }, throwable -> LogUtil.d(throwable.getMessage()));
    }

    private Map<Class<? extends EventHistoryInfo>, Command> initEventActor() {
        Map<Class<? extends EventHistoryInfo>, Command> messageEventActorMapper
                = new HashMap<>();

        messageEventActorMapper.put(SocketMemberUpdatedEvent.class, this::onMemberUpdated);
        messageEventActorMapper.put(SocketTeamCreatedEvent.class, this::onTeamCreated);
        messageEventActorMapper.put(SocketTeamJoinEvent.class, this::onTeamJoin);
        messageEventActorMapper.put(SocketTeamLeaveEvent.class, this::onTeamLeft);
        messageEventActorMapper.put(SocketTeamDeletedEvent.class, this::onTeamDeleted);
        messageEventActorMapper.put(SocketTeamPlanUpdatedEvent.class, this::onTeamPlanUpdated);
        messageEventActorMapper.put(SocketTeamUsageUpdatedEvent.class, this::onTeamUsageUpdated);
        messageEventActorMapper.put(SocketChatCloseEvent.class, this::onChatClosed);
        messageEventActorMapper.put(SocketChatCreatedEvent.class, this::onChatCreated);
        messageEventActorMapper.put(SocketConnectBotCreatedEvent.class, this::onConnectBotCreated);
        messageEventActorMapper.put(SocketConnectBotDeletedEvent.class, this::onConnectBotDeleted);
        messageEventActorMapper.put(SocketConnectBotUpdatedEvent.class, this::onConnectBotUpdated);
        messageEventActorMapper.put(SocketTopicLeftEvent.class, this::onTopicLeft);
        messageEventActorMapper.put(SocketTopicDeletedEvent.class, this::onTopicDeleted);
        messageEventActorMapper.put(SocketTopicCreatedEvent.class, this::onTopicCreated);
        messageEventActorMapper.put(SocketTopicInvitedEvent.class, this::onTopicInvited);
        messageEventActorMapper.put(SocketTopicJoinedEvent.class, this::onTopicJoined);
        messageEventActorMapper.put(SocketTopicUpdatedEvent.class, this::onTopicUpdated);
        messageEventActorMapper.put(SocketTopicStarredEvent.class, this::onTopicStarred);
        messageEventActorMapper.put(SocketTopicUnstarredEvent.class, this::onTopicUnstarred);
        messageEventActorMapper.put(SocketTopicKickedoutEvent.class, this::onTopicKickOut);
        messageEventActorMapper.put(SocketMemberStarredEvent.class, this::onMemberStarred);
        messageEventActorMapper.put(SocketAnnouncementDeletedEvent.class, this::onAnnouncementDeleted);
        messageEventActorMapper.put(SocketAnnouncementUpdatedEvent.class, this::onAnnouncementStatusUpdated);
        messageEventActorMapper.put(SocketAnnouncementCreatedEvent.class, this::onAnnouncementCreated);
        messageEventActorMapper.put(SocketTopicPushEvent.class, this::onRoomSubscriptionUpdated);
        messageEventActorMapper.put(SocketTopicFolderCreatedEvent.class, this::onTopicFolderCreated);
        messageEventActorMapper.put(SocketTopicFolderUpdatedEvent.class, this::onTopicFolderUpdated);
        messageEventActorMapper.put(SocketTopicFolderDeletedEvent.class, this::onFolderDeleted);
        messageEventActorMapper.put(SocketTopicFolderItemCreatedEvent.class, this::onFolderItemCreated);
        messageEventActorMapper.put(SocketTopicFolderItemDeletedEvent.class, this::onFolderItemDeleted);
        messageEventActorMapper.put(SocketTeamUpdatedEvent.class, this::onTeamUpdated);

        // 메세지 관련 처리
        messageEventActorMapper.put(SocketFileCreated.class, this::onFileCreated);
        messageEventActorMapper.put(SocketFileDeletedEvent.class, this::onFileDeleted);
        messageEventActorMapper.put(SocketFileUnsharedEvent.class, this::onFileUnshared);
        messageEventActorMapper.put(SocketFileCommentDeletedEvent.class, this::onFileCommentDeleted);
        messageEventActorMapper.put(SocketMessageDeletedEvent.class, this::onMessageDeleted);
        messageEventActorMapper.put(SocketRoomMarkerEvent.class, this::onRoomMarkerUpdated);
        messageEventActorMapper.put(SocketMessageCreatedEvent.class, o -> onMessageCreated(o, true));
        messageEventActorMapper.put(SocketMessageStarredEvent.class, this::onMessageStarred);
        messageEventActorMapper.put(SocketMessageUnstarredEvent.class, this::onMessageUnstarred);
        messageEventActorMapper.put(SocketLinkPreviewMessageEvent.class, this::onLinkPreviewCreated);
        messageEventActorMapper.put(SocketLinkPreviewThumbnailEvent.class, this::onLinkPreviewImage);

        messageEventActorMapper.put(SocketPollCreatedEvent.class, this::onPollCreated);
        messageEventActorMapper.put(SocketPollDeletedEvent.class, this::onPollDeleted);
        messageEventActorMapper.put(SocketPollFinishedEvent.class, this::onPollFinished);
        messageEventActorMapper.put(SocketPollVotedEvent.class, this::onPollVoted);
        messageEventActorMapper.put(SocketMentionMarkerUpdatedEvent.class, this::onMentionMarkerUpdated);
        messageEventActorMapper.put(SocketMemberOnlineStatusChangeEvent.class, this::onMemberOnlineStatusChanged);

        return messageEventActorMapper;

    }

    public void onTeamCreated(Object object) {
        try {
            SocketTeamCreatedEvent event = SocketModelExtractor.getObjectWithoutCheckTeam(object, SocketTeamCreatedEvent.class);
            saveEvent(event);
            JandiPreference.setSocketConnectedLastTime(event.getTs());
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public SocketStart getStartInfo() {
        String token = TokenUtil.getAccessToken();
        return new SocketStart(token, UserAgentUtil.getDefaultUserAgent());
    }

    public void onFileDeleted(Object object) {
        try {
            SocketFileDeletedEvent event =
                    SocketModelExtractor.getObject(object, SocketFileDeletedEvent.class);
            saveEvent(event);

            JandiPreference.setSocketConnectedLastTime(event.getTs());
            MessageRepository.getRepository().updateStatus(event.getFile().getId(), "archived");
            ResMessages.FileMessage fileMessage = MessageRepository.getRepository()
                    .getFileMessage(event.getFile().getId());
            doAfterFileDeleted(fileMessage);
            postEvent(new DeleteFileEvent(event.getTeamId(), event.getFile().getId()));
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onFileCreated(Object object) {
        postEvent(new FileCreatedEvent());
    }

    private void saveEvent(EventHistoryInfo event) {
        SocketEventRepository.getInstance().addEvent(event);
    }

    public void onFileCommentCreated(Object object) {
        try {
            SocketFileCommentCreatedEvent socketFileEvent =
                    SocketModelExtractor.getObject(object, SocketFileCommentCreatedEvent.class);
            saveEvent(socketFileEvent);
            JandiPreference.setSocketConnectedLastTime(socketFileEvent.getTs());
            postEvent(
                    new FileCommentRefreshEvent(socketFileEvent.getEvent(),
                            socketFileEvent.getTeamId(),
                            socketFileEvent.getFile().getId(),
                            socketFileEvent.getComment().getId(),
                            TextUtils.equals(socketFileEvent.getEvent(), "file_comment_created")));
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onFileCommentDeleted(Object object) {
        try {
            SocketFileCommentDeletedEvent socketEvent =
                    SocketModelExtractor.getObject(object, SocketFileCommentDeletedEvent.class);
            saveEvent(socketEvent);
            JandiPreference.setSocketConnectedLastTime(socketEvent.getTs());


            long messageId = socketEvent.getComment().getId();
            MessageRepository.getRepository().deleteMessageOfMessageId(messageId);

            List<SocketFileCommentDeletedEvent.Room> rooms = socketEvent.getRooms();

            FileCommentRefreshEvent event = new FileCommentRefreshEvent(socketEvent.getEvent(),
                    socketEvent.getTeamId(),
                    socketEvent.getFile().getId(),
                    socketEvent.getComment().getId(),
                    false /* isAdded */);

            if (rooms != null && !rooms.isEmpty()) {
                List<Long> sharedRooms = new ArrayList<>();
                Observable.from(rooms)
                        .collect(() -> sharedRooms, (list, room) -> list.add(room.getId()))
                        .subscribe();
                event.setSharedRooms(sharedRooms);
            }

            postEvent(event);


        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onMessageDeleted(Object object) {
        try {
            SocketMessageDeletedEvent event =
                    SocketModelExtractor.getObject(object, SocketMessageDeletedEvent.class, true, false);

            if (event.getTeamId() != AccountRepository.getRepository().getSelectedTeamId()) {
                if (accountRefreshSubject != null && !accountRefreshSubscribe.isUnsubscribed()) {
                    accountRefreshSubject.onNext(new SocketRoomMarkerEvent());
                }
                return;
            }

            saveEvent(event);
            JandiPreference.setSocketConnectedLastTime(event.getTs());

            SocketMessageDeletedEvent.Data data = event.getData();
            long roomId = data.getRoomId();
            long linkId = data.getLinkId();
            long messageId = data.getMessageId();
            MessageRepository.getRepository().deleteMessageOfMessageId(messageId);

            if (ChatRepository.getInstance(event.getTeamId()).isChat(roomId)) {
                Chat chat = ChatRepository.getInstance(event.getTeamId()).getChat(roomId);
                if (chat.getReadLinkId() <= linkId) {
                    ChatRepository.getInstance(event.getTeamId()).updateUnreadCount(roomId, chat.getUnreadCount() - 1);
                    AccountRepository.getRepository().decreaseUnread(event.getTeamId());
                    postEvent(TeamBadgeUpdateEvent.fromLocal());
                }
                if (chat.getLastMessage() != null
                        && data.getLinkId() >= chat.getLastMessage().getId()) {
                    long teamId = event.getTeamId();
                    long userId = chat.getCompanionId();
                    try {
                        ResMessages resMessages = directMessageApi.get().getDirectMessages(teamId, userId, data.getLinkId(), 1);
                        if (resMessages != null
                                && resMessages.records != null
                                && !resMessages.records.isEmpty()) {
                            ResMessages.Link link = resMessages.records.get(0);
                            String contentText = getContentText(link.message);
                            ChatRepository.getInstance(event.getTeamId()).updateLastMessage(roomId, link.id, contentText, "created");
                        } else {
                            ChatRepository.getInstance(event.getTeamId()).updateLastMessage(roomId, linkId, "", "archived");
                        }
                    } catch (RetrofitException e) {
                        ChatRepository.getInstance(event.getTeamId()).updateLastMessage(roomId, linkId, "", "archived");
                        e.printStackTrace();
                    }
                }
            } else if (TopicRepository.getInstance(event.getTeamId()).isTopic(roomId)) {
                Topic topic = TopicRepository.getInstance(event.getTeamId()).getTopic(roomId);
                if (topic.getReadLinkId() <= linkId) {
                    TopicRepository.getInstance(event.getTeamId()).updateUnreadCount(roomId, topic.getUnreadCount() - 1);
                    AccountRepository.getRepository().decreaseUnread(event.getTeamId());
                    postEvent(TeamBadgeUpdateEvent.fromLocal());
                }
            } else {
                return;
            }

            postEvent(event);

        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onTopicUpdated(Object object) {
        try {
            SocketTopicUpdatedEvent event =
                    SocketModelExtractor.getObject(object, SocketTopicUpdatedEvent.class);
            saveEvent(event);
            Topic topic = event.getData().getTopic();
            TopicRepository.getInstance(event.getTeamId()).updateTopic(topic);
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            postEvent(new TopicInfoUpdateEvent(topic.getId()));
            postEvent(new RetrieveTopicListEvent());
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onChatClosed(Object object) {
        try {
            SocketChatCloseEvent event = SocketModelExtractor.getObject(object, SocketChatCloseEvent.class);
            saveEvent(event);
            SocketChatCloseEvent.Data data = event.getData();
            ChatRepository.getInstance(event.getTeamId()).updateChatOpened(data.getChatId(), false);
            JandiPreference.setSocketConnectedLastTime(event.getTs());

            postEvent(new ChatListRefreshEvent());
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onChatCreated(Object object) {
        try {
            SocketChatCreatedEvent event = SocketModelExtractor.getObject(object, SocketChatCreatedEvent.class);
            saveEvent(event);
            SocketChatCreatedEvent.Data data = event.getData();

            Chat chat = data.getChat();
            chat.setReadLinkId(-1);
            chat.setCompanionId(Observable.from(chat.getMembers())
                    .takeFirst(memberId -> memberId != TeamInfoLoader.getInstance(event.getTeamId()).getMyId())
                    .toBlocking().firstOrDefault(-1L));

            ChatRepository.getInstance(event.getTeamId()).addChat(chat);

            JandiPreference.setSocketConnectedLastTime(event.getTs());

            postEvent(new ChatListRefreshEvent());
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onTopicLeft(Object object) {
        try {
            SocketTopicLeftEvent event = SocketModelExtractor.getObject(object, SocketTopicLeftEvent.class);
            saveEvent(event);
            SocketTopicLeftEvent.Data data = event.getData();
            if (data.getMemberId() == TeamInfoLoader.getInstance().getMyId()) {
                TopicRepository.getInstance(event.getTeamId()).updateTopicJoin(data.getTopicId(), false);
                postEvent(new TopicLeftEvent(event.getTeamId(), event.getData().getTopicId(), true));
            } else {
                postEvent(new TopicLeftEvent(event.getTeamId(), event.getData().getTopicId(), false));
            }
            TopicRepository.getInstance(event.getTeamId()).removeMember(data.getTopicId(), data.getMemberId());
            RoomMarkerRepository.getInstance(event.getTeamId()).deleteMarker(data.getTopicId(), data.getMemberId());
            List<Folder> folders = FolderRepository.getInstance().getFolders();
            boolean find = false;
            for (Folder folder : folders) {
                List<Long> roomIds = folder.getRooms();
                for (Long roomId : roomIds) {
                    if (roomId == data.getTopicId()) {
                        roomIds.remove(roomId);
                        find = true;
                        break;
                    }
                }
                if (find) {
                    if (roomIds.size() == 0) {
                        folders.remove(folder);
                    }
                    break;
                }
            }
            JandiPreference.setSocketConnectedLastTime(event.getTs());

//            PollRepository.initiate().upsertPollStatus(data.getTopicId(), "deleted");

            postEvent(new RetrieveTopicListEvent());
            postEvent(new RequestRefreshPollBadgeCountEvent(event.getTeamId()));
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onMemberStarred(Object object) {
        try {
            SocketMemberStarredEvent event = SocketModelExtractor.getObject(object, SocketMemberStarredEvent.class);
            saveEvent(event);
            SocketMemberStarredEvent.Member member = event.getMember();
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            HumanRepository.getInstance(event.getTeamId()).updateStarred(member.getId(), true);
            postEvent(new MemberStarredEvent(member.getId()));
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onFileUnshared(Object object) {
        try {
            SocketFileUnsharedEvent event =
                    SocketModelExtractor.getObject(object, SocketFileUnsharedEvent.class);
            saveEvent(event);

            long fileId = event.getFile().getId();
            long roomId = event.room.id;

            MessageRepository.getRepository().deleteSharedRoom(fileId, roomId);
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            postEvent(new UnshareFileEvent(roomId, fileId));
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }


    public void onFileShared(Object object) {
        try {
            SocketFileShareEvent event =
                    SocketModelExtractor.getObject(object, SocketFileShareEvent.class);
            saveEvent(event);
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            long teamId = event.getTeamId();
            long fileId = event.getFile().getId();
            List<Integer> shareEntities = event.getFile().getShareEntities();

            postEvent(new ShareFileEvent(teamId, fileId, shareEntities));
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onRoomMarkerUpdated(Object object) {
        try {
            SocketRoomMarkerEvent event =
                    SocketModelExtractor.getObject(object, SocketRoomMarkerEvent.class, true, false);
            saveEvent(event);

            SocketRoomMarkerEvent.MarkerRoom room = event.getRoom();
            SocketRoomMarkerEvent.Marker marker = event.getMarker();

            long roomId = room.getId();
            long memberId = marker.getMemberId();
            long lastLinkId = marker.getLastLinkId();
            RoomMarkerRepository.getInstance(event.getTeamId()).upsertRoomMarker(roomId, memberId, lastLinkId);

            if (TeamInfoLoader.getInstance().getMyId() == memberId) {
                if (TeamInfoLoader.getInstance().isTopic(roomId)) {
                    TopicRepository.getInstance(event.getTeamId()).updateReadLinkId(roomId, lastLinkId);
                    TopicRepository.getInstance(event.getTeamId()).updateUnreadCount(roomId, 0);
                } else if (TeamInfoLoader.getInstance().isChat(roomId)) {
                    ChatRepository.getInstance(event.getTeamId()).updateReadLinkId(roomId, lastLinkId);
                    ChatRepository.getInstance(event.getTeamId()).updateUnreadCount(roomId, 0);
                }
            }

            JandiPreference.setSocketConnectedLastTime(event.getTs());
            postEvent(new RoomMarkerEvent(event.getRoom().getId()));

            long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
            final long markeredMemberId = memberId;
            // 내 마커가 갱신된 경우
            Observable.from(AccountRepository.getRepository().getAccountTeams())
                    .takeFirst(userTeam -> userTeam.getMemberId() == markeredMemberId)
                    .subscribe(it -> {
                        if (it.getTeamId() != selectedTeamId) {
                            if (accountRefreshSubject != null && !accountRefreshSubscribe.isUnsubscribed()) {
                                accountRefreshSubject.onNext(new SocketRoomMarkerEvent());
                            }
                        } else {
                            Observable.just(it)
                                    .concatMap(userTeam -> {
                                        return Observable.concat(
                                                Observable.from(TopicRepository.getInstance(event.getTeamId()).getJoinedTopics())
                                                        .map(Topic::getUnreadCount),
                                                Observable.from(ChatRepository.getInstance(event.getTeamId()).getOpenedChats())
                                                        .map(Chat::getUnreadCount));

                                    })
                                    .filter(count -> count > 0)
                                    .defaultIfEmpty(0)
                                    .reduce((integer, integer2) -> integer + integer2)
                                    .subscribe(count -> {
                                        AccountRepository.getRepository().updateUnread(selectedTeamId, count);
                                        postEvent(TeamBadgeUpdateEvent.fromLocal());
                                    }, Throwable::printStackTrace);
                        }
                    });

        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onLinkPreviewCreated(final Object object) {
        try {
            SocketLinkPreviewMessageEvent event =
                    SocketModelExtractor.getObject(object, SocketLinkPreviewMessageEvent.class);
            saveEvent(event);

            SocketLinkPreviewMessageEvent.Data data = event.getData();
            ResMessages.TextMessage textMessage = MessageRepository.getRepository().getTextMessage(data.getMessageId());
            if (textMessage != null) {
                textMessage.linkPreview = data.getLinkPreview();
                MessageRepository.getRepository().upsertTextMessage(textMessage);
            }

            JandiPreference.setSocketConnectedLastTime(event.getTs());

            postEvent(new LinkPreviewUpdateEvent(data.getMessageId()));
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onLinkPreviewImage(final Object object) {
        try {
            SocketLinkPreviewThumbnailEvent socketLinkPreviewMessageEvent =
                    SocketModelExtractor.getObject(object, SocketLinkPreviewThumbnailEvent.class);
            saveEvent(socketLinkPreviewMessageEvent);

            JandiPreference.setSocketConnectedLastTime(socketLinkPreviewMessageEvent.getTs());
            SocketLinkPreviewThumbnailEvent.Data data = socketLinkPreviewMessageEvent.getData();
            ResMessages.LinkPreview linkPreview = data.getLinkPreview();

            long messageId = data.getMessageId();

            ResMessages.TextMessage textMessage =
                    MessageRepository.getRepository().getTextMessage(messageId);
            textMessage.linkPreview = linkPreview;
            MessageRepository.getRepository().upsertTextMessage(textMessage);

            postEvent(new LinkPreviewUpdateEvent(messageId));
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }

    }

    public void onAnnouncementCreated(Object object) {
        // 공지사항 정보 추가

        try {
            SocketAnnouncementCreatedEvent event =
                    SocketModelExtractor.getObject(object, SocketAnnouncementCreatedEvent.class);
            saveEvent(event);

            SocketAnnouncementCreatedEvent.Data data = event.getData();
            long topicId = data.getTopicId();
            TopicRepository.getInstance(event.getTeamId()).createAnnounce(topicId, data.getAnnouncement());
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            postEvent(event);
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }

    }

    public void onAnnouncementDeleted(Object object) {
        try {

            // 공지사항 정보 갱신 로직
            SocketAnnouncementDeletedEvent event =
                    SocketModelExtractor.getObject(object, SocketAnnouncementDeletedEvent.class);
            saveEvent(event);

            TopicRepository.getInstance(event.getTeamId()).removeAnnounce(event.getData().getTopicId());
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            postEvent(event);
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onRoomSubscriptionUpdated(Object object) {
        try {

            // 푸시 정보 갱신 로직 추가
            SocketTopicPushEvent socketTopicPushEvent =
                    SocketModelExtractor.getObject(object, SocketTopicPushEvent.class);
            saveEvent(socketTopicPushEvent);

            SocketTopicPushEvent.Data data = socketTopicPushEvent.getData();
            long roomId = data.getRoomId();
            boolean subscribe = data.isSubscribe();
            JandiPreference.setSocketConnectedLastTime(socketTopicPushEvent.getTs());
            TopicRepository.getInstance(socketTopicPushEvent.getTeamId()).updatePushSubscribe(roomId, subscribe);

            postEvent(socketTopicPushEvent);
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    void postEvent(Object object) {
        if (eventPublisher != null && !eventSubscribe.isUnsubscribed()) {
            eventPublisher.call(object);
        }
    }

    public void stopMarkerObserver() {
        if (accountRefreshSubscribe != null && !accountRefreshSubscribe.isUnsubscribed()) {
            accountRefreshSubscribe.unsubscribe();
        }
    }

    public ResAccessToken refreshToken() throws RetrofitException {
        String jandiRefreshToken = TokenUtil.getRefreshToken();
        ReqAccessToken refreshReqToken = ReqAccessToken.createRefreshReqToken(jandiRefreshToken);
        return loginApi.get().getAccessToken(refreshReqToken);
    }

    public void onMessageUnstarred(Object object) {
        try {
            SocketMessageUnstarredEvent event
                    = SocketModelExtractor.getObject(object, SocketMessageUnstarredEvent.class);
            saveEvent(event);

            MessageRepository.getRepository().updateStarred(event.getStarredInfo()
                    .getMessageId(), false);

            JandiPreference.setSocketConnectedLastTime(event.getTs());
            postEvent(new MessageStarEvent(event.getStarredInfo().getMessageId(), false));

        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }


    public void onMessageStarred(Object object) {
        try {
            SocketMessageStarredEvent socketFileEvent
                    = SocketModelExtractor.getObject(object, SocketMessageStarredEvent.class);
            saveEvent(socketFileEvent);

            MessageRepository.getRepository().updateStarred(socketFileEvent.getStarredInfo()
                    .getMessageId(), true);

            JandiPreference.setSocketConnectedLastTime(socketFileEvent.getTs());
            postEvent(new MessageStarEvent(socketFileEvent.getStarredInfo().getMessageId(), true));

        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onFolderDeleted(Object object) {
        try {
            SocketTopicFolderDeletedEvent event
                    = SocketModelExtractor.getObject(object, SocketTopicFolderDeletedEvent.class);
            saveEvent(event);

            long folderId = event.getData().getFolderId();
            FolderRepository.getInstance(event.getTeamId()).deleteFolder(folderId);

            JandiPreference.setSocketConnectedLastTime(event.getTs());

            postEvent(new TopicFolderRefreshEvent());

        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onFolderItemCreated(Object object) {
        try {
            SocketTopicFolderItemCreatedEvent event
                    = SocketModelExtractor.getObject(object, SocketTopicFolderItemCreatedEvent.class);
            saveEvent(event);

            SocketTopicFolderItemCreatedEvent.Data data = event.getData();
            long folderId = data.getFolderId();
            long roomId = data.getRoomId();
            FolderRepository.getInstance(event.getTeamId()).removeTopicOfTeam(data.getTeamId(), Arrays.asList(roomId));
            FolderRepository.getInstance(event.getTeamId()).addTopic(folderId, roomId);

            JandiPreference.setSocketConnectedLastTime(event.getTs());

            postEvent(new TopicFolderRefreshEvent());

        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onFolderItemDeleted(Object object) {
        try {
            SocketTopicFolderItemDeletedEvent event
                    = SocketModelExtractor.getObject(object, SocketTopicFolderItemDeletedEvent.class);
            saveEvent(event);

            long folderId = event.getData().getFolderId();
            long roomId = event.getData().getRoomId();
            FolderRepository.getInstance(event.getTeamId()).removeTopic(folderId, roomId);

            JandiPreference.setSocketConnectedLastTime(event.getTs());

            postEvent(new TopicFolderRefreshEvent());

        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onTopicFolderCreated(Object object) {
        try {
            SocketTopicFolderCreatedEvent event
                    = SocketModelExtractor.getObject(object, SocketTopicFolderCreatedEvent.class);
            saveEvent(event);

            JandiPreference.setSocketConnectedLastTime(event.getTs());
            FolderRepository.getInstance(event.getTeamId()).removeTopicOfTeam(event.getTeamId(), event.getData().getFolder().getRooms());
            FolderRepository.getInstance(event.getTeamId()).addFolder(event.getData().getFolder());

            postEvent(new TopicFolderRefreshEvent());

        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());

        }
    }

    public void onTopicFolderUpdated(Object object) {
        try {
            SocketTopicFolderUpdatedEvent event
                    = SocketModelExtractor.getObject(object, SocketTopicFolderUpdatedEvent.class);
            saveEvent(event);

            JandiPreference.setSocketConnectedLastTime(event.getTs());
            Folder folder = event.getData().getFolder();
            FolderRepository.getInstance(event.getTeamId()).updateFolderName(folder.getId(), folder.getName());
            FolderRepository.getInstance(event.getTeamId()).updateFolderSeq(event.getTeamId(), folder.getId(), folder.getSeq());

            postEvent(new TopicFolderRefreshEvent());

        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }

    }

    public void onTeamLeft(Object object) {
        try {
            SocketTeamLeaveEvent event = SocketModelExtractor.getObjectWithoutCheckVersion(object, SocketTeamLeaveEvent.class);
            saveEvent(event);

            if (event.getVersion() == 2) {
                SocketTeamLeaveEvent.Data data = event.getData();
                long memberId = data.getMemberId();
                TeamLeaveEvent teamLeaveEvent = new TeamLeaveEvent(data.getTeamId(), memberId);

                long myId = TeamInfoLoader.getInstance().getMyId();

                if (memberId == myId) {
                    Observable.just(event)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(leaveEvent -> {
                                ResAccountInfo.UserTeam team = AccountRepository.getRepository().getSelectedTeamInfo();
                                if (team == null) {
                                    return;
                                }
                                String teamName = JandiApplication.getContext()
                                        .getString(R.string.jandi_your_access_disabled, team.getName());
                                ColoredToast.showError(teamName);
                                AccountRepository.getRepository().removeSelectedTeamInfo();
                                JandiApplication.getContext().startActivity(
                                        Henson.with(JandiApplication.getContext())
                                                .gotoTeamSelectListActivity()
                                                .shouldRefreshAccountInfo(true)
                                                .build()
                                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                                        | Intent.FLAG_ACTIVITY_CLEAR_TASK));

                                InitialInfoRepository.getInstance().removeInitialInfo(data.getTeamId());
                                JandiPreference.setSocketConnectedLastTime(-1);

                                PollRepository.getInstance().clear(data.getTeamId());
                                TeamInfoLoader instance = TeamInfoLoader.getInstance();
                                instance = null;
                            });
                } else {
                    HumanRepository.getInstance(event.getTeamId()).updateStatus(memberId, data.getMemberStatus());
                    JandiPreference.setSocketConnectedLastTime(event.getTs());
                    postEvent(teamLeaveEvent);
                }
            } else {
                SocketTeamLeaveEvent.Data data = event.getData();
                List<SocketTeamLeaveEvent.Member> members = data.getMembers();
                for (SocketTeamLeaveEvent.Member member : members) {
                    long memberId = member.getId();
                    TeamLeaveEvent teamLeaveEvent = new TeamLeaveEvent(data.getTeamId(), memberId);

                    long myId = TeamInfoLoader.getInstance().getMyId();

                    if (memberId == myId) {
                        Observable.just(event)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(leaveEvent -> {
                                    ResAccountInfo.UserTeam team = AccountRepository.getRepository().getSelectedTeamInfo();
                                    if (team == null) {
                                        return;
                                    }
                                    String teamName = JandiApplication.getContext()
                                            .getString(R.string.jandi_your_access_disabled, team.getName());
                                    ColoredToast.showError(teamName);
                                    AccountRepository.getRepository().removeSelectedTeamInfo();
                                    JandiApplication.getContext().startActivity(
                                            Henson.with(JandiApplication.getContext())
                                                    .gotoTeamSelectListActivity()
                                                    .shouldRefreshAccountInfo(true)
                                                    .build()
                                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                                            | Intent.FLAG_ACTIVITY_CLEAR_TASK));

                                    InitialInfoRepository.getInstance().removeInitialInfo(data.getTeamId());
                                    JandiPreference.setSocketConnectedLastTime(-1);

                                    PollRepository.getInstance().clear(data.getTeamId());
                                    TeamInfoLoader instance = TeamInfoLoader.getInstance();
                                    instance = null;
                                });
                    } else {
                        HumanRepository.getInstance(event.getTeamId()).updateStatus(memberId, member.getStatus());
                        JandiPreference.setSocketConnectedLastTime(event.getTs());
                        postEvent(teamLeaveEvent);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onTeamDeleted(Object object) {
        try {
            SocketTeamDeletedEvent event = SocketModelExtractor.getObjectWithoutCheckVersion(object, SocketTeamDeletedEvent.class);
            saveEvent(event);

            long teamId = -1;

            if (event.getVersion() == 2) {
                teamId = event.getData().getTeamId();
            } else {
                teamId = event.getData().getTeam().getId();
            }

            final long finalTeamId = teamId;

            long selectedTeamId = TeamInfoLoader.getInstance().getTeamId();

            if (teamId == selectedTeamId) {
                Observable.just(event)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(deletedEvent -> {
                            String deletedTeam = JandiApplication.getContext()
                                    .getString(R.string.jandi_deleted_team);
                            ColoredToast.showError(deletedTeam);

                            AccountRepository.getRepository().removeSelectedTeamInfo();
                            JandiApplication.getContext().startActivity(
                                    Henson.with(JandiApplication.getContext())
                                            .gotoTeamSelectListActivity()
                                            .shouldRefreshAccountInfo(true)
                                            .build().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

                            InitialInfoRepository.getInstance().removeInitialInfo(finalTeamId);
                            JandiPreference.setSocketConnectedLastTime(-1);

                            PollRepository.getInstance().clear(finalTeamId);

                            TeamInfoLoader instance = TeamInfoLoader.getInstance();
                            instance = null;
                        });
            } else {
                AccountRepository.getRepository().removeTeamInfo(teamId);
                JandiPreference.setSocketConnectedLastTime(event.getTs());
                postEvent(new TeamDeletedEvent(teamId));
            }
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onTopicKickOut(Object object) {
        try {
            SocketTopicKickedoutEvent event =
                    SocketModelExtractor.getObject(object, SocketTopicKickedoutEvent.class);
            saveEvent(event);

            SocketTopicKickedoutEvent.Data data = event.getData();

            TopicRepository.getInstance(event.getTeamId()).updateTopicJoin(data.getRoomId(), false);
            TopicRepository.getInstance(event.getTeamId()).removeMember(data.getRoomId(), TeamInfoLoader.getInstance().getMyId());
            RoomMarkerRepository.getInstance(event.getTeamId()).deleteMarker(data.getRoomId(), TeamInfoLoader.getInstance().getMyId());
            JandiPreference.setSocketConnectedLastTime(event.getTs());

            postEvent(new TopicKickedoutEvent(data.getRoomId(), data.getTeamId()));

        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onMessageCreated(Object object, boolean fromEventHistory) {
        try {
            SocketMessageCreatedEvent event = SocketModelExtractor.getObject(object, SocketMessageCreatedEvent.class, true, false);
            saveEvent(event);

            ResMessages.Link link = event.getData().getLinkMessage();
            if (event.getTeamId() == TeamInfoLoader.getInstance().getTeamId()) {
                JandiPreference.setSocketConnectedLastTime(event.getTs());

                if (link.fromEntity == TeamInfoLoader.getInstance().getMyId()) {
                    SendMessageRepository.getRepository().deleteCompletedMessages(Arrays.asList(link.id));
                }
                final long tempLinkId = link.id;
                Observable.from(link.toEntity)
                        .distinct()
                        .subscribe(roomId -> {
                            MessageRepository.getRepository().updateDirty(roomId, tempLinkId);
                        });

                doAfterMessageCreated(link);

                postEvent(event);

                if (!(fromEventHistory)) {
                    handleMessageIfMentionToMe(link);
                }
            }

            if (link.message != null) {
                if (event.getData().getLinkMessage().fromEntity != TeamInfoLoader.getInstance().getMyId()) {
                    long teamId = event.getData().getLinkMessage().teamId;
                    AccountRepository.getRepository().increaseUnread(teamId);
                    postEvent(TeamBadgeUpdateEvent.fromLocal());
                }
            }

        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    private void handleMessageIfMentionToMe(ResMessages.Link link) {
        Observable.just(link)
                .filter(link1 -> {
                    if (link1.message instanceof ResMessages.TextMessage) {
                        Collection<MentionObject> mentions = ((ResMessages.TextMessage) link1.message).mentions;
                        return mentions != null && !mentions.isEmpty();
                    } else if (link1.message instanceof ResMessages.CommentMessage) {
                        Collection<MentionObject> mentions = ((ResMessages.CommentMessage) link1.message).mentions;
                        return mentions != null && !mentions.isEmpty();
                    } else {
                        return false;
                    }
                })
                .filter(link1 -> {
                    Collection<MentionObject> mentions;
                    if (link1.message instanceof ResMessages.TextMessage) {
                        mentions = ((ResMessages.TextMessage) link1.message).mentions;
                    } else {
                        mentions = ((ResMessages.CommentMessage) link1.message).mentions;
                    }
                    return Observable.from(mentions)
                            .takeFirst(mentionObject ->
                                    "room".equals(mentionObject.getType())
                                            || mentionObject.getId() == TeamInfoLoader.getInstance().getMyId())
                            .map(it -> true)
                            .toBlocking().firstOrDefault(false);
                })
                .subscribe(link1 -> {

                    if (EventBus.getDefault().hasSubscriberForEvent(MentionMessageEvent.class)) {
                        EventBus.getDefault().post(new MentionMessageEvent(link1));
                    } else {
                        InitialMentionInfoRepository.getInstance(link.teamId).increaseUnreadCount();
                    }

                }, Throwable::printStackTrace);
    }

    private void doAfterMessageCreated(ResMessages.Link linkMessage) {
        if (linkMessage.message == null) {
            // 시스템 메세지인 경우..
            return;
        }
        List<Long> toEntity = linkMessage.toEntity;
        for (int i = 0, toEntitySize = toEntity.size(); i < toEntitySize; i++) {
            long roomId = toEntity.get(i);

            boolean isTopic = TeamInfoLoader.getInstance().isTopic(roomId);
            boolean isMyMessage = TeamInfoLoader.getInstance().getMyId() == linkMessage.message.writerId;
            if (isTopic) {
                TopicRepository.getInstance(linkMessage.teamId).updateLastLinkId(roomId, linkMessage.id);
                if (!isMyMessage) {
                    TopicRepository.getInstance(linkMessage.teamId).incrementUnreadCount(roomId);
                }
            } else if (TeamInfoLoader.getInstance().isChat(roomId)) {

                ResMessages.OriginalMessage message = linkMessage.message;
                String text = "";
                text = getContentText(message);

                ChatRepository.getInstance(linkMessage.teamId).updateLastMessage(roomId, linkMessage.messageId, text, "created");
                ChatRepository.getInstance(linkMessage.teamId).updateLastLinkId(roomId, linkMessage.id);
                ChatRepository.getInstance(linkMessage.teamId).updateChatOpened(roomId, true);

                if (!isMyMessage) {
                    ChatRepository.getInstance(linkMessage.teamId).incrementUnreadCount(roomId);
                }
            }
        }
    }

    // 특정 룸의 마지막 메세지이면 룸 리스트 정보를 갱신하기 위해 호출 create->archived
    private void doAfterFileDeleted(ResMessages.FileMessage fileMessage) {

        ChatRepository chatRepository = ChatRepository.getInstance(fileMessage.teamId);

        for (ResMessages.OriginalMessage.IntegerWrapper e : fileMessage.shareEntities) {
            long roomId = e.getShareEntity();
            ResMessages.Link lastLink = MessageRepository.getRepository().getLastMessage(roomId);
            if (TeamInfoLoader.getInstance().isChat(roomId)) {
                if (lastLink.message.id == fileMessage.id) {
                    chatRepository.updateLastMessage(roomId, lastLink.messageId, fileMessage.content.title, "archived");
                }
            }
        }

    }

    private String getContentText(ResMessages.OriginalMessage message) {
        String text;
        if (message instanceof ResMessages.TextMessage) {
            text = ((ResMessages.TextMessage) message).content.body;
        } else if (message instanceof ResMessages.CommentMessage) {
            text = ((ResMessages.CommentMessage) message).content.body;
        } else if (message instanceof ResMessages.FileMessage) {
            text = ((ResMessages.FileMessage) message).content.title;
        } else if (message instanceof ResMessages.StickerMessage
                || message instanceof ResMessages.CommentStickerMessage) {
            text = "(sticker)";
        } else {
            text = "";
        }
        return text;
    }

    public void onConnectBotCreated(Object object) {
        try {
            SocketConnectBotCreatedEvent event = SocketModelExtractor.getObject(object, SocketConnectBotCreatedEvent.class);
            saveEvent(event);

            BotRepository.getInstance(event.getTeamId()).addBot(event.getData().getBot());
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            postEvent(new RefreshConnectBotEvent());
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onConnectBotDeleted(Object object) {
        try {
            SocketConnectBotDeletedEvent event = SocketModelExtractor.getObject(object, SocketConnectBotDeletedEvent.class);
            saveEvent(event);

            BotRepository.getInstance(event.getTeamId()).updateBotStatus(event.getData().getBotId(), "deleted");
            JandiPreference.setSocketConnectedLastTime(event.getTs());

            postEvent(new RefreshConnectBotEvent());
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onConnectBotUpdated(Object object) {
        try {
            SocketConnectBotUpdatedEvent event = SocketModelExtractor.getObject(object, SocketConnectBotUpdatedEvent.class);
            saveEvent(event);

            BotRepository.getInstance(event.getTeamId()).updateBot(event.getData().getBot());
            JandiPreference.setSocketConnectedLastTime(event.getTs());

            postEvent(new RefreshConnectBotEvent());
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onTeamJoin(Object object) {
        try {
            SocketTeamJoinEvent event = SocketModelExtractor.getObjectWithoutCheckVersion(object, SocketTeamJoinEvent.class);
            saveEvent(event);

            if (event.getVersion() == 2) {
                SocketTeamJoinEvent.Data data = event.getData();
                Human member = data.getMember();
                HumanRepository.getInstance(event.getTeamId()).addHuman(member);
            } else {
                SocketTeamJoinEvent.Data data = event.getData();
                List<Human> members = data.getMembers();
                for (Human member : members) {
                    HumanRepository.getInstance(event.getTeamId()).addHuman(member);
                }
            }

            JandiPreference.setSocketConnectedLastTime(event.getTs());
            postEvent(new RetrieveTopicListEvent());
            postEvent(new TeamJoinEvent());
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onTopicCreated(Object object) {
        try {
            SocketTopicCreatedEvent event = SocketModelExtractor.getObject(object, SocketTopicCreatedEvent.class);
            saveEvent(event);

            Topic topic = event.getData().getTopic();
            FolderItem folderItem = event.getData().getFolderItem();

            if (topic.getCreatorId() == TeamInfoLoader.getInstance().getMyId()) {
                topic.setSubscribe(true);
                topic.setIsJoined(true);
            }
            TopicRepository.getInstance(event.getTeamId()).addTopic(topic);

            if (folderItem != null) {
                FolderRepository.getInstance(event.getTeamId())
                        .addTopic(folderItem.getFolderId(), topic.getId());
            }

            JandiPreference.setSocketConnectedLastTime(event.getTs());

            postEvent(new RetrieveTopicListEvent());
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onTopicJoined(Object object) {
        try {
            SocketTopicJoinedEvent event = SocketModelExtractor.getObjectWithoutCheckVersion(object, SocketTopicJoinedEvent.class);
            saveEvent(event);

            if (event.getVersion() == 2) {
                SocketTopicJoinedEvent.Data data = event.getData();
                long memberId = data.getMemberId();
                List<Long> memberList = new ArrayList<>();
                memberList.add(memberId);
                TopicRepository.getInstance(event.getTeamId()).addMember(data.getTopicId(), memberList);
                RoomMarkerRepository.getInstance(event.getTeamId()).upsertRoomMarker(data.getTopicId(), memberId, -1);
                if (TeamInfoLoader.getInstance().getMyId() == memberId) {
                    TopicRepository.getInstance(event.getTeamId()).updateTopicJoin(data.getTopicId(), true);
                    TopicRepository.getInstance(event.getTeamId()).updatePushSubscribe(data.getTopicId(), true);
                    TopicRepository.getInstance(event.getTeamId()).updateReadLinkId(data.getTopicId(), -1);

                }
            } else {
                SocketTopicJoinedEvent.Data data = event.getData();
                TopicRepository.getInstance(event.getTeamId()).addMember(data.getTopicId(), data.getMemberIds());
                for (long memberId : data.getMemberIds()) {
                    RoomMarkerRepository.getInstance(event.getTeamId()).upsertRoomMarker(data.getTopicId(), memberId, -1);
                    if (TeamInfoLoader.getInstance().getMyId() == memberId) {
                        TopicRepository.getInstance(event.getTeamId()).updateTopicJoin(data.getTopicId(), true);
                        TopicRepository.getInstance(event.getTeamId()).updatePushSubscribe(data.getTopicId(), true);
                        TopicRepository.getInstance(event.getTeamId()).updateReadLinkId(data.getTopicId(), -1);
                    }
                }
            }

            JandiPreference.setSocketConnectedLastTime(event.getTs());

            postEvent(new TopicJoinEvent(event.getTeamId(), event.getData().getTopicId()));
            postEvent(new RetrieveTopicListEvent());
            return;
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }

    }

    public void onTopicInvited(Object object) {
        try {
            SocketTopicInvitedEvent event = SocketModelExtractor.getObject(object, SocketTopicInvitedEvent.class);
            saveEvent(event);

            SocketTopicInvitedEvent.Data data = event.getData();
            Topic topic = data.getTopic();
            long topicId = topic.getId();
            long myId = TeamInfoLoader.getInstance().getMyId();

            RoomMarkerRepository.getInstance(event.getTeamId()).upsertRoomMarker(topicId, myId, -1);

            TopicRepository.getInstance(event.getTeamId()).deleteTopic(topicId);

            topic.setSubscribe(true);
            topic.setIsJoined(true);
            topic.setReadLinkId(-1);

            TopicRepository.getInstance(event.getTeamId()).addTopic(topic);

            JandiPreference.setSocketConnectedLastTime(event.getTs());
            postEvent(new RetrieveTopicListEvent());
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onMemberUpdated(Object object) {
        try {
            SocketMemberUpdatedEvent event = SocketModelExtractor.getObject(object, SocketMemberUpdatedEvent.class);
            saveEvent(event);

            SocketMemberUpdatedEvent.Data data = event.getData();
            Human member = data.getMember();
            HumanRepository.getInstance(event.getTeamId()).updateHuman(member);
            JandiPreference.setSocketConnectedLastTime(event.getTs());

            postEvent(new ProfileChangeEvent(data.getMember()));
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onTopicDeleted(Object object) {
        try {
            SocketTopicDeletedEvent event = SocketModelExtractor.getObject(object, SocketTopicDeletedEvent.class);
            saveEvent(event);

            long topicId = event.getData().getTopicId();

            TopicRepository.getInstance(event.getTeamId()).deleteTopic(topicId);
            List<Folder> folders = FolderRepository.getInstance().getFolders();
            boolean find = false;
            for (Folder folder : folders) {
                List<Long> roomIds = folder.getRooms();
                for (Long roomId : roomIds) {
                    if (roomId == topicId) {
                        roomIds.remove(roomId);
                        find = true;
                        break;
                    }
                }
                if (find) {
                    if (roomIds.size() == 0) {
                        folders.remove(folder);
                    }
                    break;
                }
            }

            RoomMarkerRepository.getInstance(event.getTeamId()).deleteMarkers(topicId);
            JandiPreference.setSocketConnectedLastTime(event.getTs());

            postEvent(new TopicDeleteEvent(event.getTeamId(), topicId));
            postEvent(new RetrieveTopicListEvent());
            postEvent(new RequestRefreshPollBadgeCountEvent(event.getTeamId()));
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onTopicStarred(Object object) {
        try {
            SocketTopicStarredEvent event = SocketModelExtractor.getObject(object, SocketTopicStarredEvent.class);
            saveEvent(event);

            SocketTopicStarredEvent.Topic topic = event.getTopic();
            long id = topic.getId();
            TopicRepository.getInstance(event.getTeamId()).updateStarred(id, true);
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            postEvent(new TopicInfoUpdateEvent(id));
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onTopicUnstarred(Object object) {
        try {
            SocketTopicUnstarredEvent event = SocketModelExtractor.getObject(object, SocketTopicUnstarredEvent.class);
            saveEvent(event);

            SocketTopicUnstarredEvent.Topic topic = event.getTopic();
            long id = topic.getId();
            TopicRepository.getInstance(event.getTeamId()).updateStarred(id, false);
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            postEvent(new TopicInfoUpdateEvent(id));
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }

    }

    public void onMemberUnstarred(Object object) {
        try {
            SocketMemberUnstarredEvent event = SocketModelExtractor.getObject(object, SocketMemberUnstarredEvent.class);
            saveEvent(event);

            SocketMemberUnstarredEvent.Member member = event.getMember();
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            HumanRepository.getInstance(event.getTeamId()).updateStarred(member.getId(), false);
            postEvent(new MemberStarredEvent(member.getId()));
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }

    }

    public void onAnnouncementStatusUpdated(Object object) {
        try {
            SocketAnnouncementUpdatedEvent event = SocketModelExtractor.getObject(object, SocketAnnouncementUpdatedEvent.class);
            saveEvent(event);

            SocketAnnouncementUpdatedEvent.Data data = event.getData();
            boolean opened = data.isOpened();
            TopicRepository.getInstance(event.getTeamId()).updateAnnounceOpened(data.getTopicId(), opened);
            JandiPreference.setSocketConnectedLastTime(event.getTs());

            postEvent(new AnnouncementUpdatedEvent(data.getTopicId(), opened));

        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }

    }

    public void onTeamUpdated(Object object) {
        try {
            SocketTeamUpdatedEvent event = SocketModelExtractor.getObjectWithoutCheckTeam(object, SocketTeamUpdatedEvent.class);
            saveEvent(event);

            if (event.getData().getTeam().getId() == TeamInfoLoader.getInstance().getTeamId()) {
                TeamRepository.getInstance(event.getTeamId()).updateTeam(event.getData().getTeam());
                JandiPreference.setSocketConnectedLastTime(event.getTs());
            }
            postEvent(new TeamInfoChangeEvent());
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void updateEventHistory() {
        historyUpdator.updateEventHistory(this::postEvent, o -> {
            if (accountRefreshSubject != null && !accountRefreshSubscribe.isUnsubscribed()) {
                accountRefreshSubject.onNext(new SocketRoomMarkerEvent());
            }
        }, () -> {
            MessageRepository.getRepository().deleteAllLink();
            JandiPreference.setSocketConnectedLastTime(-1);
            IntroActivity.startActivitySkipAnimation(JandiApplication.getContext(), false);
        });
    }

    public void onPollCreated(Object object) {
        try {
            SocketPollCreatedEvent event = SocketModelExtractor.getObject(object, SocketPollCreatedEvent.class);
            saveEvent(event);

            SocketPollCreatedEvent.Data data = event.getData();

            Poll poll = data != null ? data.getPoll() : null;

            if (poll != null && poll.getId() > 0) {
                upsertPoll(poll);
                poll = getPollFromDatabase(poll.getId());
            }

            JandiPreference.setSocketConnectedLastTime(event.getTs());

            if (poll != null && poll.getId() > 0) {
                InitialPollInfoRepository.getInstance(event.getTeamId()).increaseVotableCount();
                postEvent(new SocketPollEvent(poll, SocketPollEvent.Type.CREATED));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPollFinished(Object object) {
        try {
            SocketPollFinishedEvent event = SocketModelExtractor.getObject(object, SocketPollFinishedEvent.class);
            saveEvent(event);

            SocketPollFinishedEvent.Data data = event.getData();

            ResMessages.Link link = data.getLinkMessage();
            Poll poll = link != null ? link.poll : null;
            if (poll != null && poll.getId() > 0) {
                upsertPoll(poll);
                poll = getPollFromDatabase(poll.getId());
            }

            JandiPreference.setSocketConnectedLastTime(event.getTs());

            if (poll != null && poll.getId() > 0) {
                InitialPollInfoRepository.getInstance(event.getTeamId()).decreaseVotableCount();
                postEvent(new SocketPollEvent(poll, SocketPollEvent.Type.FINISHED));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPollDeleted(Object object) {
        try {
            SocketPollDeletedEvent event = SocketModelExtractor.getObject(object, SocketPollDeletedEvent.class);
            saveEvent(event);

            SocketPollDeletedEvent.Data data = event.getData();

            ResMessages.Link link = data.getLinkMessage();
            Poll poll = link != null ? link.poll : null;
            if (poll != null && poll.getId() > 0) {
                upsertPoll(poll);
                poll = getPollFromDatabase(poll.getId());
            }

            JandiPreference.setSocketConnectedLastTime(event.getTs());

            if (poll != null && poll.getId() > 0) {
                InitialPollInfoRepository.getInstance(event.getTeamId()).decreaseVotableCount();
                postEvent(new SocketPollEvent(poll, SocketPollEvent.Type.DELETED));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPollVoted(Object object) {
        try {
            SocketPollVotedEvent event = SocketModelExtractor.getObject(object, SocketPollVotedEvent.class);
            saveEvent(event);

            SocketPollVotedEvent.Data data = event.getData();

            ResMessages.Link link = data.getLinkMessage();
            Poll poll = link != null ? link.poll : null;
            if (poll != null && poll.getId() > 0 && poll.isMine()) {
                upsertPollVotedStatus(poll);
                poll = getPollFromDatabase(poll.getId());
                poll.setIsMine(true);
                InitialPollInfoRepository.getInstance(event.getTeamId()).decreaseVotableCount();
            }

            JandiPreference.setSocketConnectedLastTime(event.getTs());

            postEvent(new SocketPollEvent(poll, SocketPollEvent.Type.VOTED));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPollCommentCreated(Object object) {
        try {
            SocketPollCommentCreatedEvent event = SocketModelExtractor.getObject(object, SocketPollCommentCreatedEvent.class);
            saveEvent(event);

            JandiPreference.setSocketConnectedLastTime(event.getTs());
            postEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPollCommentDeleted(Object object) {
        try {
            SocketPollCommentDeletedEvent event = SocketModelExtractor.getObject(object, SocketPollCommentDeletedEvent.class);
            saveEvent(event);

            JandiPreference.setSocketConnectedLastTime(event.getTs());
            postEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void upsertPoll(Poll poll) {
        PollRepository.getInstance().upsertPoll(poll);
    }

    private void upsertPollVotedStatus(Poll poll) {
        PollRepository.getInstance().upsertPollVoteStatus(poll);
    }

    private Poll getPollFromDatabase(long pollId) {
        return PollRepository.getInstance().getPollById(pollId);
    }

    public void onMentionMarkerUpdated(Object object) {
        try {
            SocketMentionMarkerUpdatedEvent event = SocketModelExtractor.getObject(object, SocketMentionMarkerUpdatedEvent.class);
            saveEvent(event);

            Mention mention = InitialMentionInfoRepository.getInstance(event.getTeamId()).getMention();
            long lastMentionedMessageId = event.getData().getLastMentionedMessageId();
            mention.setLastMentionedMessageId(lastMentionedMessageId);
            mention.setUnreadCount(0);
            InitialMentionInfoRepository.getInstance(event.getTeamId()).upsertMention(mention);
            TeamInfoLoader.getInstance().refreshMention();

            postEvent(new RefreshMentionBadgeCountEvent());

            JandiPreference.setSocketConnectedLastTime(event.getTs());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onMemberRankUpdated(Object object) {
        try {
            SocketMemberRankUpdatedEvent event = SocketModelExtractor.getObject(object, SocketMemberRankUpdatedEvent.class);
            saveEvent(event);

            SocketMemberRankUpdatedEvent.Data data = event.getData();
            List<Long> memberIds = data.getMemberIds();
            if (memberIds.contains(TeamInfoLoader.getInstance().getMyId())) {
                Intent intent = new Intent(JandiApplication.getContext(), RankResetActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                JandiApplication.getContext().startActivity(intent);
            } else {
                long rankId = data.getRankId();
                for (Long memberId : memberIds) {
                    HumanRepository.getInstance(event.getTeamId()).updateRank(memberId, rankId);
                    TeamInfoLoader.getInstance().updateUser(memberId);
                }
                postEvent(new MemberRankUpdatedEvent());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onTeamPlanUpdated(Object object) {
        try {
            SocketTeamPlanUpdatedEvent event = SocketModelExtractor.getObject(object, SocketTeamPlanUpdatedEvent.class);
            saveEvent(event);
            Intent intent = new Intent(JandiApplication.getContext(), TeamPlanResetActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            JandiApplication.getContext().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void onTeamUsageUpdated(Object object) {
        try {
            SocketTeamUsageUpdatedEvent event = SocketModelExtractor.getObject(object, SocketTeamUsageUpdatedEvent.class);
            saveEvent(event);
            TeamUsage data = event.getData();
            TeamInfoLoader.getInstance().updateTeamUsage(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onInvitationCreated(Object object) {
        try {
            SocketTeamInvitationCreatedEvent event =
                    SocketModelExtractor.getObjectWithoutCheckTeam(object, SocketTeamInvitationCreatedEvent.class);
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            postEvent(new TeamJoinEvent());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onMemberOnlineStatusChanged(Object object) {
        try {
            SocketMemberOnlineStatusChangeEvent event =
                    SocketModelExtractor.getObjectWithoutCheckTeam(object, SocketMemberOnlineStatusChangeEvent.class);
            ResOnlineStatus.Record data = event.getData();

            if (TeamInfoLoader.getInstance().getMember(data.getMemberId()) != null) {
                if (data.getPresence().equals("online")) {
                    TeamInfoLoader.getInstance().getOnlineStatus().setOnlineMember(data.getMemberId());
                } else {
                    TeamInfoLoader.getInstance().getOnlineStatus().setOfflineMember(data.getMemberId());
                }
                EventBus.getDefault().post(new MemberOnlineStatusChangeEvent(data.getMemberId(), data.getPresence()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public interface Command {
        void command(EventHistoryInfo t);
    }

}