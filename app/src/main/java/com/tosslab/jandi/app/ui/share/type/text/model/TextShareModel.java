package com.tosslab.jandi.app.ui.share.type.text.model;

import android.content.Context;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.client.MessageManipulator_;
import com.tosslab.jandi.app.ui.share.type.to.EntityInfo;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 2. 13..
 */
@EBean
public class TextShareModel {

    @RootContext
    Context context;

    public List<EntityInfo> getEntityInfos() {

        List<FormattedEntity> entities = new ArrayList<FormattedEntity>();

        EntityManager entityManager = EntityManager.getInstance(context);
        entities.addAll(entityManager.getJoinedChannels());
        entities.addAll(entityManager.getGroups());
        entities.addAll(entityManager.getFormattedUsersWithoutMe());

        Iterator<EntityInfo> iterator = Observable.from(entities)
                .map(entity -> {

                    boolean publicTopic = entity.isPublicTopic();
                    boolean privateGroup = entity.isPrivateGroup();
                    boolean user = entity.isUser();

                    String userLargeProfileUrl;
                    if (user) {
                        userLargeProfileUrl = entity.getUserLargeProfileUrl();
                    } else {
                        userLargeProfileUrl = "";
                    }
                    return new EntityInfo(entity.getId(), entity.getName(), publicTopic, privateGroup, userLargeProfileUrl);

                })
                .toBlocking()
                .getIterator();

        List<EntityInfo> entityInfos = new ArrayList<EntityInfo>();

        while (iterator.hasNext()) {
            entityInfos.add(iterator.next());
        }

        return entityInfos;

    }

    public void sendMessage(EntityInfo entity, String messageText) throws JandiNetworkException {

        MessageManipulator messageManipulator = MessageManipulator_.getInstance_(context);

        int entityType;

        if (entity.isPublicTopic()) {
            entityType = JandiConstants.TYPE_PUBLIC_TOPIC;
        } else if (entity.isPrivateTopic()) {
            entityType = JandiConstants.TYPE_PRIVATE_TOPIC;
        } else {
            entityType = JandiConstants.TYPE_DIRECT_MESSAGE;
        }
        messageManipulator.initEntity(entityType, entity.getEntityId());

        messageManipulator.sendMessage(messageText);

    }
}
