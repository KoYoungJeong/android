package com.tosslab.jandi.app.services.socket;

import android.content.Context;
import android.text.TextUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.ChatListRefreshEvent;
import com.tosslab.jandi.app.events.entities.MemberStarredEvent;
import com.tosslab.jandi.app.events.entities.ProfileChangeEvent;
import com.tosslab.jandi.app.events.entities.RefreshConnectBotEvent;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.events.entities.TopicDeleteEvent;
import com.tosslab.jandi.app.events.entities.TopicFolderRefreshEvent;
import com.tosslab.jandi.app.events.entities.TopicInfoUpdateEvent;
import com.tosslab.jandi.app.events.entities.TopicKickedoutEvent;
import com.tosslab.jandi.app.events.files.DeleteFileEvent;
import com.tosslab.jandi.app.events.files.FileCommentRefreshEvent;
import com.tosslab.jandi.app.events.files.ShareFileEvent;
import com.tosslab.jandi.app.events.files.UnshareFileEvent;
import com.tosslab.jandi.app.events.messages.AnnouncementUpdatedEvent;
import com.tosslab.jandi.app.events.messages.LinkPreviewUpdateEvent;
import com.tosslab.jandi.app.events.messages.MessageStarEvent;
import com.tosslab.jandi.app.events.messages.RoomMarkerEvent;
import com.tosslab.jandi.app.events.messages.SocketPollEvent;
import com.tosslab.jandi.app.events.poll.RequestRefreshPollBadgeCountEvent;
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
import com.tosslab.jandi.app.local.orm.repositories.info.RoomMarkerRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.TeamRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.TopicRepository;
import com.tosslab.jandi.app.local.orm.repositories.socket.SocketEventRepository;
import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.direct.message.DirectMessageApi;
import com.tosslab.jandi.app.network.client.events.EventsApi;
import com.tosslab.jandi.app.network.client.main.LoginApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.json.JacksonMapper;
import com.tosslab.jandi.app.network.models.EventHistoryInfo;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.network.models.start.Chat;
import com.tosslab.jandi.app.network.models.start.Folder;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.network.models.start.Marker;
import com.tosslab.jandi.app.network.models.start.Topic;
import com.tosslab.jandi.app.network.socket.domain.SocketStart;
import com.tosslab.jandi.app.services.socket.model.SocketEventHistoryUpdator;
import com.tosslab.jandi.app.services.socket.model.SocketEventVersionModel;
import com.tosslab.jandi.app.services.socket.to.MessageOfOtherTeamEvent;
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
import com.tosslab.jandi.app.services.socket.to.SocketFileDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileShareEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileUnsharedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketLinkPreviewMessageEvent;
import com.tosslab.jandi.app.services.socket.to.SocketLinkPreviewThumbnailEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMemberStarredEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMemberUnstarredEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMemberUpdatedEvent;
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
import com.tosslab.jandi.app.services.socket.to.SocketTeamDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTeamDomainUpdatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTeamJoinEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTeamLeaveEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTeamNameUpdatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTeamUpdatedEvent;
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
import com.tosslab.jandi.app.ui.account.AccountHomeActivity;
import com.tosslab.jandi.app.ui.intro.IntroActivity;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.UserAgentUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
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

    private final Context context;
    private final ObjectMapper objectMapper;
    private final Lazy<LoginApi> loginApi;
    private final Lazy<EventsApi> eventsApi;
    private final Lazy<PollApi> pollApi;
    private final Lazy<DirectMessageApi> directMessageApi;
    PublishSubject<Object> eventPublisher;
    private PublishSubject<SocketRoomMarkerEvent> accountRefreshSubject;
    private Subscription accountRefreshSubscribe;
    private Subscription eventSubscribe;

    private SocketEventHistoryUpdator historyUpdator;

    @Inject
    JandiSocketServiceModel(Context context,
                            Lazy<LoginApi> loginApi,
                            SocketEventHistoryUpdator historyUpdator,
                            Lazy<DirectMessageApi> directMessageApi) {
        this.context = context;
        this.loginApi = loginApi;
        this.historyUpdator = historyUpdator;
        this.directMessageApi = directMessageApi;
        this.objectMapper = JacksonMapper.getInstance().getObjectMapper();
        historyUpdator.putAllEventActor(initEventActor());

        initEventPublisher();
    }

    private void initEventPublisher() {
        eventPublisher = PublishSubject.create();
        eventSubscribe = eventPublisher
                .onBackpressureBuffer()
                .buffer(700, TimeUnit.MILLISECONDS)
                .filter(objects1 -> objects1 != null && objects1.size() > 0)
                .map(LinkedHashSet::new)
                .subscribe(objects -> {

                    try {
                        TeamInfoLoader.getInstance().refresh();
                        EventBus eventBus = EventBus.getDefault();
                        for (Object object : objects) {
                            if (eventBus.hasSubscriberForEvent(object.getClass())) {
                                eventBus.post(object);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    public void stopEventPublisher() {
        eventPublisher.onCompleted();
        if (eventSubscribe != null && !eventSubscribe.isUnsubscribed()) {
            eventSubscribe.unsubscribe();
        }
    }

    public void startMarkerObserver() {
        accountRefreshSubject = PublishSubject.create();
        accountRefreshSubscribe = accountRefreshSubject.onBackpressureBuffer()
                .throttleWithTimeout(500, TimeUnit.MILLISECONDS)
                .subscribe(event -> {

                    postEvent(new MessageOfOtherTeamEvent());
                    postEvent(new RetrieveTopicListEvent());

                }, throwable -> LogUtil.d(throwable.getMessage()));
    }

    private Map<Class<? extends EventHistoryInfo>, Command> initEventActor() {
        Map<Class<? extends EventHistoryInfo>, Command> messageEventActorMapper
                = new HashMap<>();

        messageEventActorMapper.put(SocketMemberUpdatedEvent.class, this::onMemberUpdated);
        messageEventActorMapper.put(SocketTeamJoinEvent.class, this::onTeamJoin);
        messageEventActorMapper.put(SocketTeamLeaveEvent.class, this::onTeamLeft);
        messageEventActorMapper.put(SocketTeamDeletedEvent.class, this::onTeamDeleted);
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
        messageEventActorMapper.put(SocketTeamNameUpdatedEvent.class, this::onTeamNameUpdated);
        messageEventActorMapper.put(SocketTeamDomainUpdatedEvent.class, this::onTeamDomainUpdated);
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
        messageEventActorMapper.put(SocketFileDeletedEvent.class, this::onFileDeleted);
        messageEventActorMapper.put(SocketFileUnsharedEvent.class, this::onFileUnshared);
        messageEventActorMapper.put(SocketFileCommentDeletedEvent.class, this::onFileCommentDeleted);
        messageEventActorMapper.put(SocketMessageDeletedEvent.class, this::onMessageDeleted);
        messageEventActorMapper.put(SocketRoomMarkerEvent.class, this::onRoomMarkerUpdated);
        messageEventActorMapper.put(SocketMessageCreatedEvent.class, this::onMessageCreated);
        messageEventActorMapper.put(SocketMessageStarredEvent.class, this::onMessageStarred);
        messageEventActorMapper.put(SocketMessageUnstarredEvent.class, this::onMessageUnstarred);
        messageEventActorMapper.put(SocketLinkPreviewMessageEvent.class, this::onLinkPreviewCreated);
        messageEventActorMapper.put(SocketLinkPreviewThumbnailEvent.class, this::onLinkPreviewImage);

        messageEventActorMapper.put(SocketPollCreatedEvent.class, this::onPollCreated);
        messageEventActorMapper.put(SocketPollDeletedEvent.class, this::onPollDeleted);
        messageEventActorMapper.put(SocketPollFinishedEvent.class, this::onPollFinished);
        messageEventActorMapper.put(SocketPollVotedEvent.class, this::onPollVoted);

        return messageEventActorMapper;

    }

    public SocketStart getStartInfo() {
        String token = TokenUtil.getAccessToken();
        return new SocketStart(token, UserAgentUtil.getDefaultUserAgent());
    }

    @Deprecated
    public void onTeamNameUpdated(Object object) {
        // do nothing
//        try {
//            SocketTeamNameUpdatedEvent event = getObject(object, SocketTeamNameUpdatedEvent.class);
//            SocketTeamNameUpdatedEvent.Team team = event.getTeam();
//            long teamId = team.getTopicId();
//            String name = team.getName();
//
//            AccountRepository.getRepository().updateTeamName(teamId, name);
//            TeamRepository.getInstance().updateTeamName(teamId, name);
//            JandiPreference.setSocketConnectedLastTime(event.getTs());
//
//            postEvent(new TeamInfoChangeEvent());
//        } catch (Exception e) {
//            LogUtil.d(TAG, e.getMessage());
//        }


    }

    public void onFileDeleted(Object object) {
        try {
            SocketFileDeletedEvent event =
                    getObject(object, SocketFileDeletedEvent.class);
            saveEvent(event);

            JandiPreference.setSocketConnectedLastTime(event.getTs());
            MessageRepository.getRepository().updateStatus(event.getFile().getId(), "archived");
            postEvent(new DeleteFileEvent(event.getTeamId(), event.getFile().getId()));
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    private void saveEvent(EventHistoryInfo event) {
        SocketEventRepository.getInstance().addEvent(event);
    }

    public void onFileCommentCreated(Object object) {
        try {
            SocketFileCommentCreatedEvent socketFileEvent =
                    getObject(object, SocketFileCommentCreatedEvent.class);
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
                    getObject(object, SocketFileCommentDeletedEvent.class);
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
                    getObject(object, SocketMessageDeletedEvent.class);
            saveEvent(event);
            JandiPreference.setSocketConnectedLastTime(event.getTs());

            SocketMessageDeletedEvent.Data data = event.getData();
            long roomId = data.getRoomId();
            long linkId = data.getLinkId();
            long messageId = data.getMessageId();
            MessageRepository.getRepository().deleteMessageOfMessageId(messageId);

            if (ChatRepository.getInstance().isChat(roomId)) {
                Chat chat = ChatRepository.getInstance().getChat(roomId);
                if (chat.getReadLinkId() <= linkId) {
                    ChatRepository.getInstance().updateUnreadCount(roomId, chat.getUnreadCount() - 1);
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
                            ChatRepository.getInstance().updateLastMessage(roomId, link.id, contentText, "created");
                        } else {
                            ChatRepository.getInstance().updateLastMessage(roomId, linkId, "", "archived");
                        }
                    } catch (RetrofitException e) {
                        ChatRepository.getInstance().updateLastMessage(roomId, linkId, "", "archived");
                        e.printStackTrace();
                    }
                }
            } else if (TopicRepository.getInstance().isTopic(roomId)) {
                Topic topic = TopicRepository.getInstance().getTopic(roomId);
                if (topic.getReadLinkId() <= linkId) {
                    TopicRepository.getInstance().updateUnreadCount(roomId, topic.getUnreadCount() - 1);
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
                    getObject(object, SocketTopicUpdatedEvent.class);
            saveEvent(event);
            TopicRepository.getInstance().updateTopic(event.getData().getTopic());
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            postEvent(new TopicInfoUpdateEvent(event.getData().getTopic().getId()));
            postEvent(new RetrieveTopicListEvent());
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onChatClosed(Object object) {
        try {
            SocketChatCloseEvent event = getObject(object, SocketChatCloseEvent.class);
            saveEvent(event);
            SocketChatCloseEvent.Data chat = event.getChat();
            ChatRepository.getInstance().updateChatOpened(chat.getId(), false);
            JandiPreference.setSocketConnectedLastTime(event.getTs());

            postEvent(new ChatListRefreshEvent());
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onChatCreated(Object object) {
        try {
            SocketChatCreatedEvent event = getObject(object, SocketChatCreatedEvent.class);
            saveEvent(event);
            SocketChatCreatedEvent.Data data = event.getData();

            Chat chat = data.getChat();
            InitialInfo initialInfo = new InitialInfo();
            initialInfo.setTeamId(chat.getTeamId());
            chat.setInitialInfo(initialInfo);
            Collection<Marker> markers = chat.getMarkers();
            if (markers == null) {
                markers = new ArrayList<>();
                chat.setMarkers(markers);
            }

            if (markers.isEmpty()) {
                markers = new ArrayList<>();

                for (Long memberId : chat.getMembers()) {
                    Marker marker = new Marker();
                    marker.setChat(chat);
                    marker.setReadLinkId(-1);
                    marker.setMemberId(memberId);
                    markers.add(marker);
                }
            } else {
                for (Marker marker : chat.getMarkers()) {
                    marker.setChat(chat);
                }
            }

            chat.setReadLinkId(-1);
            chat.setCompanionId(Observable.from(chat.getMembers())
                    .takeFirst(memberId -> memberId != TeamInfoLoader.getInstance().getMyId())
                    .toBlocking().firstOrDefault(-1L));

            ChatRepository.getInstance().addChat(chat);

            JandiPreference.setSocketConnectedLastTime(event.getTs());

            postEvent(new ChatListRefreshEvent());
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onTopicLeft(Object object) {
        try {
            SocketTopicLeftEvent event = getObject(object, SocketTopicLeftEvent.class);
            saveEvent(event);
            SocketTopicLeftEvent.Data data = event.getData();
            if (data.getMemberId() == TeamInfoLoader.getInstance().getMyId()) {
                TopicRepository.getInstance().updateTopicJoin(data.getTopicId(), false);
                postEvent(new TopicDeleteEvent(event.getTeamId(), data.getTopicId()));
            }
            TopicRepository.getInstance().removeMember(data.getTopicId(), data.getMemberId());
            RoomMarkerRepository.getInstance().deleteMarker(data.getTopicId(), data.getMemberId());
            JandiPreference.setSocketConnectedLastTime(event.getTs());

            PollRepository.getInstance().upsertPollStatus(data.getTopicId(), "deleted");

            postEvent(new RetrieveTopicListEvent());
            postEvent(new RequestRefreshPollBadgeCountEvent(event.getTeamId()));
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onMemberStarred(Object object) {
        try {
            SocketMemberStarredEvent event = getObject(object, SocketMemberStarredEvent.class);
            saveEvent(event);
            SocketMemberStarredEvent.Member member = event.getMember();
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            HumanRepository.getInstance().updateStarred(member.getId(), true);
            postEvent(new MemberStarredEvent(member.getId()));
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onFileUnshared(Object object) {
        try {
            SocketFileUnsharedEvent event =
                    getObject(object, SocketFileUnsharedEvent.class);
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
                    getObject(object, SocketFileShareEvent.class);
            saveEvent(event);
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            long teamId = event.getTeamId();
            long fileId = event.getFile().getId();
            postEvent(new ShareFileEvent(teamId, fileId));
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onRoomMarkerUpdated(Object object) {
        try {
            SocketRoomMarkerEvent event =
                    getObject(object, SocketRoomMarkerEvent.class, true, false);
            saveEvent(event);

            SocketRoomMarkerEvent.MarkerRoom room = event.getRoom();
            SocketRoomMarkerEvent.Marker marker = event.getMarker();

            long roomId = room.getId();
            long memberId = marker.getMemberId();
            long lastLinkId = marker.getLastLinkId();
            RoomMarkerRepository.getInstance().upsertRoomMarker(roomId, memberId, lastLinkId);

            if (TeamInfoLoader.getInstance().getMyId() == memberId) {
                if (TeamInfoLoader.getInstance().isTopic(roomId)) {
                    TopicRepository.getInstance().updateReadId(roomId, lastLinkId);
                    TopicRepository.getInstance().updateUnreadCount(roomId, 0);
                } else if (TeamInfoLoader.getInstance().isChat(roomId)) {
                    ChatRepository.getInstance().updateReadLinkId(roomId, lastLinkId);
                    ChatRepository.getInstance().updateUnreadCount(roomId, 0);
                }
            }

            JandiPreference.setSocketConnectedLastTime(event.getTs());

            postEvent(new RoomMarkerEvent(event.getRoom().getId()));
            final long markeredMemberId = memberId;
            // 내 마커가 갱신된 경우 뱃지 갱신하도록 함
            Observable.from(AccountRepository.getRepository().getAccountTeams())
                    .takeFirst(userTeam -> userTeam.getMemberId() == markeredMemberId)
                    .subscribe(it -> {
                        if (accountRefreshSubject != null && !accountRefreshSubscribe.isUnsubscribed()) {
                            accountRefreshSubject.onNext(new SocketRoomMarkerEvent());
                        }
                    });
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onLinkPreviewCreated(final Object object) {
        try {
            SocketLinkPreviewMessageEvent event =
                    getObject(object, SocketLinkPreviewMessageEvent.class);
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
                    getObject(object, SocketLinkPreviewThumbnailEvent.class);
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
                    getObject(object, SocketAnnouncementCreatedEvent.class);
            saveEvent(event);

            SocketAnnouncementCreatedEvent.Data data = event.getData();
            long topicId = data.getTopicId();
            TopicRepository.getInstance().createAnnounce(topicId, data.getAnnouncement());
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
                    getObject(object, SocketAnnouncementDeletedEvent.class);
            saveEvent(event);

            TopicRepository.getInstance().removeAnnounce(event.getData().getTopicId());
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
                    getObject(object, SocketTopicPushEvent.class);
            saveEvent(socketTopicPushEvent);

            SocketTopicPushEvent.Data data = socketTopicPushEvent.getData();
            long roomId = data.getRoomId();
            boolean subscribe = data.isSubscribe();
            JandiPreference.setSocketConnectedLastTime(socketTopicPushEvent.getTs());
            TopicRepository.getInstance().updatePushSubscribe(roomId, subscribe);

            postEvent(socketTopicPushEvent);
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    void postEvent(Object object) {
        if (eventPublisher != null && !eventSubscribe.isUnsubscribed()) {
            eventPublisher.onNext(object);
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
                    = getObject(object, SocketMessageUnstarredEvent.class);
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
                    = getObject(object, SocketMessageStarredEvent.class);
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
                    = getObject(object, SocketTopicFolderDeletedEvent.class);
            saveEvent(event);

            long folderId = event.getData().getFolderId();
            FolderRepository.getInstance().deleteFolder(folderId);

            JandiPreference.setSocketConnectedLastTime(event.getTs());

            postEvent(new TopicFolderRefreshEvent());

        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onFolderItemCreated(Object object) {
        try {
            SocketTopicFolderItemCreatedEvent event
                    = getObject(object, SocketTopicFolderItemCreatedEvent.class);
            saveEvent(event);

            SocketTopicFolderItemCreatedEvent.Data data = event.getData();
            long folderId = data.getFolderId();
            long roomId = data.getRoomId();
            FolderRepository.getInstance().removeTopicOfTeam(data.getTeamId(), Arrays.asList(roomId));
            FolderRepository.getInstance().addTopic(folderId, roomId);

            JandiPreference.setSocketConnectedLastTime(event.getTs());

            postEvent(new TopicFolderRefreshEvent());

        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onFolderItemDeleted(Object object) {
        try {
            SocketTopicFolderItemDeletedEvent event
                    = getObject(object, SocketTopicFolderItemDeletedEvent.class);
            saveEvent(event);

            long folderId = event.getData().getFolderId();
            long roomId = event.getData().getRoomId();
            FolderRepository.getInstance().removeTopic(folderId, roomId);

            JandiPreference.setSocketConnectedLastTime(event.getTs());

            postEvent(new TopicFolderRefreshEvent());

        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onTopicFolderCreated(Object object) {
        try {
            SocketTopicFolderCreatedEvent event
                    = getObject(object, SocketTopicFolderCreatedEvent.class);
            saveEvent(event);

            JandiPreference.setSocketConnectedLastTime(event.getTs());
            FolderRepository.getInstance().removeTopicOfTeam(event.getTeamId(), event.getData().getFolder().getRooms());
            FolderRepository.getInstance().addFolder(event.getTeamId(), event.getData().getFolder());

            postEvent(new TopicFolderRefreshEvent());

        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());

        }
    }

    public void onTopicFolderUpdated(Object object) {
        try {
            SocketTopicFolderUpdatedEvent event
                    = getObject(object, SocketTopicFolderUpdatedEvent.class);
            saveEvent(event);

            JandiPreference.setSocketConnectedLastTime(event.getTs());
            Folder folder = event.getData().getFolder();
            FolderRepository.getInstance().updateFolderName(folder.getId(), folder.getName());
            FolderRepository.getInstance().updateFolderSeq(event.getTeamId(), folder.getId(), folder.getSeq());

            postEvent(new TopicFolderRefreshEvent());

        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }

    }

    public void onTeamLeft(Object object) {
        try {
            SocketTeamLeaveEvent event = getObject(object, SocketTeamLeaveEvent.class);
            saveEvent(event);

            SocketTeamLeaveEvent.Data data = event.getData();
            long memberId = data.getMemberId();
            TeamLeaveEvent teamLeaveEvent = new TeamLeaveEvent(data.getTeamId(), memberId);

            long myId = TeamInfoLoader.getInstance().getMyId();

            if (memberId == myId) {
                Observable.just(event)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(leaveEvent -> {
                            ResAccountInfo.UserTeam team = AccountRepository.getRepository().getSelectedTeamInfo();
                            String teamName = JandiApplication.getContext()
                                    .getString(R.string.jandi_your_access_disabled, team.getName());
                            ColoredToast.showError(teamName);
                            AccountRepository.getRepository().removeSelectedTeamInfo();
                            AccountHomeActivity.startActivity(JandiApplication.getContext(), true);

                            InitialInfoRepository.getInstance().removeInitialInfo(data.getTeamId());
                            JandiPreference.setSocketConnectedLastTime(-1);

                            PollRepository.getInstance().clear(data.getTeamId());
                            TeamInfoLoader instance = TeamInfoLoader.getInstance();
                            instance = null;
                        });
            } else {

                HumanRepository.getInstance().updateStatus(memberId, "disabled");
                JandiPreference.setSocketConnectedLastTime(event.getTs());

                postEvent(teamLeaveEvent);
            }


        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onTeamDeleted(Object object) {
        try {
            SocketTeamDeletedEvent event = getObject(object, SocketTeamDeletedEvent.class, true, false);
            saveEvent(event);

            long teamId = event.getData().getTeamId();

            long selectedTeamId = TeamInfoLoader.getInstance().getTeamId();

            if (teamId == selectedTeamId) {
                Observable.just(event)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(deletedEvent -> {
                            String deletedTeam = JandiApplication.getContext()
                                    .getString(R.string.jandi_deleted_team);
                            ColoredToast.showError(deletedTeam);

                            AccountRepository.getRepository().removeSelectedTeamInfo();
                            AccountHomeActivity.startActivity(JandiApplication.getContext(), true);

                            InitialInfoRepository.getInstance().removeInitialInfo(teamId);
                            JandiPreference.setSocketConnectedLastTime(-1);

                            PollRepository.getInstance().clear(teamId);

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
                    getObject(object, SocketTopicKickedoutEvent.class);
            saveEvent(event);

            SocketTopicKickedoutEvent.Data data = event.getData();

            TopicRepository.getInstance().updateTopicJoin(data.getRoomId(), false);
            TopicRepository.getInstance().removeMember(data.getRoomId(), TeamInfoLoader.getInstance().getMyId());
            RoomMarkerRepository.getInstance().deleteMarker(data.getRoomId(), TeamInfoLoader.getInstance().getMyId());
            JandiPreference.setSocketConnectedLastTime(event.getTs());

            PollRepository.getInstance().upsertPollStatus(data.getRoomId(), "deleted");

            postEvent(new TopicKickedoutEvent(data.getRoomId(), data.getTeamId()));
            postEvent(new RetrieveTopicListEvent());
            postEvent(new RequestRefreshPollBadgeCountEvent(event.getTeamId()));

        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onMessageCreated(Object object) {
        try {
            SocketMessageCreatedEvent event = getObject(object, SocketMessageCreatedEvent.class, true, false);
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
            }

            if (link.message != null) {
                final long markeredMemberId = link.message.writerId;
                // 내가 아닌 사람이 메세지를 쓴 경우 뱃지가 갱신되도록 함
                Observable.from(AccountRepository.getRepository().getAccountTeams())
                        .takeFirst(userTeam -> userTeam.getMemberId() != markeredMemberId)
                        .subscribe(it -> {
                            if (accountRefreshSubject != null && !accountRefreshSubscribe.isUnsubscribed()) {
                                accountRefreshSubject.onNext(new SocketRoomMarkerEvent());
                            }
                        });
            }

        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
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
                TopicRepository.getInstance().updateLastLinkId(roomId, linkMessage.id);
                if (!isMyMessage) {
                    TopicRepository.getInstance().incrementUnreadCount(roomId);
                }
            } else if (TeamInfoLoader.getInstance().isChat(roomId)) {

                ResMessages.OriginalMessage message = linkMessage.message;
                String text = "";
                text = getContentText(message);

                ChatRepository.getInstance().updateLastMessage(roomId, linkMessage.messageId, text, "created");
                ChatRepository.getInstance().updateLastLinkId(roomId, linkMessage.id);
                ChatRepository.getInstance().updateChatOpened(roomId, true);

                if (!isMyMessage) {
                    ChatRepository.getInstance().incrementUnreadCount(roomId);
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

    private <T extends EventHistoryInfo> T getObject(Object object, Class<T> clazz) throws Exception {
        return getObject(object, clazz, true, true);
    }

    private <T extends EventHistoryInfo> T getObject(Object object, Class<T> clazz, boolean checkVersion, boolean checkTeamId) throws Exception {
        T t;
        if (object.getClass() != clazz) {
            t = objectMapper.readValue(object.toString(), clazz);
        } else {
            t = (T) object;
        }
        if (checkVersion) {
            throwExceptionIfInvaildVersion(t);
        }

        if (checkTeamId) {
            throwExceptionIfInvaildTeamId(t);
        }
        return t;
    }

    private <T> void throwExceptionIfInvaildTeamId(T t) throws Exception {
        if (t instanceof EventHistoryInfo) {
            long teamId = ((EventHistoryInfo) t).getTeamId();
            if (teamId != 0
                    && teamId != AccountRepository.getRepository().getSelectedTeamId()) {
                throw new Exception("Ignore Team : " + t.getClass().getName());
            }
        }
    }

    <T extends EventHistoryInfo> void throwExceptionIfInvaildVersion(T object) throws Exception {
        if (!SocketEventVersionModel.validVersion(object)) {
            throw new Exception("Invalid Version : " + object.getClass().getName());
        }
    }

    public void onConnectBotCreated(Object object) {
        try {
            SocketConnectBotCreatedEvent event = getObject(object, SocketConnectBotCreatedEvent.class);
            saveEvent(event);

            BotRepository.getInstance().addBot(event.getData().getBot());
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            postEvent(new RefreshConnectBotEvent());
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onConnectBotDeleted(Object object) {
        try {
            SocketConnectBotDeletedEvent event = getObject(object, SocketConnectBotDeletedEvent.class);
            saveEvent(event);

            BotRepository.getInstance().updateBotStatus(event.getData().getBotId(), "deleted");
            JandiPreference.setSocketConnectedLastTime(event.getTs());

            postEvent(new RefreshConnectBotEvent());
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onConnectBotUpdated(Object object) {
        try {
            SocketConnectBotUpdatedEvent event = getObject(object, SocketConnectBotUpdatedEvent.class);
            saveEvent(event);

            BotRepository.getInstance().updateBot(event.getData().getBot());
            JandiPreference.setSocketConnectedLastTime(event.getTs());

            postEvent(new RefreshConnectBotEvent());
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onTeamJoin(Object object) {
        try {
            SocketTeamJoinEvent event = getObject(object, SocketTeamJoinEvent.class);
            saveEvent(event);

            SocketTeamJoinEvent.Data data = event.getData();
            HumanRepository.getInstance().addHuman(data.getTeamId(), data.getMember());
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            postEvent(new RetrieveTopicListEvent());
            postEvent(new TeamJoinEvent());
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onTopicCreated(Object object) {
        try {
            SocketTopicCreatedEvent event = getObject(object, SocketTopicCreatedEvent.class);
            saveEvent(event);

            Topic topic = event.getData().getTopic();

            List<Marker> markers = new ArrayList<>();
            for (Long memberId : topic.getMembers()) {
                Marker marker = new Marker();
                marker.setTopic(topic);
                marker.setMemberId(memberId);
                marker.setReadLinkId(-1);
                markers.add(marker);
            }
            if (topic.getCreatorId() == TeamInfoLoader.getInstance().getMyId()) {
                topic.setSubscribe(true);
                topic.setIsJoined(true);
            }
            topic.setMarkers(markers);
            TopicRepository.getInstance().addTopic(topic);
            JandiPreference.setSocketConnectedLastTime(event.getTs());

            postEvent(new RetrieveTopicListEvent());
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onTopicJoined(Object object) {
        try {
            SocketTopicJoinedEvent event = getObject(object, SocketTopicJoinedEvent.class);
            saveEvent(event);

            SocketTopicJoinedEvent.Data data = event.getData();
            TopicRepository.getInstance().addMember(data.getTopicId(), Arrays.asList(data.getMemberId()));
            RoomMarkerRepository.getInstance().upsertRoomMarker(data.getTopicId(), data.getMemberId(), -1);
            if (TeamInfoLoader.getInstance().getMyId() == data.getMemberId()) {
                TopicRepository.getInstance().updateTopicJoin(data.getTopicId(), true);
                TopicRepository.getInstance().updatePushSubscribe(data.getTopicId(), true);
                TopicRepository.getInstance().updateReadId(data.getTopicId(), -1);
            }

            JandiPreference.setSocketConnectedLastTime(event.getTs());

            postEvent(new RetrieveTopicListEvent());

        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onTopicInvited(Object object) {
        try {
            SocketTopicInvitedEvent event = getObject(object, SocketTopicInvitedEvent.class);
            saveEvent(event);

            SocketTopicInvitedEvent.Data data = event.getData();
            Topic topic = data.getTopic();
            long topicId = topic.getId();
            long myId = TeamInfoLoader.getInstance().getMyId();

            RoomMarkerRepository.getInstance().upsertRoomMarker(topicId, myId, -1);

            TopicRepository.getInstance().deleteTopic(topicId);

            if (topic.getMarkers() == null) {
                ArrayList<Marker> markers = new ArrayList<>();
                topic.setMarkers(markers);
                for (Long memberId : topic.getMembers()) {
                    Marker marker = new Marker();
                    marker.setTopic(topic);
                    marker.setMemberId(memberId);
                    marker.setReadLinkId(-1);

                    markers.add(marker);
                }
            }


            topic.setSubscribe(true);
            topic.setIsJoined(true);
            topic.setReadLinkId(-1);
            topic.setLastLinkId(-1);

            TopicRepository.getInstance().addTopic(topic);

            JandiPreference.setSocketConnectedLastTime(event.getTs());

            postEvent(new RetrieveTopicListEvent());
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onMemberUpdated(Object object) {
        try {
            SocketMemberUpdatedEvent event = getObject(object, SocketMemberUpdatedEvent.class);
            saveEvent(event);

            SocketMemberUpdatedEvent.Data data = event.getData();
            HumanRepository.getInstance().updateHuman(data.getMember());
            JandiPreference.setSocketConnectedLastTime(event.getTs());

            postEvent(new ProfileChangeEvent(data.getMember()));
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onTopicDeleted(Object object) {
        try {
            SocketTopicDeletedEvent event = getObject(object, SocketTopicDeletedEvent.class);
            saveEvent(event);

            long topicId = event.getData().getTopicId();

            TopicRepository.getInstance().deleteTopic(topicId);
            RoomMarkerRepository.getInstance().deleteMarkers(topicId);
            JandiPreference.setSocketConnectedLastTime(event.getTs());

            PollRepository.getInstance().upsertPollStatus(topicId, "deleted");

            postEvent(new TopicDeleteEvent(event.getTeamId(), topicId));
            postEvent(new RetrieveTopicListEvent());
            postEvent(new RequestRefreshPollBadgeCountEvent(event.getTeamId()));
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onTopicStarred(Object object) {
        try {
            SocketTopicStarredEvent event = getObject(object, SocketTopicStarredEvent.class);
            saveEvent(event);

            SocketTopicStarredEvent.Topic topic = event.getTopic();
            long id = topic.getId();
            TopicRepository.getInstance().updateStarred(id, true);
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            postEvent(new TopicInfoUpdateEvent(id));
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public void onTopicUnstarred(Object object) {
        try {
            SocketTopicUnstarredEvent event = getObject(object, SocketTopicUnstarredEvent.class);
            saveEvent(event);

            SocketTopicUnstarredEvent.Topic topic = event.getTopic();
            long id = topic.getId();
            TopicRepository.getInstance().updateStarred(id, false);
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            postEvent(new TopicInfoUpdateEvent(id));
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }

    }

    public void onMemberUnstarred(Object object) {
        try {
            SocketMemberUnstarredEvent event = getObject(object, SocketMemberUnstarredEvent.class);
            saveEvent(event);

            SocketMemberUnstarredEvent.Member member = event.getMember();
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            HumanRepository.getInstance().updateStarred(member.getId(), false);
            postEvent(new MemberStarredEvent(member.getId()));
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }

    }

    @Deprecated
    public void onTeamDomainUpdated(Object object) {
        // do nothing
//        try {
//            SocketTeamDomainUpdatedEvent event = getObject(object, SocketTeamDomainUpdatedEvent.class);
//            SocketTeamDomainUpdatedEvent.Team team = event.getTeam();
//            long teamId = team.getTopicId();
//            String domain = team.getDomain();
//
//            AccountRepository.getRepository().updateTeamDomain(teamId, domain);
//            TeamRepository.getInstance().updateTeamDomain(teamId, domain);
//            JandiPreference.setSocketConnectedLastTime(event.getTs());
//
//            postEvent(new TeamInfoChangeEvent());
//        } catch (Exception e) {
//            LogUtil.d(TAG, e.getMessage());
//        }

    }

    public void onAnnouncementStatusUpdated(Object object) {
        try {
            SocketAnnouncementUpdatedEvent event = getObject(object, SocketAnnouncementUpdatedEvent.class);
            saveEvent(event);

            SocketAnnouncementUpdatedEvent.Data data = event.getData();
            boolean opened = data.isOpened();
            TopicRepository.getInstance().updateAnnounceOpened(data.getTopicId(), opened);
            JandiPreference.setSocketConnectedLastTime(event.getTs());

            postEvent(new AnnouncementUpdatedEvent(data.getTopicId(), opened));

        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }

    }

    public void onTeamUpdated(Object object) {
        try {
            SocketTeamUpdatedEvent event = getObject(object, SocketTeamUpdatedEvent.class, true, false);
            saveEvent(event);

            if (event.getData().getTeam().getId() == TeamInfoLoader.getInstance().getTeamId()) {
                TeamRepository.getInstance().updateTeam(event.getData().getTeam());
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
            IntroActivity.startActivity(context, false);
        });
    }

    public void onPollCreated(Object object) {
        try {
            SocketPollCreatedEvent event = getObject(object, SocketPollCreatedEvent.class);
            saveEvent(event);

            SocketPollCreatedEvent.Data data = event.getData();

            Poll poll = data != null ? data.getPoll() : null;

            if (poll != null && poll.getId() > 0) {
                upsertPoll(poll);
                poll = getPollFromDatabase(poll.getId());
            }

            JandiPreference.setSocketConnectedLastTime(event.getTs());

            if (poll != null && poll.getId() > 0) {
                postEvent(new SocketPollEvent(poll, SocketPollEvent.Type.CREATED));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPollFinished(Object object) {
        try {
            SocketPollFinishedEvent event = getObject(object, SocketPollFinishedEvent.class);
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
                postEvent(new SocketPollEvent(poll, SocketPollEvent.Type.FINISHED));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPollDeleted(Object object) {
        try {
            SocketPollDeletedEvent event = getObject(object, SocketPollDeletedEvent.class);
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
                postEvent(new SocketPollEvent(poll, SocketPollEvent.Type.DELETED));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPollVoted(Object object) {
        try {
            SocketPollVotedEvent event = getObject(object, SocketPollVotedEvent.class);
            saveEvent(event);

            SocketPollVotedEvent.Data data = event.getData();

            ResMessages.Link link = data.getLinkMessage();
            Poll poll = link != null ? link.poll : null;
            if (poll != null && poll.getId() > 0 && poll.isMine()) {
                upsertPollVotedStatus(poll);
                poll = getPollFromDatabase(poll.getId());
            }

            JandiPreference.setSocketConnectedLastTime(event.getTs());

            if (poll != null && poll.getId() > 0) {
                postEvent(new SocketPollEvent(poll, SocketPollEvent.Type.VOTED));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPollCommentCreated(Object object) {
        try {
            SocketPollCommentCreatedEvent event = getObject(object, SocketPollCommentCreatedEvent.class);
            saveEvent(event);

            JandiPreference.setSocketConnectedLastTime(event.getTs());
            postEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPollCommentDeleted(Object object) {
        try {
            SocketPollCommentDeletedEvent event = getObject(object, SocketPollCommentDeletedEvent.class);
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

    public interface Command {

        void command(EventHistoryInfo t);
    }
}
