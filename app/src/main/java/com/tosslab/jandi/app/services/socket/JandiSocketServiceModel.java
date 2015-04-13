package com.tosslab.jandi.app.services.socket;

import android.content.Context;

import com.tosslab.jandi.app.events.entities.MemberStarredEvent;
import com.tosslab.jandi.app.events.entities.ProfileChangeEvent;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.events.entities.TopicDeleteEvent;
import com.tosslab.jandi.app.events.entities.TopicInfoUpdateEvent;
import com.tosslab.jandi.app.events.files.DeleteFileEvent;
import com.tosslab.jandi.app.events.files.FileCommentRefreshEvent;
import com.tosslab.jandi.app.events.files.ShareFileEvent;
import com.tosslab.jandi.app.events.team.TeamInfoChangeEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.network.client.JandiEntityClient_;
import com.tosslab.jandi.app.network.manager.RequestManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.socket.domain.ConnectTeam;
import com.tosslab.jandi.app.services.socket.to.SocketFileCommentEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileDeleteEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileUnsharedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMemberEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicEvent;
import com.tosslab.jandi.app.ui.team.select.model.AccountInfoRequest;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 4. 6..
 */
public class JandiSocketServiceModel {
    private final Context context;
    private final ObjectMapper objectMapper;

    public JandiSocketServiceModel(Context context) {

        this.context = context;
        this.objectMapper = new ObjectMapper();
    }


    public ConnectTeam getConnectTeam() {
        ResAccountInfo.UserTeam selectedTeamInfo = JandiAccountDatabaseManager.getInstance(context).getSelectedTeamInfo();
        EntityManager entityManager = EntityManager.getInstance(context);
        FormattedEntity me = entityManager.getMe();
        return new ConnectTeam(selectedTeamInfo.getTeamId(), selectedTeamInfo.getName(), selectedTeamInfo.getMemberId(), me.getName());

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

            EventBus.getDefault().post(new RetrieveTopicListEvent());

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

            EventBus.getDefault().post(new TeamInfoChangeEvent());

        } catch (JandiNetworkException e) {
            e.printStackTrace();
        }


    }

    public void deleteFile(Object object) {
        try {
            ObjectMapper objectMapper = this.objectMapper;
            SocketFileEvent socketFileEvent = objectMapper.readValue(object.toString(), SocketFileDeleteEvent.class);

            EventBus.getDefault().post(new DeleteFileEvent(socketFileEvent.getFile().getId()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshFileComment(Object object) {
        ObjectMapper objectMapper = this.objectMapper;
        try {
            SocketFileEvent socketFileEvent = objectMapper.readValue(object.toString(), SocketFileCommentEvent.class);
            EventBus.getDefault().post(new FileCommentRefreshEvent(socketFileEvent.getFile().getId()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void refreshMessage(Object object) {
        try {
            ObjectMapper objectMapper = this.objectMapper;
            SocketMessageEvent socketMessageEvent = objectMapper.readValue(object.toString(), SocketMessageEvent.class);
            EventBus.getDefault().post(socketMessageEvent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshTopicState(Object object) {
        refreshEntity();
        try {
            SocketTopicEvent socketTopicEvent = objectMapper.readValue(object.toString(), SocketTopicEvent.class);
            EventBus.getDefault().post(new TopicInfoUpdateEvent(socketTopicEvent.getTopic().getId()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshChatCloseListener(Object object) {
        refreshEntity();
    }

    public void refreshMemberProfile() {
        refreshEntity();
        EventBus.getDefault().post(new ProfileChangeEvent());
    }

    public void refreshTopicDelete(Object object) {
        refreshEntity();
        try {
            SocketTopicEvent socketTopicEvent = objectMapper.readValue(object.toString(), SocketTopicEvent.class);
            EventBus.getDefault().post(new TopicDeleteEvent(socketTopicEvent.getTopic().getId()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshMemberStarred(Object object) {
        refreshEntity();
        try {
            SocketMemberEvent socketMemberEvent = objectMapper.readValue(object.toString(), SocketMemberEvent.class);
            EventBus.getDefault().post(new MemberStarredEvent(socketMemberEvent.getMember().getId()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unshareFile(Object object) {
        try {
            ObjectMapper objectMapper = this.objectMapper;
            SocketFileEvent socketFileEvent = objectMapper.readValue(object.toString(), SocketFileUnsharedEvent.class);

            EventBus.getDefault().post(new ShareFileEvent(socketFileEvent.getFile().getId()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
