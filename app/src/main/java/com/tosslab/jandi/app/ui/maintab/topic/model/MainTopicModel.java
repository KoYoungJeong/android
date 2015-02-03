package com.tosslab.jandi.app.ui.maintab.topic.model;

import android.content.Context;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
@EBean
public class MainTopicModel {

    @RootContext
    Context context;

    @Bean
    JandiEntityClient jandiEntityClient;


    /**
     * topic 생성
     */
    public ResCommon createTopicInBackground(String entityName) throws JandiNetworkException {
        return jandiEntityClient.createPublicTopic(entityName);
    }

    public List<FormattedEntity> getJoinEntities(List<FormattedEntity> joinedChannels, List<FormattedEntity> groups) {
        List<FormattedEntity> entities = new ArrayList<FormattedEntity>();

        entities.addAll(joinedChannels);
        entities.addAll(groups);

        Collections.sort(entities, new EntityComparator());

        return entities;
    }

    public List<FormattedEntity> getUnjoinEntities(List<FormattedEntity> unjoinedChannels) {
        List<FormattedEntity> entities = new ArrayList<FormattedEntity>();
        entities.addAll(unjoinedChannels);
        Collections.sort(entities, new EntityComparator());


        return entities;
    }

    public void joinPublicTopic(ResLeftSideMenu.Channel channel) throws JandiNetworkException {
        jandiEntityClient.joinChannel(channel);
    }

    public boolean hasAlarmCount(List<FormattedEntity> joinEntities) {
        for (FormattedEntity joinEntity : joinEntities) {
            if (joinEntity.alarmCount > 0) {
                return true;
            }
        }
        return false;
    }
}
