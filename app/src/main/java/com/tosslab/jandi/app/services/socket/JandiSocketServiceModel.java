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
import com.tosslab.jandi.app.events.team.TeamInfoChangeEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.network.client.JandiEntityClient_;
import com.tosslab.jandi.app.network.manager.RequestManager;
import com.tosslab.jandi.app.network.manager.TokenRefreshRequest;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.socket.domain.ConnectTeam;
import com.tosslab.jandi.app.network.spring.JacksonMapper;
import com.tosslab.jandi.app.services.socket.to.SocketFileCommentEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileDeleteEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileUnsharedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMemberEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMemberProfileEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.services.socket.to.SocketRoomMarkerEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicEvent;
import com.tosslab.jandi.app.ui.team.select.model.AccountInfoRequest;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.parse.ParseUpdateUtil;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Subscription;
import rx.subjects.PublishSubject;

/**
 * Created by Steve SeongUg Jung on 15. 4. 6..
 */
public class JandiSocketServiceModel {
    private final Context context;
    private final ObjectMapper objectMapper;
    private PublishSubject<SocketRoomMarkerEvent> markerPublishSubject;
    private Subscription subscribe;

    public JandiSocketServiceModel(Context context) {

        this.context = context;
        this.objectMapper = JacksonMapper.getInstance().getObjectMapper();
    }


    public ConnectTeam getConnectTeam() {
        ResAccountInfo.UserTeam selectedTeamInfo = JandiAccountDatabaseManager.getInstance(context).getSelectedTeamInfo();

        if (selectedTeamInfo == null) {
            return null;
        }

        EntityManager entityManager = EntityManager.getInstance(context);
        FormattedEntity me = entityManager.getMe();

        if (me == null) {
            return null;
        }

        String token = JandiPreference.getAccessToken(context);
        return new ConnectTeam(token, selectedTeamInfo.getTeamId(), selectedTeamInfo.getName(), selectedTeamInfo.getMemberId(), me.getName());

    }

    public void refreshEntity() {
        try {
            JandiEntityClient jandiEntityClient = JandiEntityClient_.getInstance_(context);
            ResLeftSideMenu totalEntitiesInfo = jandiEntityClient.getTotalEntitiesInfo();
            JandiEntityDatabaseManager.getInstance(context).upsertLeftSideMenu(totalEntitiesInfo);
            int totalUnreadCount = BadgeUtils.getTotalUnreadCount(totalEntitiesInfo);
            JandiPreference.setBadgeCount(context, totalUnreadCount);
            BadgeUtils.setBadge(context, totalUnreadCount);
            EntityManager.getInstance(context).refreshEntity(totalEntitiesInfo);

            postEvent(new RetrieveTopicListEvent());

            ParseUpdateUtil.updateParseWithoutSelectedTeam(context);

        } catch (JandiNetworkException e) {
            e.printStackTrace();
        }
    }

    public void refreshAccountInfo() {
        try {
            AccountInfoRequest accountInfoRequest = AccountInfoRequest.create(context);
            RequestManager<ResAccountInfo> requestManager = RequestManager.newInstance(context, accountInfoRequest);
            ResAccountInfo resAccountInfo = requestManager.request();
            JandiAccountDatabaseManager.getInstance(context).upsertAccountAllInfo(resAccountInfo);

            postEvent(new TeamInfoChangeEvent());

        } catch (JandiNetworkException e) {
            e.printStackTrace();
        }


    }

    public void deleteFile(Object object) {
        try {
            SocketFileEvent socketFileEvent = objectMapper.readValue(object.toString(), SocketFileDeleteEvent.class);

            postEvent(new DeleteFileEvent(socketFileEvent.getFile().getId()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshFileComment(Object object) {
        try {
            SocketFileEvent socketFileEvent = objectMapper.readValue(object.toString(), SocketFileCommentEvent.class);
            postEvent(new FileCommentRefreshEvent(socketFileEvent.getFile().getId()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void refreshMessage(Object object) {
        try {
            SocketMessageEvent socketMessageEvent = objectMapper.readValue(object.toString(), SocketMessageEvent.class);

            if (TextUtils.equals(socketMessageEvent.getMessageType(), "topic_leave") ||
                    TextUtils.equals(socketMessageEvent.getMessageType(), "topic_join") ||
                    TextUtils.equals(socketMessageEvent.getMessageType(), "topic_invite")) {
                refreshEntity();
            }

            postEvent(socketMessageEvent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshTopicState(Object object) {
        refreshEntity();
        try {
            SocketTopicEvent socketTopicEvent = objectMapper.readValue(object.toString(), SocketTopicEvent.class);
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
            SocketMemberProfileEvent socketTopicEvent = objectMapper.readValue(object.toString(), SocketMemberProfileEvent.class);
            postEvent(new ProfileChangeEvent(socketTopicEvent.getMember().id));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void refreshTopicDelete(Object object) {
        refreshEntity();
        try {
            SocketTopicEvent socketTopicEvent = objectMapper.readValue(object.toString(), SocketTopicEvent.class);
            postEvent(new TopicDeleteEvent(socketTopicEvent.getTopic().getId()));

            ParseUpdateUtil.updateParseWithoutSelectedTeam(context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshMemberStarred(Object object) {
        refreshEntity();
        try {
            SocketMemberEvent socketMemberEvent = objectMapper.readValue(object.toString(), SocketMemberEvent.class);
            postEvent(new MemberStarredEvent(socketMemberEvent.getMember().getId()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unshareFile(Object object) {
        try {
            SocketFileEvent socketFileEvent = objectMapper.readValue(object.toString(), SocketFileUnsharedEvent.class);

            postEvent(new ShareFileEvent(socketFileEvent.getFile().getId()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void updateMarker(Object object) {
        try {
            SocketRoomMarkerEvent socketRoomMarkerEvent = objectMapper.readValue(object.toString(), SocketRoomMarkerEvent.class);
            postEvent(socketRoomMarkerEvent);
            if (EntityManager.getInstance(context).getMe().getId() == socketRoomMarkerEvent.getMarker().getMemberId()) {
                markerPublishSubject.onNext(socketRoomMarkerEvent);
            }
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
                    JandiEntityClient jandiEntityClient = JandiEntityClient_.getInstance_(context);
                    try {
                        ResLeftSideMenu entitiesInfo = jandiEntityClient.getTotalEntitiesInfo();
                        JandiEntityDatabaseManager.getInstance(context).upsertLeftSideMenu(entitiesInfo);
                        int totalUnreadCount = BadgeUtils.getTotalUnreadCount(entitiesInfo);
                        JandiPreference.setBadgeCount(context, totalUnreadCount);
                        BadgeUtils.setBadge(context, totalUnreadCount);

                        EntityManager.getInstance(context).refreshEntity(entitiesInfo);

                        postEvent(new RetrieveTopicListEvent());

                    } catch (JandiNetworkException e) {
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
            ResAccessToken token = new TokenRefreshRequest(context, JandiPreference.getRefreshToken(context)).request();
            return token != null;
        } catch (Exception e) {
            return false;
        }
    }

    public void createFile(Object object) {
        try {
            SocketFileEvent socketFileEvent = objectMapper.readValue(object.toString(), SocketFileEvent.class);
            postEvent(new CreateFileEvent(socketFileEvent.getFile().getId()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
