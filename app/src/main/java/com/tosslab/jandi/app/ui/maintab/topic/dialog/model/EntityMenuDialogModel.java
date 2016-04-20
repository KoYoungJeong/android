package com.tosslab.jandi.app.ui.maintab.topic.dialog.model;

import android.preference.PreferenceManager;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.chat.ChatApi;
import com.tosslab.jandi.app.network.client.rooms.RoomsApi;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ReqUpdateTopicPushSubscribe;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.services.socket.to.SocketTopicPushEvent;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.json.JSONException;

import javax.inject.Inject;

import dagger.Lazy;
import de.greenrobot.event.EventBus;


/**
 * Created by Steve SeongUg Jung on 15. 1. 7..
 */
@EBean
public class EntityMenuDialogModel {

    @Bean
    EntityClientManager entityClientManager;

    @Inject
    Lazy<ChatApi> chatApi;
    @Inject
    Lazy<RoomsApi> roomsApi;

    @AfterInject
    void initObject() {
        DaggerApiClientComponent.create().inject(this);
    }

    public FormattedEntity getEntity(long entityId) {
        return EntityManager.getInstance().getEntityById(entityId);
    }

    public void requestStarred(long entityId) throws RetrofitException {
        entityClientManager.enableFavorite(entityId);
    }

    public void requestUnstarred(long entityId) throws RetrofitException {
        entityClientManager.disableFavorite(entityId);
    }

    public void requestLeaveEntity(long entityId, boolean publicTopic) throws RetrofitException {
        if (publicTopic) {
            entityClientManager.leaveChannel(entityId);
        } else {
            entityClientManager.leavePrivateGroup(entityId);
        }
    }

    public void refreshEntities() throws RetrofitException {
        ResLeftSideMenu totalEntitiesInfo = entityClientManager.getTotalEntitiesInfo();
        LeftSideMenuRepository.getRepository().upsertLeftSideMenu(totalEntitiesInfo);
        int totalUnreadCount = BadgeUtils.getTotalUnreadCount(totalEntitiesInfo);
        BadgeUtils.setBadge(JandiApplication.getContext(), totalUnreadCount);
        EntityManager.getInstance().refreshEntity();
    }

    public ResCommon requestDeleteChat(long memberId, long entityId) throws RetrofitException {
        return chatApi.get().deleteChat(memberId, entityId);
    }

    public void leaveEntity(boolean publicTopic) {
        String distictId = EntityManager.getInstance().getDistictId();
        try {
            MixpanelMemberAnalyticsClient
                    .getInstance(JandiApplication.getContext(), distictId)
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
        try {
            updatePushStatus(teamId, entityId, isTopicPushOn);
        } catch (RetrofitException e) {
            e.printStackTrace();
        }

    }

    public void updatePushStatus(long teamId, long entityId, boolean pushOn) throws RetrofitException {
        ReqUpdateTopicPushSubscribe req = new ReqUpdateTopicPushSubscribe(pushOn);
        roomsApi.get().updateTopicPushSubscribe(teamId, entityId, req);
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
