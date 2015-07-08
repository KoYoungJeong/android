package com.tosslab.jandi.app.ui.maintab.topic.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.ui.maintab.topic.domain.Topic;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.List;

import retrofit.RetrofitError;
import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
@EBean
public class MainTopicModel {

    @RootContext
    Context context;

    @Bean
    EntityClientManager entityClientManager;


    public Observable<Topic> getJoinEntities(List<FormattedEntity> joinedChannels, List<FormattedEntity>
            groups) {

        return Observable.merge(Observable.from(joinedChannels), Observable.from(groups))
                .map(formattedEntity -> new Topic.Builder()
                        .entityId(formattedEntity.getId())
                        .description(formattedEntity.getDescription())
                        .isJoined(true)
                        .isPublic(formattedEntity.isPublicTopic())
                        .isStarred(formattedEntity.isStarred)
                        .memberCount(formattedEntity.getMemberCount())
                        .name(formattedEntity.getName())
                        .unreadCount(formattedEntity.alarmCount)
                        .build());
    }

    public Observable<Topic> getUnjoinEntities(List<FormattedEntity> unjoinedChannels) {
        return Observable.from(unjoinedChannels).map(formattedEntity -> new Topic.Builder()
                .entityId(formattedEntity.getId())
                .description(formattedEntity.getDescription())
                .isJoined(false)
                .isPublic(formattedEntity.isPublicTopic())
                .isStarred(formattedEntity.isStarred)
                .memberCount(formattedEntity.getMemberCount())
                .name(formattedEntity.getName())
                .unreadCount(formattedEntity.alarmCount)
                .build());

    }

    public void joinPublicTopic(ResLeftSideMenu.Channel channel) throws RetrofitError {
        entityClientManager.joinChannel(channel);
    }

    public boolean hasAlarmCount(Observable<Topic> joinEntities) {

        Topic defaultValue = new Topic.Builder().build();
        Topic first = joinEntities.filter(topic -> topic.getUnreadCount() > 0)
                .firstOrDefault(defaultValue)
                .toBlocking()
                .first();

        return first != defaultValue;

    }

    public boolean updateBadge(SocketMessageEvent event, List<Topic> joinedTopics) {
        Topic emptyEntity = new Topic.Builder().build();
        Topic entity = Observable.from(joinedTopics)
                .filter(entity1 -> {
                    if (!TextUtils.equals(event.getMessageType(), "file_comment")) {
                        return entity1.getEntityId() == event.getRoom().getId();
                    } else if (TextUtils.equals(event.getMessageType(), "link_preview_create")) {
                        // 단순 메세지 업데이트인 경우
                        return false;
                    } else {
                        for (SocketMessageEvent.MessageRoom messageRoom : event.getRooms()) {
                            if (entity1.getEntityId() == messageRoom.getId()) {
                                return true;
                            }
                        }
                        return false;
                    }
                })
                .firstOrDefault(emptyEntity)
                .toBlocking()
                .first();

        if (entity != emptyEntity && entity.getEntityId() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean refreshEntity() {
        try {
            ResLeftSideMenu totalEntitiesInfo = entityClientManager.getTotalEntitiesInfo();
            JandiEntityDatabaseManager.getInstance(context).upsertLeftSideMenu(totalEntitiesInfo);
            int totalUnreadCount = BadgeUtils.getTotalUnreadCount(totalEntitiesInfo);
            JandiPreference.setBadgeCount(context, totalUnreadCount);
            BadgeUtils.setBadge(context, totalUnreadCount);
            EntityManager.getInstance(context).refreshEntity(totalEntitiesInfo);
            return true;
        } catch (RetrofitError e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void resetBadge(Context context, int entityId) {
        EntityManager.getInstance(context).getEntityById(entityId).alarmCount = 0;
    }
}
