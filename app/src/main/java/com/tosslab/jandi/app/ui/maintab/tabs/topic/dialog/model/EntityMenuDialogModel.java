package com.tosslab.jandi.app.ui.maintab.tabs.topic.dialog.model;

import android.preference.PreferenceManager;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.repositories.info.TopicRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.chat.ChatApi;
import com.tosslab.jandi.app.network.client.rooms.RoomsApi;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqUpdateTopicPushSubscribe;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.services.socket.to.SocketTopicPushEvent;
import com.tosslab.jandi.app.team.TeamInfoLoader;
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

    public ResCommon requestDeleteChat(long memberId, long entityId) throws RetrofitException {
        return chatApi.get().deleteChat(memberId, entityId);
    }

    public boolean isDefaultTopic(long entityId) {
        return TeamInfoLoader.getInstance().isDefaultTopic(entityId);
    }

    @Background
    public void updateNotificationOnOff(long entityId, boolean isTopicPushOn) {
        if (!NetworkCheckUtil.isConnected()) {
            EventBus.getDefault().post(new SocketTopicPushEvent());
        } else {

            final long teamId = TeamInfoLoader.getInstance().getTeamId();
            try {
                updatePushStatus(teamId, entityId, isTopicPushOn);
                TopicRepository.getInstance().updatePushSubscribe(entityId, isTopicPushOn);
                TeamInfoLoader.getInstance().refresh();
            } catch (RetrofitException e) {
                e.printStackTrace();
            }
        }


    }

    public void updatePushStatus(long teamId, long entityId, boolean pushOn) throws RetrofitException {
        ReqUpdateTopicPushSubscribe req = new ReqUpdateTopicPushSubscribe(pushOn);
        roomsApi.get().updateTopicPushSubscribe(teamId, entityId, req);
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
