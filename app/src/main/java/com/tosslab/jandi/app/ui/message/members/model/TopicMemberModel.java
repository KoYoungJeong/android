package com.tosslab.jandi.app.ui.message.members.model;

import android.content.Context;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.ui.entities.chats.to.ChatChooseItem;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 1. 20..
 */
@EBean
public class TopicMemberModel {


    @RootContext
    Context context;

    public List<ChatChooseItem> getTopicMembers(int entityId) {
        List<Integer> members = EntityManager.getInstance(context).getEntityById(entityId).getMembers();
        List<FormattedEntity> formattedUsers = EntityManager.getInstance(context).getFormattedUsers();

        Iterator<ChatChooseItem> iterator = Observable.from(members)
                .map(memberEntityId -> Observable.from(formattedUsers)
                        .filter(entity -> entity.getId() == memberEntityId)
                        .map(entity -> {

                            ChatChooseItem chatChooseItem = new ChatChooseItem();
                            return chatChooseItem.entityId(entity.getId())
                                    .email(entity.getUserEmail())
                                    .photoUrl(entity.getUserLargeProfileUrl())
                                    .starred(entity.isStarred)
                                    .name(entity.getName());

                        })
                        .toBlocking()
                        .first())
                .toBlocking()
                .getIterator();

        List<ChatChooseItem> chatChooseItems = new ArrayList<ChatChooseItem>();
        while (iterator.hasNext()) {
            chatChooseItems.add(iterator.next());
        }

        return chatChooseItems;

    }
}
