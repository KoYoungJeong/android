package com.tosslab.jandi.app.ui.maintab.topic.create.model;

import android.content.Context;
import android.text.TextUtils;


import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import retrofit.RetrofitError;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
@EBean
public class TopicCreateModel {

    @Bean
    EntityClientManager entityClientManager;

    @RootContext
    Context context;

    public ResCommon createTopic(String entityName, boolean publicSelected, String topicDescription) throws RetrofitError {
        if (publicSelected) {
            return entityClientManager.createPublicTopic(entityName, topicDescription);
        } else {
            return entityClientManager.createPrivateGroup(entityName, topicDescription);
        }

    }

    public void refreshEntity() throws RetrofitError {
        ResLeftSideMenu totalEntitiesInfo = entityClientManager.getTotalEntitiesInfo();
        LeftSideMenuRepository.getRepository().upsertLeftSideMenu(totalEntitiesInfo);
        int totalUnreadCount = BadgeUtils.getTotalUnreadCount(totalEntitiesInfo);
        JandiPreference.setBadgeCount(context, totalUnreadCount);
        BadgeUtils.setBadge(context, totalUnreadCount);

    }

    public boolean validTitle(String topicTitle) {
        return TextUtils.isEmpty(topicTitle);
    }
}
