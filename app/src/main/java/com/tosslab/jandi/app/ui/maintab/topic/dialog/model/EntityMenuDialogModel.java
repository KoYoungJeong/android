package com.tosslab.jandi.app.ui.maintab.topic.dialog.model;

import android.content.Context;
import android.preference.PreferenceManager;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.BadgeCountRepository;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ReqUpdateTopicPushSubscribe;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.services.socket.to.SocketTopicPushEvent;
import com.tosslab.jandi.app.utils.BadgeUtils;
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
public class EntityMenuDialogModel {

    @RootContext
    Context context;

    @Bean
    EntityClientManager entityClientManager;

    public FormattedEntity getEntity(long entityId) {
        return EntityManager.getInstance().getEntityById(entityId);
    }

    public void requestStarred(long entityId) throws RetrofitError {
        entityClientManager.enableFavorite(entityId);
    }

    public void requestUnstarred(long entityId) throws RetrofitError {
        entityClientManager.disableFavorite(entityId);
    }

    public void requestLeaveEntity(long entityId, boolean publicTopic) throws RetrofitError {
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
        BadgeCountRepository badgeCountRepository = BadgeCountRepository.getRepository();
        badgeCountRepository.upsertBadgeCount(EntityManager.getInstance().getTeamId(), totalUnreadCount);
        BadgeUtils.setBadge(context, badgeCountRepository.getTotalBadgeCount());
        EntityManager.getInstance().refreshEntity();
    }

    public ResCommon requestDeleteChat(long memberId, long entityId) throws RetrofitError {
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

    public boolean isDefaultTopic(long entityId) {
        return EntityManager.getInstance().getDefaultTopicId() == entityId;
    }

    @Background
    public void updateNotificationOnOff(long entityId, boolean isTopicPushOn) {
        if (!NetworkCheckUtil.isConnected()) {
            getEntity(entityId).isTopicPushOn = isTopicPushOn;
            EventBus.getDefault().post(new SocketTopicPushEvent());
            return;
        }

        final long teamId = EntityManager.getInstance().getTeamId();
        updatePushStatus(teamId, entityId, isTopicPushOn);

    }

    public void updatePushStatus(long teamId, long entityId, boolean pushOn) throws RetrofitError {
        ReqUpdateTopicPushSubscribe req = new ReqUpdateTopicPushSubscribe(pushOn);
        RequestApiManager.getInstance().updateTopicPushSubscribe(teamId, entityId, req);
    }

    public boolean isPushOn(long entityId) {
        FormattedEntity entity = EntityManager.getInstance().getEntityById(entityId);
        return entity.isTopicPushOn;
    }

    public boolean isBot(long entityId) {
        return EntityManager.getInstance().isBot(entityId);
    }

    public boolean isTopicOwner(long entityId) {
        final EntityManager entityManager = EntityManager.getInstance();
        return entityManager.isTopicOwner(entityId, entityManager.getMe().getId());
    }

    public boolean isGlobalPushOff() {
        return PreferenceManager.getDefaultSharedPreferences(JandiApplication.getContext())
                .getBoolean("setting_push_auto_alarm", true);
    }
}
