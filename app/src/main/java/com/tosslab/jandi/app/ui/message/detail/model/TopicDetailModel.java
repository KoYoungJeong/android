package com.tosslab.jandi.app.ui.message.detail.model;

import android.content.Context;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ReqUpdateTopicPushSubscribe;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.json.JSONException;

import java.util.List;



@EBean
public class TopicDetailModel {

    @Bean
    EntityClientManager entityClientManager;


    public String getTopicName(long entityId) {

        return EntityManager.getInstance().getEntityById(entityId).getName();
    }

    public String getTopicDescription(long entityId) {
        FormattedEntity entity = EntityManager.getInstance().getEntityById(entityId);
        ResLeftSideMenu.Entity rawEntity = entity.getEntity();
        if (entity.isPublicTopic()) {
            return ((ResLeftSideMenu.Channel) rawEntity).description;
        } else if (entity.isPrivateGroup()) {
            return ((ResLeftSideMenu.PrivateGroup) rawEntity).description;
        } else {
            return "";
        }
    }

    public int getTopicMemberCount(long entityId) {
        FormattedEntity entity = EntityManager.getInstance().getEntityById(entityId);
        return entity.getMemberCount();
    }

    public boolean isStarred(long entityId) {

        return EntityManager.getInstance().getEntityById(entityId).isStarred;
    }

    public boolean isOwner(long entityId) {
        return EntityManager.getInstance().isMyTopic(entityId);

    }

    public void deleteTopic(long entityId, int entityType) throws IOException {
        if (entityType == JandiConstants.TYPE_PUBLIC_TOPIC) {
            entityClientManager.deleteChannel(entityId);
        } else {
            entityClientManager.deletePrivateGroup(entityId);
        }
    }

    public int getEntityType(long entityId) {

        FormattedEntity entity = EntityManager.getInstance().getEntityById(entityId);
        if (entity.isPublicTopic()) {
            return JandiConstants.TYPE_PUBLIC_TOPIC;
        } else if (entity.isPrivateGroup()) {
            return JandiConstants.TYPE_PRIVATE_TOPIC;
        } else {
            return JandiConstants.TYPE_DIRECT_MESSAGE;
        }
    }

    public void trackDeletingEntity(Context context, int entityType) {
        String distictId = EntityManager.getInstance().getDistictId();
        try {
            MixpanelMemberAnalyticsClient
                    .getInstance(context, distictId)
                    .trackDeletingEntity(entityType == JandiConstants.TYPE_PUBLIC_TOPIC);
        } catch (JSONException e) {
        }
    }

    public void trackTopicDeleteSuccess(long entityId) {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.TopicDelete)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, true)
                        .property(PropertyKey.TopicId, entityId)
                        .build());
    }

    public void trackTopicDeleteFail(int errorCode) {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.TopicDelete)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, false)
                        .property(PropertyKey.ErrorCode, errorCode)
                        .build());
    }

    public void modifyTopicName(int entityType, long entityId, String inputName) throws IOException {
        if (entityType == JandiConstants.TYPE_PUBLIC_TOPIC) {
            entityClientManager.modifyChannelName(entityId, inputName);
        } else if (entityType == JandiConstants.TYPE_PRIVATE_TOPIC) {
            entityClientManager.modifyPrivateGroupName(entityId, inputName);
        }
    }

    public void updatePushStatus(long teamId, long entityId, boolean pushOn) throws IOException {
        ReqUpdateTopicPushSubscribe req = new ReqUpdateTopicPushSubscribe(pushOn);
        RequestApiManager.getInstance().updateTopicPushSubscribe(teamId, entityId, req);
    }

    public boolean isPushOn(long entityId) {
        FormattedEntity entity = EntityManager.getInstance().getEntityById(entityId);
        return entity.isTopicPushOn;
    }

    public void trackChangingEntityName(Context context, long entityId, int entityType) {

        try {
            String distictId = EntityManager.getInstance().getDistictId();

            MixpanelMemberAnalyticsClient
                    .getInstance(context, distictId)
                    .trackChangingEntityName(entityType == JandiConstants.TYPE_PUBLIC_TOPIC);
        } catch (JSONException e) {
        }

        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.TopicNameChange)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, true)
                        .property(PropertyKey.TopicId, entityId)
                        .build());

    }

    public void trackChangingEntityNameFail(int errorCode) {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.TopicNameChange)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, false)
                        .property(PropertyKey.TopicId, errorCode)
                        .build());

    }

    public void trackTopicStarSuccess(long topicId) {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.TopicStar)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, true)
                        .property(PropertyKey.TopicId, topicId)
                        .build());
    }

    public void trackTopicUnStarSuccess(long topicId) {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.TopicUnStar)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, true)
                        .property(PropertyKey.TopicId, topicId)
                        .build());

    }

    public void trackTopicStarFail(int errorCode) {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.TopicStar)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, false)
                        .property(PropertyKey.ErrorCode, errorCode)
                        .build());
    }

    public void trackTopicUnStarFail(int errorCode) {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.TopicUnStar)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, false)
                        .property(PropertyKey.ErrorCode, errorCode)
                        .build());

    }

    public boolean isDefaultTopic(long entityId) {
        return EntityManager.getInstance().getDefaultTopicId() == entityId;
    }

    public boolean isTeamOwner() {
        return EntityManager.getInstance().getMe().isTeamOwner();
    }


    public int getEnabledTeamMemberCount() {
        List<FormattedEntity> formattedUsers = EntityManager.getInstance().getFormattedUsers();

        int size = formattedUsers.size();
        int total = 0;
        for (int idx = 0; idx < size; idx++) {
            FormattedEntity formattedEntity = formattedUsers.get(idx);
            if (formattedEntity != null
                    && formattedEntity.getUser() != null
                    && TextUtils.equals(formattedEntity.getUser().status, "enabled")) {
                ++total;
            }
        }

        return total;

    }

    public boolean isPrivateTopic(long entityId) {
        return EntityManager.getInstance().getEntityById(entityId).isPrivateGroup();
    }

    public boolean isAutoJoin(long entityId) {
        return EntityManager.getInstance().getEntityById(entityId).isAutoJoin();
    }

    public boolean isStandAlone(long entityId) {
        return EntityManager.getInstance().getEntityById(entityId).getMemberCount() <= 1;
    }

    public void updateAutoJoin(long entityId, boolean autoJoin) {
        long teamId = EntityManager.getInstance().getTeamId();
        entityClientManager.modifyChannelAutoJoin(entityId, autoJoin);
    }

    public boolean isOnGlobalPush() {
        return PreferenceManager.getDefaultSharedPreferences(JandiApplication.getContext())
                .getBoolean("setting_push_auto_alarm", true);
    }
}
