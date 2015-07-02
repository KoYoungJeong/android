package com.tosslab.jandi.app.services.socket;

import android.content.Context;
import android.content.Intent;
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
import com.tosslab.jandi.app.events.team.TeamInfoChangeEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.EntityClientManager_;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.socket.domain.ConnectTeam;
import com.tosslab.jandi.app.network.spring.JacksonMapper;
import com.tosslab.jandi.app.services.BadgeHandleService;
import com.tosslab.jandi.app.services.socket.to.SocketAnnouncementEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileCommentEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileDeleteEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileUnsharedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMemberEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMemberProfileEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.services.socket.to.SocketRoomMarkerEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicEvent;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.parse.ParseUpdateUtil;

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
    private Subscription subscribe;

    public JandiSocketServiceModel(Context context) {

        this.context = context;
        this.objectMapper = JacksonMapper.getInstance().getObjectMapper();
    }


    public ConnectTeam getConnectTeam() {
        ResAccountInfo.UserTeam selectedTeamInfo =
                JandiAccountDatabaseManager.getInstance(context).getSelectedTeamInfo();

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
                selectedTeamInfo.getTeamId(), selectedTeamInfo.getName(),
                selectedTeamInfo.getMemberId(), me.getName());
    }

    public void refreshEntity() {
        refreshEntity(true, null);
    }

    public void refreshEntity(boolean postRetrieveEvent, String socketMessageEventContent) {
        Intent intent = new Intent(context, BadgeHandleService.class);
        intent.putExtra(BadgeHandleService.KEY_POST_RETRIEVE_TOPIC_EVENT, postRetrieveEvent);
        intent.putExtra(BadgeHandleService.KEY_SOCKET_MESSAGE_EVENT, socketMessageEventContent);
        context.startService(intent);
    }

    public void refreshAccountInfo() {
        try {
            ResAccountInfo resAccountInfo = RequestApiManager.getInstance().getAccountInfoByMainRest();
            JandiAccountDatabaseManager.getInstance(context).upsertAccountAllInfo(resAccountInfo);
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
                refreshEntity(true, content);
            } else {
                postEvent(socketMessageEvent);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshTopicState(Object object) {
        refreshEntity();
        try {
            SocketTopicEvent socketTopicEvent =
                    objectMapper.readValue(object.toString(), SocketTopicEvent.class);
            postEvent(new TopicInfoUpdateEvent(socketTopicEvent.getTopic().getId()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshChatCloseListener(Object object) {
        refreshEntity();
    }

    public void refreshMemberProfile(Object object) {
        refreshEntity();
        try {
            SocketMemberProfileEvent socketTopicEvent =
                    objectMapper.readValue(object.toString(), SocketMemberProfileEvent.class);
            postEvent(new ProfileChangeEvent(socketTopicEvent.getMember().id));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void refreshTopicDelete(Object object) {
        refreshEntity();
        try {
            SocketTopicEvent socketTopicEvent =
                    objectMapper.readValue(object.toString(), SocketTopicEvent.class);
            postEvent(new TopicDeleteEvent(socketTopicEvent.getTopic().getId()));

            ParseUpdateUtil.updateParseWithoutSelectedTeam(context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshMemberStarred(Object object) {
        refreshEntity();
        try {
            SocketMemberEvent socketMemberEvent =
                    objectMapper.readValue(object.toString(), SocketMemberEvent.class);
            postEvent(new MemberStarredEvent(socketMemberEvent.getMember().getId()));
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

    public void refreshAnnouncement(Object object) {
        try {
            EntityClientManager jandiEntityClient = EntityClientManager_.getInstance_(context);
            ResLeftSideMenu totalEntitiesInfo = jandiEntityClient.getTotalEntitiesInfo();
            JandiEntityDatabaseManager.getInstance(context).upsertLeftSideMenu(totalEntitiesInfo);
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

    private <T> void postEvent(T object) {
        EventBus eventBus = EventBus.getDefault();
        if (eventBus.hasSubscriberForEvent(object.getClass())) {
            eventBus.post(object);
        }
    }

    public void startMarkerObserver() {
        markerPublishSubject = PublishSubject.create();
        subscribe = markerPublishSubject.throttleLast(1000 * 10, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    EntityClientManager entityClientManager = EntityClientManager_.getInstance_(context);
                    try {
                        ResLeftSideMenu entitiesInfo = entityClientManager.getTotalEntitiesInfo();
                        JandiEntityDatabaseManager.getInstance(context).upsertLeftSideMenu(entitiesInfo);
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
        if (!subscribe.isUnsubscribed()) {
            subscribe.unsubscribe();
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
}
