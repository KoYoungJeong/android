package com.tosslab.jandi.app.ui.message.detail.model;

import android.content.Context;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ReqUpdateTopicPushSubscribe;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.json.JSONException;

import retrofit.RetrofitError;

@EBean
public class TopicDetailModel {

    @Bean
    EntityClientManager entityClientManager;


    public String getTopicName(Context context, int entityId) {

        return EntityManager.getInstance(context).getEntityById(entityId).getName();
    }

    public String getTopicDescription(Context context, int entityId) {
        FormattedEntity entity = EntityManager.getInstance(context).getEntityById(entityId);
        ResLeftSideMenu.Entity rawEntity = entity.getEntity();
        if (entity.isPublicTopic()) {
            return ((ResLeftSideMenu.Channel) rawEntity).description;
        } else if (entity.isPrivateGroup()) {
            return ((ResLeftSideMenu.PrivateGroup) rawEntity).description;
        } else {
            return "";
        }
    }

    public int getTopicMemberCount(Context context, int entityId) {
        FormattedEntity entity = EntityManager.getInstance(context).getEntityById(entityId);
        return entity.getMemberCount();
    }

    public boolean isStarred(Context context, int entityId) {

        return EntityManager.getInstance(context).getEntityById(entityId).isStarred;
    }

    public boolean isOwner(Context context, int entityId) {
        return EntityManager.getInstance(context).isMyTopic(entityId);

    }

    public void deleteTopic(int entityId, int entityType) throws RetrofitError {
        if (entityType == JandiConstants.TYPE_PUBLIC_TOPIC) {
            entityClientManager.deleteChannel(entityId);
        } else {
            entityClientManager.deletePrivateGroup(entityId);
        }
    }

    public int getEntityType(Context context, int entityId) {

        FormattedEntity entity = EntityManager.getInstance(context).getEntityById(entityId);
        if (entity.isPublicTopic()) {
            return JandiConstants.TYPE_PUBLIC_TOPIC;
        } else if (entity.isPrivateGroup()) {
            return JandiConstants.TYPE_PRIVATE_TOPIC;
        } else {
            return JandiConstants.TYPE_DIRECT_MESSAGE;
        }
    }

    public void trackDeletingEntity(Context context, int entityType) {
        String distictId = EntityManager.getInstance(context).getDistictId();
        try {
            MixpanelMemberAnalyticsClient
                    .getInstance(context, distictId)
                    .trackDeletingEntity(entityType == JandiConstants.TYPE_PUBLIC_TOPIC);
        } catch (JSONException e) {
        }
    }

    public void modifyTopicName(int entityType, int entityId, String inputName) throws RetrofitError {
        if (entityType == JandiConstants.TYPE_PUBLIC_TOPIC) {
            entityClientManager.modifyChannelName(entityId, inputName);
        } else if (entityType == JandiConstants.TYPE_PRIVATE_TOPIC) {
            entityClientManager.modifyPrivateGroupName(entityId, inputName);
        }
    }

    public void updatePushStatus(int teamId, int entityId, boolean pushOn) throws RetrofitError {
        ReqUpdateTopicPushSubscribe req = new ReqUpdateTopicPushSubscribe(pushOn);
        RequestApiManager.getInstance().updateTopicPushSubscribe(teamId, entityId, req);
    }

    public boolean isPushOn(Context context, int entityId) {
        FormattedEntity entity = EntityManager.getInstance(context).getEntityById(entityId);
        return entity.isTopicPushOn;
    }

    public void trackChangingEntityName(Context context, int entityType) {

        try {
            String distictId = EntityManager.getInstance(context).getDistictId();

            MixpanelMemberAnalyticsClient
                    .getInstance(context, distictId)
                    .trackChangingEntityName(entityType == JandiConstants.TYPE_PUBLIC_TOPIC);
        } catch (JSONException e) {
        }
    }

}
