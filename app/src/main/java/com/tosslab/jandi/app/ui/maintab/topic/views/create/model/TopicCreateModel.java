package com.tosslab.jandi.app.ui.maintab.topic.views.create.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.repositories.info.TopicRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.start.Marker;
import com.tosslab.jandi.app.network.models.start.Topic;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.domain.track.FutureTrack;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
@EBean
public class TopicCreateModel {

    @Bean
    EntityClientManager entityClientManager;

    public Topic createTopic(String entityName, boolean publicSelected, String topicDescription, boolean isAutojoin) throws RetrofitException {
        if (publicSelected) {
            return entityClientManager.createPublicTopic(entityName, topicDescription, isAutojoin);
        } else {
            return entityClientManager.createPrivateGroup(entityName, topicDescription, isAutojoin);
        }

    }

    public boolean invalideTitle(String topicTitle) {
        return TextUtils.isEmpty(topicTitle) || TextUtils.getTrimmedLength(topicTitle) <= 0;
    }

    public void trackTopicCreateSuccess(long topicId) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(SprinklerEvents.TopicCreate)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, true)
                .property(PropertyKey.TopicId, topicId)
                .build());

    }

    public void trackTopicCreateFail(int errorCode) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(SprinklerEvents.TopicCreate)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, false)
                .property(PropertyKey.ErrorCode, errorCode)
                .build());

    }

    public void addTopic(Topic topic) {
        List<Marker> markers = new ArrayList<>();
        for (Long memberId : topic.getMembers()) {
            Marker marker = new Marker();
            marker.setTopic(topic);
            marker.setMemberId(memberId);
            marker.setReadLinkId(-1);
            markers.add(marker);
        }

        topic.setIsJoined(true);
        topic.setReadLinkId(-1);
        topic.setLastLinkId(-1);
        topic.setMarkers(markers);
        topic.setSubscribe(true);
        TopicRepository.getInstance().addTopic(topic);
    }
}
