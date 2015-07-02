package com.tosslab.jandi.app.ui.maintab.topic.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit.RetrofitError;
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
    EntityClientManager entityClientManager;


    /**
     * topic 생성
     */
    public ResCommon createTopicInBackground(String entityName) throws RetrofitError {
        return entityClientManager.createPublicTopic(entityName);
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

    public void joinPublicTopic(ResLeftSideMenu.Channel channel) throws RetrofitError {
        entityClientManager.joinChannel(channel);
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
                        } else if(TextUtils.equals(event.getMessageType(), "link_preview_create")) {
                            // 단순 메세지 업데이트인 경우
                            return false;
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
}
