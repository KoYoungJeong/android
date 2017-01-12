package com.tosslab.jandi.app.ui.maintab.tabs.topic.dialog.model;

import android.preference.PreferenceManager;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.repositories.info.TopicRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.chat.ChatApi;
import com.tosslab.jandi.app.network.client.rooms.RoomsApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqUpdateTopicPushSubscribe;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.services.socket.to.SocketTopicPushEvent;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import javax.inject.Inject;

import dagger.Lazy;
import de.greenrobot.event.EventBus;
import rx.Completable;
import rx.schedulers.Schedulers;


public class EntityMenuDialogModel {

    EntityClientManager entityClientManager;

    Lazy<ChatApi> chatApi;
    Lazy<RoomsApi> roomsApi;

    @Inject
    public EntityMenuDialogModel(Lazy<ChatApi> chatApi,
                                 Lazy<RoomsApi> roomsApi,
                                 EntityClientManager entityClientManager) {
        this.chatApi = chatApi;
        this.roomsApi = roomsApi;
        this.entityClientManager = entityClientManager;
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

    public ResCommon requestDeleteChat(long entityId) throws RetrofitException {
        long teamId = TeamInfoLoader.getInstance().getTeamId();

        return chatApi.get().deleteChat(teamId, entityId);
    }

    public boolean isDefaultTopic(long entityId) {
        return TeamInfoLoader.getInstance().isDefaultTopic(entityId);
    }

    public void updateNotificationOnOff(long entityId, boolean isTopicPushOn) {
        if (!NetworkCheckUtil.isConnected()) {
            EventBus.getDefault().post(new SocketTopicPushEvent());
        } else {

            Completable.fromCallable(() -> {
                long teamId = TeamInfoLoader.getInstance().getTeamId();
                updatePushStatus(teamId, entityId, isTopicPushOn);
                TopicRepository.getInstance().updatePushSubscribe(entityId, isTopicPushOn);
                return true;
            }).subscribeOn(Schedulers.io())
                    .subscribe(() -> {
                    }, Throwable::printStackTrace);

        }


    }

    public void updatePushStatus(long teamId, long entityId, boolean pushOn) throws RetrofitException {
        ReqUpdateTopicPushSubscribe req = new ReqUpdateTopicPushSubscribe(pushOn);
        roomsApi.get().updateTopicPushSubscribe(teamId, entityId, req);
        AnalyticsValue.Label label;
        if (pushOn) {
            label = AnalyticsValue.Label.On;
        } else {
            label = AnalyticsValue.Label.Off;
        }
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab,
                AnalyticsValue.Action.TopicSubMenu_Notification,
                label);
    }

    public boolean isPushOn(long topicId) {
        return TeamInfoLoader.getInstance().isPushSubscribe(topicId);
    }

    public boolean isJandiBot(long id) {
        return TeamInfoLoader.getInstance().isJandiBot(id);
    }

    public boolean isGlobalPushOff() {
        return PreferenceManager.getDefaultSharedPreferences(JandiApplication.getContext())
                .getBoolean("setting_push_auto_alarm", true);
    }
}
