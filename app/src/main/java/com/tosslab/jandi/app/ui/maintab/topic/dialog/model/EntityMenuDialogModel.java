package com.tosslab.jandi.app.ui.maintab.topic.dialog.model;

import android.content.Context;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.push.model.PushOnOffModel;
import com.tosslab.jandi.app.services.socket.to.SocketTopicPushEvent;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.json.JSONException;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

/**
 * Created by Steve SeongUg Jung on 15. 1. 7..
 */
@EBean
public class EntityMenuDialogModel extends PushOnOffModel {

    @RootContext
    Context context;

    @Bean
    EntityClientManager entityClientManager;

    public FormattedEntity getEntity(int entityId) {
        return EntityManager.getInstance().getEntityById(entityId);
    }

    public void requestStarred(int entityId) throws RetrofitError {
        entityClientManager.enableFavorite(entityId);
    }

    public void requestUnstarred(int entityId) throws RetrofitError {
        entityClientManager.disableFavorite(entityId);
    }

    public void requestLeaveEntity(int entityId, boolean publicTopic) throws RetrofitError {
        if (publicTopic) {
            entityClientManager.leaveChannel(entityId);
        } else {
            entityClientManager.leavePrivateGroup(entityId);
        }
    }

    public void refreshEntities() throws RetrofitError {
        ResLeftSideMenu totalEntitiesInfo = entityClientManager.getTotalEntitiesInfo();
        LeftSideMenuRepository.getRepository().upsertLeftSideMenu(totalEntitiesInfo);
        int totalUnreadCount = BadgeUtils.getTotalUnreadCount(totalEntitiesInfo);
        JandiPreference.setBadgeCount(context, totalUnreadCount);
        BadgeUtils.setBadge(context, totalUnreadCount);
        EntityManager.getInstance().refreshEntity();
    }

    public ResCommon requestDeleteChat(int memberId, int entityId) throws RetrofitError {
        return RequestApiManager.getInstance().deleteChatByChatApi(memberId, entityId);
    }

    public void leaveEntity(boolean publicTopic) {
        String distictId = EntityManager.getInstance().getDistictId();
        try {
            MixpanelMemberAnalyticsClient
                    .getInstance(context, distictId)
                    .trackLeavingEntity(publicTopic);
        } catch (JSONException e) {
        }
    }

    public boolean isDefaultTopic(int entityId) {
        return EntityManager.getInstance().getDefaultTopicId() == entityId;
    }

    @Background
    public void updateNotificationOnOff(int entityId, boolean isTopicPushOn) {
        if (!NetworkCheckUtil.isConnected()) {
            getEntity(entityId).isTopicPushOn = isTopicPushOn;
            EventBus.getDefault().post(new SocketTopicPushEvent());
            return;
        }

        final int teamId = EntityManager.getInstance().getTeamId();
        updatePushStatus(teamId, entityId, isTopicPushOn);
    }
}
