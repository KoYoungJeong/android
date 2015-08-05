package com.tosslab.jandi.app.services.socket;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.events.entities.MemberStarredEvent;
import com.tosslab.jandi.app.events.entities.ProfileChangeEvent;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.events.entities.TopicDeleteEvent;
import com.tosslab.jandi.app.events.entities.TopicInfoUpdateEvent;
import com.tosslab.jandi.app.events.files.CreateFileEvent;
import com.tosslab.jandi.app.events.files.DeleteFileEvent;
import com.tosslab.jandi.app.events.files.FileCommentRefreshEvent;
import com.tosslab.jandi.app.events.files.ShareFileEvent;
import com.tosslab.jandi.app.events.messages.SocketMessageStarEvent;
import com.tosslab.jandi.app.events.team.TeamInfoChangeEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.EntityClientManager_;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.socket.domain.ConnectTeam;
import com.tosslab.jandi.app.network.spring.JacksonMapper;
import com.tosslab.jandi.app.services.socket.to.SocketAnnouncementEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileCommentEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileDeleteEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileUnsharedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketLinkPreviewMessageEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMemberEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMemberProfileEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageStarredEvent;
import com.tosslab.jandi.app.services.socket.to.SocketRoomMarkerEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicPushEvent;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.UserAgentUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;
import rx.Subscription;
import rx.subjects.PublishSubject;

/**
 * Created by Steve SeongUg Jung on 15. 4. 6..
 */
public class JandiSocketServiceModel {
    public static final String TAG = JandiSocketServiceModel.class.getSimpleName();

    private final Context context;
    private final ObjectMapper objectMapper;
    private PublishSubject<SocketRoomMarkerEvent> markerPublishSubject;
    private Subscription markerSubscribe;
    private EntitySocketModel entitySocketModel;

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

        EntityManager entityManager = EntityManager.getInstance(context);
        FormattedEntity me = entityManager.getMe();

        if (me == null) {
            return null;
        }

        String token = JandiPreference.getAccessToken(context);
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

    public void refreshEntity(boolean postRetrieveEvent
            , String socketMessageEventContent
            , Object event, boolean parseUpdate) {

        entitySocketModel.refreshEntity(new EntitySocketModel.EntityRefreshEventWrapper
                (postRetrieveEvent, parseUpdate, socketMessageEventContent, event));
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

            postEvent(new DeleteFileEvent(socketFileEvent.getFile().getId()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshFileComment(Object object) {
        try {
            SocketFileEvent socketFileEvent =
                    objectMapper.readValue(object.toString(), SocketFileCommentEvent.class);
            postEvent(new FileCommentRefreshEvent(socketFileEvent.getFile().getId()));
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

            refreshEntity(new TopicDeleteEvent(socketTopicEvent.getTopic().getId()), true);
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

            postEvent(new ShareFileEvent(socketFileEvent.getFile().getId()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void updateMarker(Object object) {
        try {
            SocketRoomMarkerEvent socketRoomMarkerEvent =
                    objectMapper.readValue(object.toString(), SocketRoomMarkerEvent.class);
            postEvent(socketRoomMarkerEvent);
            if (EntityManager.getInstance(context).getMe().getId()
                    == socketRoomMarkerEvent.getMarker().getMemberId()) {
                markerPublishSubject.onNext(socketRoomMarkerEvent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateLinkPreviewMessage(Object object) {
        try {
            SocketLinkPreviewMessageEvent socketLinkPreviewMessageEvent =
                    objectMapper.readValue(object.toString(), SocketLinkPreviewMessageEvent.class);

            postEvent(socketLinkPreviewMessageEvent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshAnnouncement(Object object) {
        try {
            EntityClientManager jandiEntityClient = EntityClientManager_.getInstance_(context);
            ResLeftSideMenu totalEntitiesInfo = jandiEntityClient.getTotalEntitiesInfo();
            LeftSideMenuRepository.getRepository().upsertLeftSideMenu(totalEntitiesInfo);
            EntityManager.getInstance(context).refreshEntity(totalEntitiesInfo);

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
            EntityManager.getInstance(context).refreshEntity(totalEntitiesInfo);

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
        markerSubscribe = markerPublishSubject.throttleWithTimeout(1000 * 10, TimeUnit.MILLISECONDS)
                .onBackpressureBuffer()
                .subscribe(o -> {
                    EntityClientManager entityClientManager = EntityClientManager_.getInstance_(context);
                    try {
                        ResLeftSideMenu entitiesInfo = entityClientManager.getTotalEntitiesInfo();
                        LeftSideMenuRepository.getRepository().upsertLeftSideMenu(entitiesInfo);
                        int totalUnreadCount = BadgeUtils.getTotalUnreadCount(entitiesInfo);
                        JandiPreference.setBadgeCount(context, totalUnreadCount);
                        BadgeUtils.setBadge(context, totalUnreadCount);

                        EntityManager.getInstance(context).refreshEntity(entitiesInfo);

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

    public boolean refreshToken() {
        try {
            String jandiRefreshToken = JandiPreference.getRefreshToken(context);
            ResAccessToken token = RequestApiManager.getInstance().getAccessTokenByMainRest(ReqAccessToken.createRefreshReqToken(jandiRefreshToken));
            return token != null;
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage());
            return false;
        }
    }

    public void createFile(Object object) {
        try {
            SocketFileEvent socketFileEvent =
                    objectMapper.readValue(object.toString(), SocketFileEvent.class);
            postEvent(new CreateFileEvent(socketFileEvent.getFile().getId()));
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
                    .getMessageId(), true);

            postEvent(new SocketMessageStarEvent(socketFileEvent.getStarredInfo().getMessageId(), true));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshStarredMessage(Object object) {
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
}
