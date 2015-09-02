package com.tosslab.jandi.app.ui.maintab.topics.views.joinabletopiclist.model;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.maintab.topics.domain.Topic;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.List;

import retrofit.RetrofitError;
import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
@EBean
public class JoinableTopicModel {

    @Bean
    EntityClientManager entityClientManager;

    public Observable<Topic> getUnjoinEntities(List<FormattedEntity> unjoinedChannels) {
        return Observable.from(unjoinedChannels).map(formattedEntity -> {

            int creatorId = ((ResLeftSideMenu.Channel) formattedEntity.getEntity()).ch_creatorId;

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
        });
    }

    public void joinPublicTopic(int id) throws RetrofitError {
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
}
