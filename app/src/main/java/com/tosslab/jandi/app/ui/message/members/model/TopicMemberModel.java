package com.tosslab.jandi.app.ui.message.members.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.ui.entities.chats.to.ChatChooseItem;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
                                    .enabled(TextUtils.equals(entity.getUser().status, "enabled"))
                                    .name(entity.getName());

                        })
                        .toBlocking()
                        .firstOrDefault(new ChatChooseItem().entityId(-1)))
                .filter(chatChooseItem -> chatChooseItem.getEntityId() != -1)
                .toBlocking()
                .getIterator();

        List<ChatChooseItem> chatChooseItems = new ArrayList<ChatChooseItem>();
        while (iterator.hasNext()) {
            chatChooseItems.add(iterator.next());
        }

        Collections.sort(chatChooseItems, new Comparator<ChatChooseItem>() {
            @Override
            public int compare(ChatChooseItem lhs, ChatChooseItem rhs) {
                if (lhs.isEnabled()) {
                    if (rhs.isEnabled()) {
                        return 0;
                    } else {
                        return 1;
                    }
                } else {
                    if (rhs.isEnabled()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            }
        });

        return chatChooseItems;

    }
}
