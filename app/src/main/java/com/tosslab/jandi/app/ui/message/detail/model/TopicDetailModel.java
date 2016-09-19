package com.tosslab.jandi.app.ui.message.detail.model;

import android.preference.PreferenceManager;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.EntityClientManager_;
import com.tosslab.jandi.app.network.client.rooms.RoomsApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqUpdateTopicPushSubscribe;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.room.TopicRoom;

import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import rx.Observable;


public class TopicDetailModel {

    private EntityClientManager entityClientManager;
    private Lazy<RoomsApi> roomsApi;

    @Inject
    public TopicDetailModel(Lazy<RoomsApi> roomsApi) {
        this.roomsApi = roomsApi;
        this.entityClientManager = EntityClientManager_.getInstance_(JandiApplication.getContext());
        ;
    }

    public TopicRoom getTopic(long entityId) {
        return TeamInfoLoader.getInstance().getTopic(entityId);
    }

    public String getTopicName(long entityId) {

        return TeamInfoLoader.getInstance().getName(entityId);
    }

    public String getTopicDescription(long entityId) {

        return TeamInfoLoader.getInstance().getTopic(entityId).getDescription();

    }

    public int getTopicMemberCount(long entityId) {
        return TeamInfoLoader.getInstance().getTopic(entityId).getMemberCount();
    }

    public boolean isStarred(long entityId) {

        return TeamInfoLoader.getInstance().isStarred(entityId);
    }

    public boolean isOwner(long entityId) {
        return TeamInfoLoader.getInstance().getTopic(entityId).getCreatorId()
                == TeamInfoLoader.getInstance().getMyId();

    }

    public void deleteTopic(long entityId, int entityType) throws RetrofitException {
        if (entityType == JandiConstants.TYPE_PUBLIC_TOPIC) {
            entityClientManager.deleteChannel(entityId);
        } else {
            entityClientManager.deletePrivateGroup(entityId);
        }
    }

    public int getEntityType(long entityId) {

        if (TeamInfoLoader.getInstance().isTopic(entityId)) {
            if (TeamInfoLoader.getInstance().isPublicTopic(entityId)) {
                return JandiConstants.TYPE_PUBLIC_TOPIC;
            } else {
                return JandiConstants.TYPE_PRIVATE_TOPIC;
            }
        } else {
            return JandiConstants.TYPE_DIRECT_MESSAGE;
        }
    }

    public void modifyTopicName(int entityType, long entityId, String inputName) throws RetrofitException {
        if (entityType == JandiConstants.TYPE_PUBLIC_TOPIC) {
            entityClientManager.modifyChannelName(entityId, inputName);
        } else if (entityType == JandiConstants.TYPE_PRIVATE_TOPIC) {
            entityClientManager.modifyPrivateGroupName(entityId, inputName);
        }
    }

    public void updatePushStatus(long teamId, long entityId, boolean pushOn) throws RetrofitException {
        ReqUpdateTopicPushSubscribe req = new ReqUpdateTopicPushSubscribe(pushOn);
        roomsApi.get().updateTopicPushSubscribe(teamId, entityId, req);
    }

    public boolean isPushOn(long entityId) {
        return TeamInfoLoader.getInstance().getTopic(entityId).isPushSubscribe();
    }

    public boolean isDefaultTopic(long entityId) {
        return TeamInfoLoader.getInstance().getDefaultTopicId() == entityId;
    }

    public boolean isTeamOwner() {
        return TeamInfoLoader.getInstance().getUser(TeamInfoLoader.getInstance().getMyId()).isTeamOwner();
    }


    public int getEnabledTeamMemberCount() {
        List<User> userList = TeamInfoLoader.getInstance().getUserList();

        return Observable.from(userList)
                .filter(user -> !user.isBot())
                .filter(user -> user.isEnabled())
                .count()
                .toBlocking()
                .firstOrDefault(0);

    }

    public boolean isPrivateTopic(long entityId) {
        return TeamInfoLoader.getInstance().isTopic(entityId)
                && !TeamInfoLoader.getInstance().isPublicTopic(entityId);
    }

    public boolean isAutoJoin(long topicId) {
        return TeamInfoLoader.getInstance().getTopic(topicId).isAutoJoin();
    }

    public boolean isStandAlone(long entityId) {
        return TeamInfoLoader.getInstance().getTopic(entityId).getMemberCount() <= 1;
    }

    public void updateAutoJoin(long entityId, boolean autoJoin) throws RetrofitException {
        entityClientManager.modifyChannelAutoJoin(entityId, autoJoin);
    }

    public boolean isOnGlobalPush() {
        return PreferenceManager.getDefaultSharedPreferences(JandiApplication.getContext())
                .getBoolean("setting_push_auto_alarm", true);
    }

    public void updateTopicStatus(long entityId, boolean starred) throws RetrofitException {
        if (starred) {
            entityClientManager.enableFavorite(entityId);
        } else {
            entityClientManager.disableFavorite(entityId);
        }
    }
}
