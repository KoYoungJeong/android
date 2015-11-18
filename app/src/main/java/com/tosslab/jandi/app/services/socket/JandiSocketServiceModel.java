package com.tosslab.jandi.app.services.socket;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.MemberStarredEvent;
import com.tosslab.jandi.app.events.entities.ProfileChangeEvent;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.events.entities.TopicDeleteEvent;
import com.tosslab.jandi.app.events.entities.TopicInfoUpdateEvent;
import com.tosslab.jandi.app.events.entities.TopicThrowoutEvent;
import com.tosslab.jandi.app.events.files.CreateFileEvent;
import com.tosslab.jandi.app.events.files.DeleteFileEvent;
import com.tosslab.jandi.app.events.files.FileCommentRefreshEvent;
import com.tosslab.jandi.app.events.files.ShareFileEvent;
import com.tosslab.jandi.app.events.messages.LinkPreviewUpdateEvent;
import com.tosslab.jandi.app.events.messages.SocketMessageStarEvent;
import com.tosslab.jandi.app.events.team.TeamInfoChangeEvent;
import com.tosslab.jandi.app.events.team.TeamLeaveEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccessTokenRepository;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.BadgeCountRepository;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.EntityClientManager_;
import com.tosslab.jandi.app.network.json.JacksonMapper;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.manager.restapiclient.JacksonConvertedSimpleRestApiClient;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.socket.domain.ConnectTeam;
import com.tosslab.jandi.app.services.socket.to.MessageOfOtherTeamEvent;
import com.tosslab.jandi.app.services.socket.to.SocketAnnouncementEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileCommentEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileDeleteEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileUnsharedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketLinkPreviewMessageEvent;
import com.tosslab.jandi.app.services.socket.to.SocketLinkPreviewThumbnailEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMemberEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMemberProfileEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageStarredEvent;
import com.tosslab.jandi.app.services.socket.to.SocketRoomMarkerEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTeamLeaveEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicFolderEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicPushEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicThrowoutEvent;
import com.tosslab.jandi.app.ui.account.AccountHomeActivity_;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.UserAgentUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by Steve SeongUg Jung on 15. 4. 6..
 */
public class JandiSocketServiceModel {
    public static final String TAG = JandiSocketServiceModel.class.getSimpleName();

    private final Context context;
    private final ObjectMapper objectMapper;
    private EntitySocketModel entitySocketModel;

    private PublishSubject<SocketRoomMarkerEvent> markerPublishSubject;
    private PublishSubject<SocketMessageEvent> messagePublishSubject;
    private PublishSubject<Runnable> linkPreviewSubject;

    private Subscription markerSubscribe;
    private Subscription messageSubscribe;
    private Subscription linkPreviewSubscribe;

    public JandiSocketServiceModel(Context context) {
        this.context = context;
        this.objectMapper = JacksonMapper.getInstance().getObjectMapper();
        entitySocketModel = new EntitySocketModel(context);
    }

    public ConnectTeam getConnectTeam() {
        ResAccountInfo.UserTeam selectedTeamInfo =
                AccountRepository.getRepository().getSelectedTeamInfo();

        if (selectedTeamInfo == null) {
            return null;
        }

        EntityManager entityManager = EntityManager.getInstance();
        FormattedEntity me = entityManager.getMe();

        if (me == null) {
            return null;
        }

        String token = AccessTokenRepository.getRepository().getAccessToken().getAccessToken();
        return new ConnectTeam(token,
                UserAgentUtil.getDefaultUserAgent(context),
                selectedTeamInfo.getTeamId(),
                selectedTeamInfo.getName(),
                selectedTeamInfo.getMemberId(), me.getName());
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

    public void refreshAccountInfo() {
        try {
            ResAccountInfo resAccountInfo = RequestApiManager.getInstance().getAccountInfoByMainRest();
            AccountRepository.getRepository().upsertAccountAllInfo(resAccountInfo);
            postEvent(new TeamInfoChangeEvent());
        } catch (RetrofitError e) {
            e.printStackTrace();
        }

    }

    public void deleteFile(Object object) {
        try {
            SocketFileEvent socketFileEvent =
                    objectMapper.readValue(object.toString(), SocketFileDeleteEvent.class);

            MessageRepository.getRepository().updateStatus(socketFileEvent.getFile().getId(), "archived");

            postEvent(new DeleteFileEvent(socketFileEvent.getTeamId(), socketFileEvent.getFile().getId()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshFileComment(Object object) {
        try {
            SocketFileCommentEvent socketFileEvent =
                    objectMapper.readValue(object.toString(), SocketFileCommentEvent.class);
            postEvent(
                    new FileCommentRefreshEvent(socketFileEvent.getEvent(),
                            socketFileEvent.getFile().getId(),
                            socketFileEvent.getComment().getId()
                    ));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshMessage(Object object) {
        try {
            String content = object.toString();
            SocketMessageEvent socketMessageEvent =
                    objectMapper.readValue(content, SocketMessageEvent.class);

            String messageType = socketMessageEvent.getMessageType();
            if (TextUtils.equals(messageType, "topic_leave")
                    || TextUtils.equals(messageType, "topic_join")
                    || TextUtils.equals(messageType, "topic_invite")) {
                refreshEntity(true, content, socketMessageEvent, false);
            } else {
                postEvent(socketMessageEvent);
            }

            messagePublishSubject.onNext(socketMessageEvent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshTopicState(Object object) {
        try {
            SocketTopicEvent socketTopicEvent =
                    objectMapper.readValue(object.toString(), SocketTopicEvent.class);
            refreshEntity(new TopicInfoUpdateEvent(socketTopicEvent.getTopic().getId()), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshChatCloseListener(Object object) {
        refreshEntity();
    }

    public void refreshMemberProfile(Object object) {
        try {
            SocketMemberProfileEvent socketTopicEvent =
                    objectMapper.readValue(object.toString(), SocketMemberProfileEvent.class);

            ResLeftSideMenu.User member = socketTopicEvent.getMember();

            refreshEntity(new ProfileChangeEvent(member), false);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void refreshTopicDelete(Object object) {
        try {
            SocketTopicEvent socketTopicEvent =
                    objectMapper.readValue(object.toString(), SocketTopicEvent.class);

            refreshEntity(new TopicDeleteEvent(socketTopicEvent.getTeamId(), socketTopicEvent.getTopic().getId()), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshMemberStarred(Object object) {
        try {
            SocketMemberEvent socketMemberEvent =
                    objectMapper.readValue(object.toString(), SocketMemberEvent.class);
            refreshEntity(new MemberStarredEvent(socketMemberEvent.getMember().getId()), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unshareFile(Object object) {
        try {
            SocketFileEvent socketFileEvent =
                    objectMapper.readValue(object.toString(), SocketFileUnsharedEvent.class);

            postEvent(new ShareFileEvent(socketFileEvent.getTeamId(), socketFileEvent.getFile().getId()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void updateMarker(Object object) {
        try {
            SocketRoomMarkerEvent socketRoomMarkerEvent =
                    objectMapper.readValue(object.toString(), SocketRoomMarkerEvent.class);
            postEvent(socketRoomMarkerEvent);
            if (EntityManager.getInstance().getMe().getId()
                    == socketRoomMarkerEvent.getMarker().getMemberId()) {
                markerPublishSubject.onNext(socketRoomMarkerEvent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateLinkPreviewMessage(final Object object) {
        linkPreviewSubject.onNext(() -> retrieveAndUpdateLinkPreview(object));
    }

    private void retrieveAndUpdateLinkPreview(Object object) {
        try {
            SocketLinkPreviewMessageEvent socketLinkPreviewMessageEvent =
                    objectMapper.readValue(object.toString(), SocketLinkPreviewMessageEvent.class);

            int teamId = socketLinkPreviewMessageEvent.getTeamId();
            if (AccountRepository.getRepository().getSelectedTeamId() != teamId) {
                return;
            }

            int messageId = socketLinkPreviewMessageEvent.getMessage().getId();

            if (updateLinkPreview(teamId, messageId)) {
                postEvent(new LinkPreviewUpdateEvent(messageId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateLinkPreviewThumbnail(final Object object) {
        linkPreviewSubject.onNext(() -> updateLinkPreview(object));
    }

    private void updateLinkPreview(Object object) {
        try {
            SocketLinkPreviewThumbnailEvent socketLinkPreviewMessageEvent =
                    objectMapper.readValue(object.toString(), SocketLinkPreviewThumbnailEvent.class);

            SocketLinkPreviewThumbnailEvent.Data data = socketLinkPreviewMessageEvent.getData();
            ResMessages.LinkPreview linkPreview = data.getLinkPreview();

            if (AccountRepository.getRepository().getSelectedTeamId() != data.getTeamId()) {
                return;
            }

            int messageId = data.getMessageId();

            ResMessages.TextMessage textMessage =
                    MessageRepository.getRepository().getTextMessage(messageId);
            textMessage.linkPreview = linkPreview;
            MessageRepository.getRepository().upsertTextMessage(textMessage);

            postEvent(new LinkPreviewUpdateEvent(messageId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean updateLinkPreview(int teamId, int messageId) {
        ResMessages.OriginalMessage message =
                RequestApiManager.getInstance().getMessage(teamId, messageId);

        if (message instanceof ResMessages.TextMessage) {
            ResMessages.TextMessage textMessage = (ResMessages.TextMessage) message;
            ResMessages.LinkPreview linkPreview = textMessage.linkPreview;
            if (linkPreview != null) {
                MessageRepository.getRepository().upsertTextMessage(textMessage);
                return true;
            }
        }
        return false;
    }

    public void refreshAnnouncement(Object object) {
        try {
            EntityClientManager jandiEntityClient = EntityClientManager_.getInstance_(context);
            ResLeftSideMenu totalEntitiesInfo = jandiEntityClient.getTotalEntitiesInfo();
            LeftSideMenuRepository.getRepository().upsertLeftSideMenu(totalEntitiesInfo);
            EntityManager.getInstance().refreshEntity();

            SocketAnnouncementEvent socketAnnouncementEvent =
                    objectMapper.readValue(object.toString(), SocketAnnouncementEvent.class);

            postEvent(socketAnnouncementEvent);
        } catch (RetrofitError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateTopicPushSubscribe(Object object) {
        try {
            EntityClientManager jandiEntityClient = EntityClientManager_.getInstance_(context);
            ResLeftSideMenu totalEntitiesInfo = jandiEntityClient.getTotalEntitiesInfo();
            LeftSideMenuRepository.getRepository().upsertLeftSideMenu(totalEntitiesInfo);
            EntityManager.getInstance().refreshEntity();

            SocketTopicPushEvent socketTopicPushEvent =
                    objectMapper.readValue(object.toString(), SocketTopicPushEvent.class);

            postEvent(socketTopicPushEvent);
        } catch (RetrofitError e) {
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
                .subscribe(o -> {
                    EntityClientManager entityClientManager = EntityClientManager_.getInstance_(context);
                    try {
                        ResLeftSideMenu entitiesInfo = entityClientManager.getTotalEntitiesInfo();
                        LeftSideMenuRepository.getRepository().upsertLeftSideMenu(entitiesInfo);
                        int totalUnreadCount = BadgeUtils.getTotalUnreadCount(entitiesInfo);
                        BadgeCountRepository badgeCountRepository = BadgeCountRepository.getRepository();
                        badgeCountRepository.upsertBadgeCount(entitiesInfo.team.id, totalUnreadCount);
                        BadgeUtils.setBadge(context, badgeCountRepository.getTotalBadgeCount());

                        EntityManager.getInstance().refreshEntity();

                        postEvent(new RetrieveTopicListEvent());

                    } catch (RetrofitError e) {
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
                .subscribe(event -> {
                    ResAccountInfo resAccountInfo = RequestApiManager.getInstance().getAccountInfoByMainRest();
                    AccountRepository.getRepository().upsertAccountAllInfo(resAccountInfo);
                    postEvent(new MessageOfOtherTeamEvent());
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

    public ResAccessToken refreshToken() throws RetrofitError {
        ResAccessToken accessToken = AccessTokenRepository.getRepository().getAccessToken();
        String jandiRefreshToken = accessToken.getRefreshToken();
        ReqAccessToken refreshReqToken = ReqAccessToken.createRefreshReqToken(jandiRefreshToken);
        return new JacksonConvertedSimpleRestApiClient().getAccessTokenByMainRest(refreshReqToken);
    }

    public void createFile(Object object) {
        try {
            SocketFileEvent socketFileEvent =
                    objectMapper.readValue(object.toString(), SocketFileEvent.class);
            postEvent(new CreateFileEvent(socketFileEvent.getTeamId(), socketFileEvent.getFile().getId()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRefreshEntityObserver() {
        entitySocketModel.stopObserver();
    }

    public void refreshUnstarredMessage(Object object) {
        try {
            SocketMessageStarredEvent socketFileEvent
                    = objectMapper.readValue(object.toString(), SocketMessageStarredEvent.class);

            MessageRepository.getRepository().updateStarred(socketFileEvent.getStarredInfo()
                    .getMessageId(), false);

            postEvent(new SocketMessageStarEvent(socketFileEvent.getStarredInfo().getMessageId(), false));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshStarredMessage(Object object) {
        try {
            SocketMessageStarredEvent socketFileEvent
                    = objectMapper.readValue(object.toString(), SocketMessageStarredEvent.class);

            MessageRepository.getRepository().updateStarred(socketFileEvent.getStarredInfo()
                    .getMessageId(), true);

            postEvent(new SocketMessageStarEvent(socketFileEvent.getStarredInfo().getMessageId(), true));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //todo
    public void refreshTopicFolder(Object object) {
        try {
            SocketTopicFolderEvent socketTopicFolderEvent
                    = objectMapper.readValue(object.toString(), SocketTopicFolderEvent.class);

            postEvent(socketTopicFolderEvent);


            //todo
//            ResFolder resFolder = new ResFolder();
//
//            if (socketTopicFolderEvent.getEvent().equals("folder_create")) {
//
//            } else if (socketTopicFolderEvent.getEvent().equals("folder_update")) {
//
//            } else if (socketTopicFolderEvent.getEvent().equals("folder_deleted")) {
//
//            } else if (socketTopicFolderEvent.getEvent().equals("folder_item_created")) {
//
//            } else if (socketTopicFolderEvent.getEvent().equals("folder_item_deleted")) {
//
//            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshLeaveMember(Object object) {
        try {
            SocketTeamLeaveEvent socketTeamLeaveEvent = objectMapper.readValue(object.toString(), SocketTeamLeaveEvent.class);
            TeamLeaveEvent teamLeaveEvent = new TeamLeaveEvent(socketTeamLeaveEvent.getTeam().getId(), socketTeamLeaveEvent.getMember().getId());

            int leaveMemberId = socketTeamLeaveEvent.getMember().getId();
            int myId = EntityManager.getInstance().getMe().getId();

            if (leaveMemberId != myId) {
                refreshEntity(teamLeaveEvent, false);
            } else {
                Observable.just(socketTeamLeaveEvent)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(leaveEvent -> {
                            String teamName = JandiApplication.getContext().getString(R.string.jandi_your_access_disabled, leaveEvent.getTeam().getName());
                            ColoredToast.showError(JandiApplication.getContext(), teamName);
                        });
                AccountRepository.getRepository().removeSelectedTeamInfo();
                AccountHomeActivity_.intent(JandiApplication.getContext())
                        .flags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
                        .start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshThrowOut(Object object) {
        try {
            SocketTopicThrowoutEvent event =
                    objectMapper.readValue(object.toString(), SocketTopicThrowoutEvent.class);

            SocketTopicThrowoutEvent.Data data = event.getData();

            postEvent(new TopicThrowoutEvent(data.getRoomId(), data.getTeamId()));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
