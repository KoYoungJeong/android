package com.tosslab.jandi.app.ui.maintab.topic.views.joinabletopiclist.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.maintab.topic.domain.Topic;
import com.tosslab.jandi.app.ui.members.model.MembersModel;
import com.tosslab.jandi.app.utils.StringCompareUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import retrofit.RetrofitError;
import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
public class JoinableTopicListModel {

    private final EntityClientManager entityClientManager;

    @Inject
    public JoinableTopicListModel(EntityClientManager entityClientManager) {
        this.entityClientManager = entityClientManager;
    }

    public void joinPublicTopic(long id) throws RetrofitError {
        entityClientManager.joinChannel(id);
    }

    public boolean refreshEntity() {
        try {
            ResLeftSideMenu totalEntitiesInfo = entityClientManager.getTotalEntitiesInfo();
            LeftSideMenuRepository.getRepository().upsertLeftSideMenu(totalEntitiesInfo);
            EntityManager.getInstance().refreshEntity();
            return true;
        } catch (RetrofitError e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Observable<List<Topic>> getSearchedTopics(final String query) {
        List<FormattedEntity> unjoinedChannels = EntityManager.getInstance().getUnjoinedChannels();
        return Observable.from(unjoinedChannels)
                .map(formattedEntity -> {
                    long creatorId =
                            ((ResLeftSideMenu.Channel) formattedEntity.getEntity()).ch_creatorId;
                    return new Topic.Builder()
                            .entityId(formattedEntity.getId())
                            .description(formattedEntity.getDescription())
                            .isJoined(false)
                            .creatorId(creatorId)
                            .isPublic(formattedEntity.isPublicTopic())
                            .isStarred(formattedEntity.isStarred)
                            .memberCount(formattedEntity.getMemberCount())
                            .name(formattedEntity.getName())
                            .markerLinkId(formattedEntity.lastLinkId)
                            .unreadCount(formattedEntity.alarmCount)
                            .build();
                })
                .filter(topic -> TextUtils.isEmpty(query)
                        || topic.getName().toLowerCase().contains(query.toLowerCase()))
                .toSortedList((lhs, rhs) -> StringCompareUtil.compare(lhs.getName(), rhs.getName()));
    }

    public Observable<Topic> getJoinTopicObservable(final Topic topic) {
        return Observable.<Topic>create(subscriber -> {
            try {
                joinPublicTopic(topic.getEntityId());

                refreshEntity();

                EntityManager entityManager = EntityManager.getInstance();
                MixpanelMemberAnalyticsClient
                        .getInstance(JandiApplication.getContext(), entityManager.getDistictId())
                        .trackJoinChannel();
                subscriber.onNext(topic);
            } catch (RetrofitError error) {
                subscriber.onError(error);
            }
            subscriber.onCompleted();
        });
    }
}
