package com.tosslab.jandi.app.ui.maintab.tabs.topic.views.create.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.local.orm.repositories.info.TopicRepository;
import com.tosslab.jandi.app.network.client.privatetopic.GroupApi;
import com.tosslab.jandi.app.network.client.publictopic.ChannelApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.start.Topic;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import javax.inject.Inject;

import dagger.Lazy;


public class TopicCreateModel {


    private final Lazy<ChannelApi> channelApi;
    private final Lazy<GroupApi> groupApi;

    @Inject
    TopicCreateModel(Lazy<ChannelApi> channelApi, Lazy<GroupApi> groupApi) {
        this.channelApi = channelApi;
        this.groupApi = groupApi;
    }

    public Topic createTopic(String entityName, boolean publicSelected, String topicDescription, boolean isAutojoin) throws RetrofitException {

        final ReqCreateTopic reqCreateTopic = new ReqCreateTopic();
        reqCreateTopic.teamId = TeamInfoLoader.getInstance().getTeamId();
        reqCreateTopic.name = entityName;
        reqCreateTopic.description = topicDescription;
        reqCreateTopic.autoJoin = isAutojoin;

        if (publicSelected) {
            return channelApi.get().createChannel(reqCreateTopic.teamId, reqCreateTopic);
        } else {
            return groupApi.get().createPrivateGroup(reqCreateTopic.teamId, reqCreateTopic);
        }

    }

    public boolean invalideTitle(String topicTitle) {
        return TextUtils.isEmpty(topicTitle) || TextUtils.getTrimmedLength(topicTitle) <= 0;
    }

    public void addTopic(Topic topic) {

        topic.setIsJoined(true);
        topic.setReadLinkId(-1);
        topic.setSubscribe(true);
        TopicRepository.getInstance().addTopic(topic);
    }
}
