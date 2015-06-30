package com.tosslab.jandi.app.ui.maintab.topic.dialog.model;

import android.content.Context;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.network.manager.RequestManager;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.maintab.chat.model.ChatDeleteRequest;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.json.JSONException;

/**
 * Created by Steve SeongUg Jung on 15. 1. 7..
 */
@EBean
public class EntityMenuDialogModel {

    @RootContext
    Context context;

    @Bean
    JandiEntityClient jandiEntityClient;

    public FormattedEntity getEntity(int entityId) {
        return EntityManager.getInstance(context).getEntityById(entityId);
    }

    public void requestStarred(int entityId) throws JandiNetworkException {
        jandiEntityClient.enableFavorite(entityId);
    }

    public void requestUnstarred(int entityId) throws JandiNetworkException {
        jandiEntityClient.disableFavorite(entityId);
    }

    public void requestLeaveEntity(int entityId, boolean publicTopic) throws JandiNetworkException {

        if (publicTopic) {
            jandiEntityClient.leaveChannel(entityId);
        } else {
            jandiEntityClient.leavePrivateGroup(entityId);
        }

    }

    public void refreshEntities() throws JandiNetworkException {
        ResLeftSideMenu totalEntitiesInfo = jandiEntityClient.getTotalEntitiesInfo();
        JandiEntityDatabaseManager.getInstance(context).upsertLeftSideMenu(totalEntitiesInfo);
        int totalUnreadCount = BadgeUtils.getTotalUnreadCount(totalEntitiesInfo);
        JandiPreference.setBadgeCount(context, totalUnreadCount);
        BadgeUtils.setBadge(context, totalUnreadCount);
        EntityManager.getInstance(context).refreshEntity(context);
    }

    public ResCommon requestDeleteChat(int memberId, int entityId) throws JandiNetworkException {
        return RequestManager.newInstance(context, ChatDeleteRequest.create(context, memberId, entityId)).request();
    }

    public void leaveEntity(boolean publicTopic) {
        String distictId = EntityManager.getInstance(context).getDistictId();
        try {
            MixpanelMemberAnalyticsClient
                    .getInstance(context, distictId)
                    .trackLeavingEntity(publicTopic);
        } catch (JSONException e) {
        }
    }

    public boolean isDefaultTopic(int entityId) {
        return EntityManager.getInstance(context).getDefaultTopicId() == entityId;
    }
}
