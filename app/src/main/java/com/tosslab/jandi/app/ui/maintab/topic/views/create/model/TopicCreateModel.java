package com.tosslab.jandi.app.ui.maintab.topic.views.create.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.BadgeCountRepository;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

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
        BadgeCountRepository badgeCountRepository = BadgeCountRepository.getRepository();
        badgeCountRepository.upsertBadgeCount(EntityManager.getInstance().getTeamId(), totalUnreadCount);
        BadgeUtils.setBadge(JandiApplication.getContext(), badgeCountRepository.getTotalBadgeCount());
    }

    public boolean invalideTitle(String topicTitle) {
        return TextUtils.isEmpty(topicTitle) || TextUtils.getTrimmedLength(topicTitle) <= 0;
    }

    public void trackTopicCreateSuccess(int topicId) {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.TopicCreate)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, true)
                        .property(PropertyKey.TopicId, topicId)
                        .build());

    }

    public void trackTopicCreateFail(int errorCode) {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.TopicCreate)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, false)
                        .property(PropertyKey.ErrorCode, errorCode)
                        .build());

    }
}
