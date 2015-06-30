package com.tosslab.jandi.app.ui.maintab.topic.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

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

    public boolean updateBadge(SocketMessageEvent event, List<FormattedEntity> joinedTopics) {
        FormattedEntity emptyEntity = new FormattedEntity();
        FormattedEntity entity = Observable.from(joinedTopics)
                .filter(new Func1<FormattedEntity, Boolean>() {
                    @Override
                    public Boolean call(FormattedEntity entity) {
                        if (!TextUtils.equals(event.getMessageType(), "file_comment")) {
                            return entity.getId() == event.getRoom().getId();
                        } else {
                            for (SocketMessageEvent.MessageRoom messageRoom : event.getRooms()) {
                                if (entity.getId() == messageRoom.getId()) {
                                    return true;
                                }
                            }
                            return false;
                        }
                    }
                })
                .firstOrDefault(emptyEntity)
                .toBlocking()
                .first();

        if (entity != emptyEntity && entity.getId() > 0) {
            entity.alarmCount++;
            return true;
        } else {
            return false;
        }
    }

    public boolean refreshEntity() {
        try {
            ResLeftSideMenu totalEntitiesInfo = jandiEntityClient.getTotalEntitiesInfo();
            JandiEntityDatabaseManager.getInstance(context).upsertLeftSideMenu(totalEntitiesInfo);
            int totalUnreadCount = BadgeUtils.getTotalUnreadCount(totalEntitiesInfo);
            JandiPreference.setBadgeCount(context, totalUnreadCount);
            BadgeUtils.setBadge(context, totalUnreadCount);
            EntityManager.getInstance(context).refreshEntity(totalEntitiesInfo);

            return true;
        } catch (JandiNetworkException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
