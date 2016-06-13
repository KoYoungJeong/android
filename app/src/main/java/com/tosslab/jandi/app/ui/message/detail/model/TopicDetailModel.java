package com.tosslab.jandi.app.ui.message.detail.model;

import android.content.Context;
import android.preference.PreferenceManager;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.rooms.RoomsApi;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqUpdateTopicPushSubscribe;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.json.JSONException;

import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import rx.Observable;


@EBean
public class TopicDetailModel {

    @Bean
    EntityClientManager entityClientManager;

    @Inject
    Lazy<RoomsApi> roomsApi;

    @AfterInject
    void initObject() {
        DaggerApiClientComponent.create().inject(this);
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

    public void trackTopicDeleteSuccess(long entityId) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(Event.TopicDelete)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, true)
                .property(PropertyKey.TopicId, entityId)
                .build());
    }

    public void trackTopicDeleteFail(int errorCode) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(Event.TopicDelete)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, false)
                .property(PropertyKey.ErrorCode, errorCode)
                .build());
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

    public void trackChangingEntityName(Context context, long entityId, int entityType) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(Event.TopicNameChange)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, true)
                .property(PropertyKey.TopicId, entityId)
                .build());

    }

    public void trackChangingEntityNameFail(int errorCode) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(Event.TopicNameChange)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, false)
                .property(PropertyKey.TopicId, errorCode)
                .build());

    }

    public void trackTopicStarSuccess(long topicId) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(Event.TopicStar)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, true)
                .property(PropertyKey.TopicId, topicId)
                .build());
    }

    public void trackTopicUnStarSuccess(long topicId) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(Event.TopicUnStar)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, true)
                .property(PropertyKey.TopicId, topicId)
                .build());

    }

    public void trackTopicStarFail(int errorCode) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(Event.TopicStar)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, false)
                .property(PropertyKey.ErrorCode, errorCode)
                .build());
    }

    public void trackTopicUnStarFail(int errorCode) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(Event.TopicUnStar)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, false)
                .property(PropertyKey.ErrorCode, errorCode)
                .build());

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

    public boolean isAutoJoin(long entityId) {
        return TeamInfoLoader.getInstance().getTopic(entityId).isAutoJoin();
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
}
