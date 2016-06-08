package com.tosslab.jandi.app.services.socket;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.MemberStarredEvent;
import com.tosslab.jandi.app.events.entities.MessageCreatedEvent;
import com.tosslab.jandi.app.events.entities.ProfileChangeEvent;
import com.tosslab.jandi.app.events.entities.RefreshConnectBotEvent;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.events.entities.TopicDeleteEvent;
import com.tosslab.jandi.app.events.entities.TopicFolderRefreshEvent;
import com.tosslab.jandi.app.events.entities.TopicInfoUpdateEvent;
import com.tosslab.jandi.app.events.entities.TopicKickedoutEvent;
import com.tosslab.jandi.app.events.files.CreateFileEvent;
import com.tosslab.jandi.app.events.files.DeleteFileEvent;
import com.tosslab.jandi.app.events.files.FileCommentRefreshEvent;
import com.tosslab.jandi.app.events.files.ShareFileEvent;
import com.tosslab.jandi.app.events.files.UnshareFileEvent;
import com.tosslab.jandi.app.events.messages.AnnouncementUpdatedEvent;
import com.tosslab.jandi.app.events.messages.LinkPreviewUpdateEvent;
import com.tosslab.jandi.app.events.messages.SocketMessageStarEvent;
import com.tosslab.jandi.app.events.team.TeamDeletedEvent;
import com.tosslab.jandi.app.events.team.TeamInfoChangeEvent;
import com.tosslab.jandi.app.events.team.TeamLeaveEvent;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.BotRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.ChatRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.FolderRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.HumanRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.RoomMarkerRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.TeamRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.TopicRepository;
import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.events.EventsApi;
import com.tosslab.jandi.app.network.client.main.LoginApi;
import com.tosslab.jandi.app.network.client.messages.MessageApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.json.JacksonMapper;
import com.tosslab.jandi.app.network.models.EventHistoryInfo;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResEventHistory;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.start.Folder;
import com.tosslab.jandi.app.network.models.start.Marker;
import com.tosslab.jandi.app.network.socket.domain.ConnectTeam;
import com.tosslab.jandi.app.services.socket.annotations.Version;
import com.tosslab.jandi.app.services.socket.to.MessageOfOtherTeamEvent;
import com.tosslab.jandi.app.services.socket.to.SocketAnnouncementCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketAnnouncementDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketAnnouncementUpdatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketChatCloseEvent;
import com.tosslab.jandi.app.services.socket.to.SocketConnectBotEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileCommentDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileCommentEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileDeleteEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileShareEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileUnsharedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketLinkPreviewMessageEvent;
import com.tosslab.jandi.app.services.socket.to.SocketLinkPreviewThumbnailEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMemberStarredEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMemberUpdatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageStarredEvent;
import com.tosslab.jandi.app.services.socket.to.SocketRoomMarkerEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTeamDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTeamJoinEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTeamLeaveEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTeamNameDomainUpdatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicFolderCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicFolderEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicFolderUpdatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicInvitedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicJoinedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicKickedoutEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicLeftEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicPushEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicStarredEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicUpdatedEvent;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.account.AccountHomeActivity_;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.UserAgentUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import dagger.Lazy;
import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class JandiSocketServiceModel {
    public static final String TAG = JandiSocketServiceModel.class.getSimpleName();

    private final Context context;
    private final ObjectMapper objectMapper;
    Lazy<AccountApi> accountApi;
    Lazy<MessageApi> messageApi;
    Lazy<LoginApi> loginApi;
    Lazy<EventsApi> eventsApi;
    private EntitySocketModel entitySocketModel;
    private PublishSubject<SocketRoomMarkerEvent> markerPublishSubject;
    private PublishSubject<SocketMessageEvent> messagePublishSubject;
    private PublishSubject<Runnable> linkPreviewSubject;
    private Subscription markerSubscribe;
    private Subscription messageSubscribe;
    private Subscription linkPreviewSubscribe;


    public JandiSocketServiceModel(Context context,
                                   Lazy<AccountApi> accountApi,
                                   Lazy<MessageApi> messageApi,
                                   Lazy<LoginApi> loginApi,
                                   Lazy<EventsApi> eventsApi) {
        this.context = context;
        this.accountApi = accountApi;
        this.messageApi = messageApi;
        this.loginApi = loginApi;
        this.eventsApi = eventsApi;
        this.objectMapper = JacksonMapper.getInstance().getObjectMapper();
        entitySocketModel = new EntitySocketModel();
    }

    public ConnectTeam getConnectTeam() {
        ResAccountInfo.UserTeam selectedTeamInfo =
                AccountRepository.getRepository().getSelectedTeamInfo();

        if (selectedTeamInfo == null) {
            return null;
        }

        long myId = TeamInfoLoader.getInstance().getMyId();

        if (myId <= 0) {
            return null;
        }

        String memberName = TeamInfoLoader.getInstance().getMemberName(myId);

        String token = TokenUtil.getAccessToken();
        return new ConnectTeam(token,
                UserAgentUtil.getDefaultUserAgent(),
                selectedTeamInfo.getTeamId(),
                selectedTeamInfo.getName(),
                selectedTeamInfo.getMemberId(), memberName);
    }

    public void refreshEntity() {
        refreshEntity(true, null, null, false);
    }

    public void refreshEntity(Object event, boolean parseUpdate) {
        refreshEntity(true, null, event, parseUpdate);
    }

    public void refreshEntity(boolean postRetrieveEvent, String socketMessageEventContent,
                              Object event, boolean parseUpdate) {

        entitySocketModel.refreshEntity(
                new EntitySocketModel.EntityRefreshEventWrapper(
                        postRetrieveEvent, parseUpdate, socketMessageEventContent, event));
    }

    public void onTeamNameUpdated(Object object) {
        try {
            SocketTeamNameDomainUpdatedEvent event = getObject(object, SocketTeamNameDomainUpdatedEvent.class);
            SocketTeamNameDomainUpdatedEvent.Team team = event.getTeam();
            long teamId = team.getId();
            String name = team.getName();

            AccountRepository.getRepository().updateTeamName(teamId, name);
            TeamRepository.getInstance().updateTeamName(teamId, name);
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            TeamInfoLoader.getInstance().refresh();

            postEvent(new TeamInfoChangeEvent());
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void onFileDeleted(Object object) {
        try {
            SocketFileEvent socketFileEvent =
                    getObject(object.toString(), SocketFileDeleteEvent.class);

            JandiPreference.setSocketConnectedLastTime(socketFileEvent.getTs());
            updateFileDeleted(socketFileEvent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateFileDeleted(SocketFileEvent socketFileEvent) {
        MessageRepository.getRepository().updateStatus(socketFileEvent.getFile().getId(), "archived");

        postEvent(new DeleteFileEvent(socketFileEvent.getTeamId(), socketFileEvent.getFile().getId()));
    }

    /**
     * message_created 로 병합됨
     *
     * @param object
     */
    @Deprecated
    public void onFileCommentCreated(Object object) {
        try {
            SocketFileCommentEvent socketFileEvent =
                    getObject(object.toString(), SocketFileCommentEvent.class);
            postEvent(
                    new FileCommentRefreshEvent(socketFileEvent.getEvent(),
                            socketFileEvent.getTeamId(),
                            socketFileEvent.getFile().getId(),
                            socketFileEvent.getComment().getId(),
                            TextUtils.equals(socketFileEvent.getEvent(), "file_comment_created")));
            JandiPreference.setSocketConnectedLastTime(socketFileEvent.getTs());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onFileCommentDeleted(Object object) {
        try {
            SocketFileCommentDeletedEvent event =
                    getObject(object.toString(), SocketFileCommentDeletedEvent.class);
            updateCommentDeleted(event);

            JandiPreference.setSocketConnectedLastTime(event.getTs());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateCommentDeleted(SocketFileCommentDeletedEvent socketCommentEvent) {
        FileCommentRefreshEvent event = new FileCommentRefreshEvent(socketCommentEvent.getEvent(),
                socketCommentEvent.getTeamId(),
                socketCommentEvent.getFile().getId(),
                socketCommentEvent.getComment().getId(),
                false /* isAdded */);

        long messageId = socketCommentEvent.getComment().getId();
        MessageRepository.getRepository().deleteMessage(messageId);

        List<SocketFileCommentDeletedEvent.Room> rooms = socketCommentEvent.getRooms();
        if (rooms != null && !rooms.isEmpty()) {
            List<Long> sharedRooms = new ArrayList<>();
            Observable.from(rooms)
                    .collect(() -> sharedRooms, (list, room) -> list.add(room.getId()))
                    .subscribe();
            event.setSharedRooms(sharedRooms);
        }

        postEvent(event);
    }

    public void onMessage(Object object) {
        try {
            String content = object.toString();
            SocketMessageEvent socketMessageEvent =
                    getObject(content, SocketMessageEvent.class);
            JandiPreference.setSocketConnectedLastTime(socketMessageEvent.getTs());

            String messageType = socketMessageEvent.getMessageType();
            if (TextUtils.equals(messageType, "message_delete")) {
                long messageId = socketMessageEvent.getMessageId();
                MessageRepository.getRepository().deleteMessage(messageId);
            } /*else if (TextUtils.equals(messageType, "topic_leave")
                    || TextUtils.equals(messageType, "topic_join")
                    || TextUtils.equals(messageType, "topic_invite")) {
            } else {
                postEvent(socketMessageEvent);
            }*/

            messagePublishSubject.onNext(socketMessageEvent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onTopicUpdated(Object object) {
        try {
            SocketTopicUpdatedEvent event =
                    getObject(object.toString(), SocketTopicUpdatedEvent.class);
            TopicRepository.getInstance().updateTopic(event.getData().getTopic());
            TeamInfoLoader.getInstance().refresh();
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            postEvent(new TopicInfoUpdateEvent(event.getData().getTopic().getId()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onChatClosed(Object object) {
        try {
            SocketChatCloseEvent event = getObject(object, SocketChatCloseEvent.class);
            SocketChatCloseEvent.Data chat = event.getChat();
            ChatRepository.getInstance().updateChatStatusToArchived(chat.getId());
            JandiPreference.setSocketConnectedLastTime(event.getTs());

            postEvent(new RetrieveTopicListEvent());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onTopicLeft(Object object) {
        try {
            SocketTopicLeftEvent event = getObject(object.toString(), SocketTopicLeftEvent.class);
            SocketTopicLeftEvent.Data data = event.getData();
            TopicRepository.getInstance().removeMember(data.getTopicId(), data.getMemberId());
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            TeamInfoLoader.getInstance().refresh();
            postEvent(new TopicDeleteEvent(event.getTeamId(), data.getTopicId()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onMemberStarred(Object object) {
        try {
            SocketMemberStarredEvent event = getObject(object.toString(), SocketMemberStarredEvent.class);
            SocketMemberStarredEvent.Member member = event.getMember();
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            long chatId = TeamInfoLoader.getInstance().getChatId(member.getId());
            if (chatId > 0) {
                ChatRepository.getInstance().updateStarred(chatId, true);
                TeamInfoLoader.getInstance().refresh();
            }
            postEvent(new MemberStarredEvent(member.getId()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onFileUnshared(Object object) {
        try {
            SocketFileUnsharedEvent socketFileEvent =
                    getObject(object.toString(), SocketFileUnsharedEvent.class);

            long fileId = socketFileEvent.getFile().getId();
            long roomId = socketFileEvent.room.id;

            MessageRepository.getRepository().deleteSharedRoom(fileId, roomId);
            JandiPreference.setSocketConnectedLastTime(socketFileEvent.getTs());
            postEvent(new UnshareFileEvent(roomId, fileId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * message_created 로 병합됨
     *
     * @param object
     */
    @Deprecated
    public void onFileShared(Object object) {
        try {
            SocketFileShareEvent socketFileShareEvent =
                    getObject(object.toString(), SocketFileShareEvent.class);
            long teamId = socketFileShareEvent.getTeamId();
            long fileId = socketFileShareEvent.getFile().getId();
            postEvent(new ShareFileEvent(teamId, fileId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void onRoomMarkerUpdated(Object object) {
        try {
            SocketRoomMarkerEvent event =
                    getObject(object.toString(), SocketRoomMarkerEvent.class);

            SocketRoomMarkerEvent.Data data = event.getData();
            Marker marker = data.getMarker();

            long roomId = data.getRoomId();
            long memberId = marker.getMemberId();
            long lastLinkId = marker.getReadLinkId();
            RoomMarkerRepository.getInstance().updateRoomMarker(roomId, memberId, lastLinkId);
            JandiPreference.setSocketConnectedLastTime(event.getTs());

            postEvent(event);
            markerPublishSubject.onNext(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onLinkPreviewCreated(final Object object) {
        // TODO 실데이터 처리
        linkPreviewSubject.onNext(() -> retrieveAndUpdateLinkPreview(object));
    }

    private void retrieveAndUpdateLinkPreview(Object object) {
        try {
            SocketLinkPreviewMessageEvent socketLinkPreviewMessageEvent =
                    getObject(object.toString(), SocketLinkPreviewMessageEvent.class);

            int teamId = socketLinkPreviewMessageEvent.getTeamId();
            if (AccountRepository.getRepository().getSelectedTeamId() != teamId) {
                return;
            }

            long messageId = socketLinkPreviewMessageEvent.getMessage().getId();
            JandiPreference.setSocketConnectedLastTime(socketLinkPreviewMessageEvent.getTs());

            if (updateLinkPreview(teamId, messageId)) {
                postEvent(new LinkPreviewUpdateEvent(messageId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onLinkPreviewImage(final Object object) {
        linkPreviewSubject.onNext(() -> updateLinkPreview(object));
    }

    private void updateLinkPreview(Object object) {
        try {
            SocketLinkPreviewThumbnailEvent socketLinkPreviewMessageEvent =
                    getObject(object.toString(), SocketLinkPreviewThumbnailEvent.class);

            SocketLinkPreviewThumbnailEvent.Data data = socketLinkPreviewMessageEvent.getData();
            ResMessages.LinkPreview linkPreview = data.getLinkPreview();

            if (AccountRepository.getRepository().getSelectedTeamId() != data.getTeamId()) {
                return;
            }

            long messageId = data.getMessageId();

            ResMessages.TextMessage textMessage =
                    MessageRepository.getRepository().getTextMessage(messageId);
            textMessage.linkPreview = linkPreview;
            MessageRepository.getRepository().upsertTextMessage(textMessage);

            postEvent(new LinkPreviewUpdateEvent(messageId));
            JandiPreference.setSocketConnectedLastTime(socketLinkPreviewMessageEvent.getTs());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean updateLinkPreview(int teamId, long messageId) {
        try {
            ResMessages.OriginalMessage message = messageApi.get().getMessage(teamId, messageId);
            if (message instanceof ResMessages.TextMessage) {
                ResMessages.TextMessage textMessage = (ResMessages.TextMessage) message;
                ResMessages.LinkPreview linkPreview = textMessage.linkPreview;
                if (linkPreview != null) {
                    MessageRepository.getRepository().upsertTextMessage(textMessage);
                    return true;
                }
            }
        } catch (RetrofitException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void onAnnouncementCreated(Object object) {
        // 공지사항 정보 추가

        try {
            SocketAnnouncementCreatedEvent event =
                    getObject(object.toString(), SocketAnnouncementCreatedEvent.class);

            SocketAnnouncementCreatedEvent.Data data = event.getData();
            int topicId = data.getTopicId();
            TopicRepository.getInstance().createAnnounce(topicId, data.getAnnouncement());
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            TeamInfoLoader.getInstance().refresh();
            postEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void onAnnouncementDeleted(Object object) {
        try {

            // 공지사항 정보 갱신 로직
            SocketAnnouncementDeletedEvent event =
                    getObject(object.toString(), SocketAnnouncementDeletedEvent.class);

            TopicRepository.getInstance().removeAnnounce(event.getData().getTopicId());
            TeamInfoLoader.getInstance().refresh();
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            postEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onRoomSubscriptionUpdated(Object object) {
        try {

            // 푸시 정보 갱신 로직 추가
            SocketTopicPushEvent socketTopicPushEvent =
                    getObject(object.toString(), SocketTopicPushEvent.class);

            SocketTopicPushEvent.Data data = socketTopicPushEvent.getData();
            int roomId = data.getRoomId();
            boolean subscribe = data.isSubscribe();
            TopicRepository.getInstance().updatePushSubscribe(roomId, subscribe);
            TeamInfoLoader.getInstance().refresh();

            postEvent(socketTopicPushEvent);
            JandiPreference.setSocketConnectedLastTime(socketTopicPushEvent.getTs());
        } catch (RetrofitException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private <T> void postEvent(T object) {
        EventBus eventBus = EventBus.getDefault();
        if (eventBus.hasSubscriberForEvent(object.getClass())) {
            eventBus.post(object);
        }
    }

    public void startMarkerObserver() {
        markerPublishSubject = PublishSubject.create();
        markerSubscribe = markerPublishSubject.throttleWithTimeout(500, TimeUnit.MILLISECONDS)
                .onBackpressureBuffer()
                .filter(event -> {
                    long markingUserId = event.getData().getMarker().getMemberId();
                    return Observable.from(AccountRepository.getRepository().getAccountTeams())
                            .takeFirst(userTeam -> userTeam.getMemberId() == markingUserId)
                            .map(userTeam1 -> true)
                            .toBlocking()
                            .firstOrDefault(false);
                })
                .subscribe(event -> {

                    try {
                        if (event.getTeamId() == TeamInfoLoader.getInstance().getTeamId()) {
                            // 같은 팀의 내 마커가 갱신된 경우
                            postEvent(new RetrieveTopicListEvent());
                        } else {
                            // 다른 팀의 내 마커가 갱신된 경우
                            ResAccountInfo resAccountInfo = accountApi.get().getAccountInfo();
                            AccountUtil.removeDuplicatedTeams(resAccountInfo);
                            AccountRepository.getRepository().upsertAccountAllInfo(resAccountInfo);

                            Observable.from(resAccountInfo.getMemberships())
                                    .map(ResAccountInfo.UserTeam::getUnread)
                                    .reduce((prev, current) -> prev + current)
                                    .subscribe(totalUnreadCount -> {
                                        BadgeUtils.setBadge(context, totalUnreadCount);
                                    });

                            postEvent(new MessageOfOtherTeamEvent());
                        }

                    } catch (RetrofitException e) {
                        e.printStackTrace();
                    }

                }, throwable -> LogUtil.d(throwable.getMessage()));
    }

    public void stopMarkerObserver() {
        if (markerSubscribe != null && !markerSubscribe.isUnsubscribed()) {
            markerSubscribe.unsubscribe();
        }
    }

    public void startMessageObserver() {
        messagePublishSubject = PublishSubject.create();
        messageSubscribe = messagePublishSubject
                .filter(event -> event.getTeamId() != AccountRepository.getRepository().getSelectedTeamId())
                .throttleWithTimeout(1000 * 3, TimeUnit.MILLISECONDS)
                .onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .subscribe(event -> {
                    try {
                        ResAccountInfo resAccountInfo = accountApi.get().getAccountInfo();
                        AccountUtil.removeDuplicatedTeams(resAccountInfo);
                        AccountRepository.getRepository().upsertAccountAllInfo(resAccountInfo);

                        postEvent(new MessageOfOtherTeamEvent());
                    } catch (RetrofitException e) {
                        e.printStackTrace();
                    }
                }, Throwable::printStackTrace);
    }

    public void stopMessageObserver() {
        if (messageSubscribe != null && !messageSubscribe.isUnsubscribed()) {
            messageSubscribe.unsubscribe();
        }
    }

    public void startLinkPreviewObserver() {
        linkPreviewSubject = PublishSubject.create();
        linkPreviewSubscribe = linkPreviewSubject
                .onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .subscribe(Runnable::run);
    }

    public void stopLinkPreviewObserver() {
        if (linkPreviewSubscribe != null && !linkPreviewSubscribe.isUnsubscribed()) {
            linkPreviewSubscribe.unsubscribe();
        }
    }

    public ResAccessToken refreshToken() throws RetrofitException {
        String jandiRefreshToken = TokenUtil.getRefreshToken();
        ReqAccessToken refreshReqToken = ReqAccessToken.createRefreshReqToken(jandiRefreshToken);
        return loginApi.get().getAccessToken(refreshReqToken);
    }

    public void onFileCreated(Object object) {
        try {
            SocketFileEvent socketFileEvent =
                    getObject(object.toString(), SocketFileEvent.class);
            JandiPreference.setSocketConnectedLastTime(socketFileEvent.getTs());
            postEvent(new CreateFileEvent(socketFileEvent.getTeamId(), socketFileEvent.getFile().getId()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopRefreshEntityObserver() {
        entitySocketModel.stopObserver();
    }

    public void onMessageUnstarred(Object object) {
        try {
            SocketMessageStarredEvent socketFileEvent
                    = getObject(object.toString(), SocketMessageStarredEvent.class);

            MessageRepository.getRepository().updateStarred(socketFileEvent.getStarredInfo()
                    .getMessageId(), false);

            JandiPreference.setSocketConnectedLastTime(socketFileEvent.getTs());
            postEvent(new SocketMessageStarEvent(socketFileEvent.getStarredInfo().getMessageId(), false));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void onMessageStarred(Object object) {
        try {
            SocketMessageStarredEvent socketFileEvent
                    = getObject(object.toString(), SocketMessageStarredEvent.class);

            MessageRepository.getRepository().updateStarred(socketFileEvent.getStarredInfo()
                    .getMessageId(), true);

            JandiPreference.setSocketConnectedLastTime(socketFileEvent.getTs());
            postEvent(new SocketMessageStarEvent(socketFileEvent.getStarredInfo().getMessageId(), true));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onFolderDeleted(Object object) {
        try {
            SocketTopicFolderEvent event
                    = getObject(object.toString(), SocketTopicFolderEvent.class);

            long folderId = event.getData().getFolderId();
            FolderRepository.getInstance().deleteFolder(folderId);

            JandiPreference.setSocketConnectedLastTime(event.getTs());
            TeamInfoLoader.getInstance().refresh();

            postEvent(event);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onFolderItemCreated(Object object) {
        try {
            SocketTopicFolderEvent event
                    = getObject(object.toString(), SocketTopicFolderEvent.class);

            long folderId = event.getData().getFolderId();
            long roomId = event.getData().getRoomId();
            FolderRepository.getInstance().addTopic(folderId, roomId);

            JandiPreference.setSocketConnectedLastTime(event.getTs());
            TeamInfoLoader.getInstance().refresh();

            postEvent(event);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onFolderItemDeleted(Object object) {
        try {
            SocketTopicFolderEvent event
                    = getObject(object.toString(), SocketTopicFolderEvent.class);

            long folderId = event.getData().getFolderId();
            long roomId = event.getData().getRoomId();
            FolderRepository.getInstance().removeTopic(folderId, roomId);

            JandiPreference.setSocketConnectedLastTime(event.getTs());
            TeamInfoLoader.getInstance().refresh();

            postEvent(event);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onTopicFolderCreated(Object object) {
        try {
            SocketTopicFolderCreatedEvent event
                    = getObject(object.toString(), SocketTopicFolderCreatedEvent.class);

            FolderRepository.getInstance().addFolder(event.getTeamId(), event.getData().getFolder());
            TeamInfoLoader.getInstance().refresh();

            postEvent(new TopicFolderRefreshEvent());

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void onTopicFolderUpdated(Object object) {
        try {
            SocketTopicFolderUpdatedEvent event
                    = getObject(object.toString(), SocketTopicFolderUpdatedEvent.class);

            Folder folder = event.getData().getFolder();
            FolderRepository.getInstance().updateFolderName(folder.getId(), folder.getName());
            FolderRepository.getInstance().updateFolderSeq(folder.getId(), folder.getSeq());

            TeamInfoLoader.getInstance().refresh();
            postEvent(new TopicFolderRefreshEvent());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void onTeamLeft(Object object) {
        try {
            SocketTeamLeaveEvent event = getObject(object.toString(), SocketTeamLeaveEvent.class);
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
                            AccountHomeActivity_.intent(JandiApplication.getContext())
                                    .flags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
                                    .start();

                            InitialInfoRepository.getInstance().removeInitialInfo(data.getTeamId());
                            JandiPreference.setSocketConnectedLastTime(-1);

                            TeamInfoLoader instance = TeamInfoLoader.getInstance();
                            instance = null;
                        });
            } else {

                HumanRepository.getInstance().removeHuman(memberId);
                JandiPreference.setSocketConnectedLastTime(event.getTs());
                TeamInfoLoader.getInstance().refresh();

                postEvent(teamLeaveEvent);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onTeamDeleted(Object object) {
        try {
            SocketTeamDeletedEvent event = getObject(object.toString(), SocketTeamDeletedEvent.class);

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
                            AccountHomeActivity_.intent(JandiApplication.getContext())
                                    .flags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
                                    .start();

                            InitialInfoRepository.getInstance().removeInitialInfo(teamId);
                            JandiPreference.setSocketConnectedLastTime(-1);

                            TeamInfoLoader instance = TeamInfoLoader.getInstance();
                            instance = null;
                        });
            } else {
                AccountRepository.getRepository().removeTeamInfo(teamId);
                JandiPreference.setSocketConnectedLastTime(event.getTs());
                postEvent(new TeamDeletedEvent(teamId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onTopicKickOut(Object object) {
        try {
            SocketTopicKickedoutEvent event =
                    getObject(object.toString(), SocketTopicKickedoutEvent.class);
            SocketTopicKickedoutEvent.Data data = event.getData();

            TopicRepository.getInstance().deleteTopic(data.getRoomId());

            postEvent(new TopicKickedoutEvent(data.getRoomId(), data.getTeamId()));
            TeamInfoLoader.getInstance().refresh();

            JandiPreference.setSocketConnectedLastTime(event.getTs());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onMessageCreated(Object object) {
        try {
            SocketMessageCreatedEvent event = getObject(object, SocketMessageCreatedEvent.class);
            insertNewMessage(event);
            JandiPreference.setSocketConnectedLastTime(event.getTs());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertNewMessage(SocketMessageCreatedEvent event) {
        long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
        if (selectedTeamId == event.getTeamId()
                && event.getData() != null
                && event.getData().getLinkMessage() != null
                && event.getData().getLinkMessage().toEntity.length > 0) {

            long roomId = event.getData().getLinkMessage().toEntity[0];
            ResMessages.Link linkMessage = event.getData().getLinkMessage();
            linkMessage.roomId = roomId;
            MessageRepository.getRepository().upsertMessage(linkMessage);
            postEvent(new MessageCreatedEvent(event.getTeamId(), roomId, linkMessage.id));
        }
    }

    private <T> T getObject(Object object, Class<T> clazz) throws Exception {
        T t = objectMapper.readValue(object.toString(), clazz);
        throwExceptionIfInvaildVersion(t);
        return t;
    }

    void throwExceptionIfInvaildVersion(Object object) throws Exception {
        if (!validVersion(object)) {
            throw new Exception("Invalid Version : " + object.getClass().getName());
        }
    }

    boolean validVersion(Object object) {
        Version annotation = object.getClass().getAnnotation(Version.class);
        if (annotation == null) {
            return false;
        } else {
            try {
                Field version = null;

                Class<?> clazz = object.getClass();
                while (version == null && clazz != null && clazz != Object.class) {

                    try {
                        version = clazz.getDeclaredField("version");
                    } catch (NoSuchFieldException e) {
                        clazz = clazz.getSuperclass();
                    }

                }


                if (version == null) {
                    return false;
                }
                version.setAccessible(true);

                int versionValue = version.getInt(object);
                version.setAccessible(false);

                if (annotation.value() == versionValue) {
                    return true;
                }

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            return false;
        }
    }

    public void onConnectBotCreated(Object object) {
        try {
            SocketConnectBotEvent event = getObject(object, SocketConnectBotEvent.class);
            BotRepository.getInstance().addBot(event.getData().getBot());
            TeamInfoLoader.getInstance().refresh();
            postEvent(new RefreshConnectBotEvent(event.getData().getBot()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onConnectBotDeleted(Object object) {
        try {
            SocketConnectBotEvent event = getObject(object, SocketConnectBotEvent.class);
            BotRepository.getInstance().removeBot(event.getData().getBot().getId());
            TeamInfoLoader.getInstance().refresh();

            postEvent(new RefreshConnectBotEvent(event.getData().getBot()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onConnectBotUpdated(Object object) {
        try {
            SocketConnectBotEvent event = getObject(object, SocketConnectBotEvent.class);
            BotRepository.getInstance().updateBot(event.getData().getBot());
            TeamInfoLoader.getInstance().refresh();

            postEvent(new RefreshConnectBotEvent(event.getData().getBot()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onTeamJoin(Object object) {
        try {
            SocketTeamJoinEvent event = getObject(object, SocketTeamJoinEvent.class);
            SocketTeamJoinEvent.Data data = event.getData();
            HumanRepository.getInstance().addHuman(data.getTeamId(), data.getMember());
            TeamInfoLoader.getInstance().refresh();
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            postEvent(new RetrieveTopicListEvent());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 확장성 생각하여 추후 모듈로 빼내야 함.
    public void updateEventHistory() {

        long socketConnectedLastTime = JandiPreference.getSocketConnectedLastTime();
        getEventHistory(socketConnectedLastTime, "file_unshared")
                .filter(eventHistoryInfo -> eventHistoryInfo instanceof SocketFileUnsharedEvent)
                .map(eventHistoryInfo -> (SocketFileUnsharedEvent) eventHistoryInfo)
                .subscribe(eventHistoryInfo -> {
                    long fileId = eventHistoryInfo.getFile().getId();
                    long roomId = eventHistoryInfo.room.id;
                    MessageRepository.getRepository().deleteSharedRoom(fileId, roomId);
                    JandiPreference.setSocketConnectedLastTime(eventHistoryInfo.getTs());
                }, Throwable::printStackTrace);

        getEventHistory(socketConnectedLastTime, "message_created")
                .filter(eventHistoryInfo -> eventHistoryInfo instanceof SocketMessageCreatedEvent)
                .map(eventHistoryInfo -> (SocketMessageCreatedEvent) eventHistoryInfo)
                .subscribe((event1) -> {
                    insertNewMessage(event1);
                    JandiPreference.setSocketConnectedLastTime(event1.getTs());
                }, Throwable::printStackTrace);

        getEventHistory(socketConnectedLastTime, "file_comment_deleted")
                .filter(eventHistoryInfo -> eventHistoryInfo instanceof SocketFileCommentDeletedEvent)
                .map(eventHistoryInfo -> (SocketFileCommentDeletedEvent) eventHistoryInfo)
                .subscribe((socketCommentEvent) -> {
                    updateCommentDeleted(socketCommentEvent);
                    JandiPreference.setSocketConnectedLastTime(socketCommentEvent.getTs());
                }, Throwable::printStackTrace);

        getEventHistory(socketConnectedLastTime, "file_deleted")
                .filter(eventHistoryInfo -> eventHistoryInfo instanceof SocketFileEvent)
                .map(eventHistoryInfo -> (SocketFileEvent) eventHistoryInfo)
                .subscribe((socketFileEvent) -> {
                    updateFileDeleted(socketFileEvent);
                    JandiPreference.setSocketConnectedLastTime(socketFileEvent.getTs());

                }, Throwable::printStackTrace);

        getEventHistory(socketConnectedLastTime, "message")
                .filter(eventHistoryInfo -> eventHistoryInfo instanceof SocketMessageEvent)
                .map(eventHistoryInfo -> (SocketMessageEvent) eventHistoryInfo)
                .filter(event -> TextUtils.equals(event.getMessageType(), "message_delete"))
                .subscribe(event -> {
                    long messageId = event.getMessageId();
                    MessageRepository.getRepository().deleteMessage(messageId);
                    postEvent(event);
                    JandiPreference.setSocketConnectedLastTime(event.getTs());

                }, Throwable::printStackTrace);

    }

    @NonNull
    private Observable<EventHistoryInfo> getEventHistory(long socketConnectedLastTime, String eventType) {
        return Observable.just(socketConnectedLastTime)
                .observeOn(Schedulers.io())
                .filter(ts -> ts == -1 || ts > 0)
                .map(ts -> {
                    try {
                        long userId = TeamInfoLoader.getInstance().getMyId();
                        return eventsApi.get().getEventHistory(ts, userId, eventType);
                    } catch (RetrofitException e) {
                        e.printStackTrace();
                        ResEventHistory resEventHistory = new ResEventHistory();
                        resEventHistory.setRecords(new ArrayList<>(0));
                        return resEventHistory;
                    }
                })
                .flatMap(resEventHistory -> Observable.from(resEventHistory.getRecords()))
                .filter(this::validVersion);
    }

    public void onTopicCreated(Object object) {
        try {
            SocketTopicCreatedEvent event = getObject(object, SocketTopicCreatedEvent.class);
            TopicRepository.getInstance().addTopic(event.getData().getTopic());
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            TeamInfoLoader.getInstance().refresh();

            postEvent(new RetrieveTopicListEvent());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onTopicJoined(Object object) {
        try {
            SocketTopicJoinedEvent event = getObject(object, SocketTopicJoinedEvent.class);
            SocketTopicJoinedEvent.Data data = event.getData();
            TopicRepository.getInstance().addMember(data.getTopicId(), Arrays.asList(data.getMemberId()));
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            TeamInfoLoader.getInstance().refresh();

            postEvent(new RetrieveTopicListEvent());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onTopicInvitedListener(Object object) {
        try {
            SocketTopicInvitedEvent event = getObject(object, SocketTopicInvitedEvent.class);
            SocketTopicInvitedEvent.Data data = event.getData();
            TopicRepository.getInstance().addMember(data.getTopicId(), data.getInvitees());
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            TeamInfoLoader.getInstance().refresh();

            postEvent(new RetrieveTopicListEvent());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onMemberUpdated(Object object) {
        try {
            SocketMemberUpdatedEvent event = getObject(object, SocketMemberUpdatedEvent.class);
            SocketMemberUpdatedEvent.Data data = event.getData();
            HumanRepository.getInstance().updateHuman(data.getMember());
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            TeamInfoLoader.getInstance().refresh();

            postEvent(new ProfileChangeEvent(data.getMember()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onTopicDeleted(Object object) {
        try {
            SocketTopicDeletedEvent event = getObject(object, SocketTopicDeletedEvent.class);
            long topicId = event.getData().getTopicId();
            TopicRepository.getInstance().deleteTopic(topicId);
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            postEvent(new TopicDeleteEvent(event.getTeamId(), topicId));
            TeamInfoLoader.getInstance().refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onTopicStarred(Object object) {
        try {
            SocketTopicStarredEvent event = getObject(object, SocketTopicStarredEvent.class);
            SocketTopicStarredEvent.Topic topic = event.getTopic();
            long id = topic.getId();
            TopicRepository.getInstance().updateStarred(id, true);
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            TeamInfoLoader.getInstance().refresh();
            postEvent(new TopicInfoUpdateEvent(id));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onTopicUnstarred(Object object) {
        try {
            SocketTopicStarredEvent event = getObject(object, SocketTopicStarredEvent.class);
            SocketTopicStarredEvent.Topic topic = event.getTopic();
            long id = topic.getId();
            TopicRepository.getInstance().updateStarred(id, false);
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            TeamInfoLoader.getInstance().refresh();
            postEvent(new TopicInfoUpdateEvent(id));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void onMemberUnstarred(Object object) {
        try {
            SocketMemberStarredEvent event = getObject(object.toString(), SocketMemberStarredEvent.class);
            SocketMemberStarredEvent.Member member = event.getMember();
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            long chatId = TeamInfoLoader.getInstance().getChatId(member.getId());
            if (chatId > 0) {
                ChatRepository.getInstance().updateStarred(chatId, false);
                TeamInfoLoader.getInstance().refresh();
            }
            postEvent(new MemberStarredEvent(member.getId()));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void onTeamDomainUpdated(Object object) {
        try {
            SocketTeamNameDomainUpdatedEvent event = getObject(object, SocketTeamNameDomainUpdatedEvent.class);
            SocketTeamNameDomainUpdatedEvent.Team team = event.getTeam();
            long teamId = team.getId();
            String domain = team.getDomain();

            AccountRepository.getRepository().updateTeamDomain(teamId, domain);
            TeamRepository.getInstance().updateTeamDomain(teamId, domain);
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            TeamInfoLoader.getInstance().refresh();

            postEvent(new TeamInfoChangeEvent());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void onAnnouncementStatusUpdated(Object object) {
        try {
            SocketAnnouncementUpdatedEvent event = getObject(object, SocketAnnouncementUpdatedEvent.class);
            SocketAnnouncementUpdatedEvent.Data data = event.getData();
            boolean opened = data.isOpened();
            TopicRepository.getInstance().updateAnnounceOpened(data.getTopicId(), opened);
            JandiPreference.setSocketConnectedLastTime(event.getTs());
            TeamInfoLoader.getInstance().refresh();

            postEvent(new AnnouncementUpdatedEvent(data.getTopicId(), opened));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
