package com.tosslab.jandi.app.ui.entities.chats.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.ui.entities.chats.to.ChatChooseItem;
import com.tosslab.jandi.app.ui.entities.chats.to.DisableDummyItem;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 1. 14..
 */
@EBean
public class ChatChooseModel {

    @RootContext
    Context context;

    public List<ChatChooseItem> getEnableUsers() {
        List<FormattedEntity> formattedUsersWithoutMe = EntityManager.getInstance().getFormattedUsersWithoutMe();

        List<ChatChooseItem> chatChooseItems = new ArrayList<ChatChooseItem>();

        Iterator<ChatChooseItem> iterator = Observable.from(formattedUsersWithoutMe)
                .filter(entity -> TextUtils.equals(entity.getUser().status, "enabled"))
                .map(formattedEntity -> {
                    ChatChooseItem chatChooseItem = new ChatChooseItem();

                    chatChooseItem.entityId(formattedEntity.getId())
                            .email(formattedEntity.getUserEmail())
                            .name(formattedEntity.getName())
                            .starred(formattedEntity.isStarred)
                            .enabled(true)
                            .photoUrl(formattedEntity.getUserLargeProfileUrl());

                    return chatChooseItem;
                })
                .toBlocking()
                .getIterator();

        while (iterator.hasNext()) {
            chatChooseItems.add(iterator.next());
        }

        Collections.sort(chatChooseItems, getChatItemComparator());


        return chatChooseItems;
    }

    private Comparator<ChatChooseItem> getChatItemComparator() {
        return (lhs, rhs) -> {
            int compareValue = 0;
            if (lhs.isEnabled()) {
                if (rhs.isEnabled()) {
                    compareValue = 0;
                } else {
                    compareValue = -1;
                }
            } else {
                if (rhs.isEnabled()) {
                    compareValue = 1;
                } else {
                    compareValue = 0;
                }
            }

            if (compareValue != 0) {
                return compareValue;
            }

            if (lhs.isStarred()) {
                if (rhs.isStarred()) {
                    return lhs.getName().compareTo(rhs.getName());
                } else {
                    return -1;
                }
            } else {
                if (rhs.isStarred()) {
                    return 1;
                } else {
                    return lhs.getName().compareTo(rhs.getName());
                }
            }
        };
    }

    public List<ChatChooseItem> getChatListWithoutMe(String name) {

        List<FormattedEntity> formattedUsersWithoutMe = EntityManager.getInstance().getFormattedUsersWithoutMe();

        List<ChatChooseItem> chatChooseItems = new ArrayList<ChatChooseItem>();

        Iterator<ChatChooseItem> iterator = Observable.from(formattedUsersWithoutMe)
                .filter(formattedEntity -> !TextUtils.isEmpty(formattedEntity.getName()) && formattedEntity.getName().toLowerCase().contains(name.toLowerCase()))
                .map(formattedEntity -> {
                    ChatChooseItem chatChooseItem = new ChatChooseItem();

                    chatChooseItem.entityId(formattedEntity.getId())
                            .email(formattedEntity.getUserEmail())
                            .name(formattedEntity.getName())
                            .starred(formattedEntity.isStarred)
                            .enabled(TextUtils.equals(formattedEntity.getUser().status, "enabled"))
                            .photoUrl(formattedEntity.getUserLargeProfileUrl());

                    return chatChooseItem;
                })
                .toBlocking()
                .getIterator();

        while (iterator.hasNext()) {
            chatChooseItems.add(iterator.next());
        }

        Collections.sort(chatChooseItems, getChatItemComparator());


        return chatChooseItems;

    }

    public int getTeamId() {
        return AccountRepository.getRepository().getSelectedTeamId();
    }

    public boolean isStarred(int entityId) {
        return EntityManager.getInstance().getEntityById(entityId).isStarred;
    }

    public boolean hasDisabledUsers() {

        List<FormattedEntity> formattedUsersWithoutMe = EntityManager.getInstance().getFormattedUsersWithoutMe();

        Boolean hasDisabled = Observable.from(formattedUsersWithoutMe)
                .filter(entity -> !TextUtils.equals(entity.getUser().status, "enabled"))
                .map(entity -> true)
                .firstOrDefault(false)
                .toBlocking()
                .first();


        return hasDisabled;
    }

    public List<ChatChooseItem> getDisableUsers() {
        List<FormattedEntity> formattedUsersWithoutMe = EntityManager.getInstance().getFormattedUsersWithoutMe();

        List<ChatChooseItem> chatChooseItems = new ArrayList<ChatChooseItem>();

        Iterator<ChatChooseItem> iterator = Observable.from(formattedUsersWithoutMe)
                .filter(entity -> !TextUtils.equals(entity.getUser().status, "enabled"))
                .map(formattedEntity -> {
                    ChatChooseItem chatChooseItem = new ChatChooseItem();

                    chatChooseItem.entityId(formattedEntity.getId())
                            .email(formattedEntity.getUserEmail())
                            .name(formattedEntity.getName())
                            .starred(formattedEntity.isStarred)
                            .enabled(false)
                            .photoUrl(formattedEntity.getUserLargeProfileUrl());

                    return chatChooseItem;
                })
                .toBlocking()
                .getIterator();

        while (iterator.hasNext()) {
            chatChooseItems.add(iterator.next());
        }

        Collections.sort(chatChooseItems, getChatItemComparator());


        return chatChooseItems;
    }

    public List<ChatChooseItem> getUsers() {

        List<ChatChooseItem> users = new ArrayList<ChatChooseItem>();

        List<ChatChooseItem> enableUsers = getEnableUsers();
        boolean hasDisabledUsers = hasDisabledUsers();
        users.addAll(enableUsers);

        if (hasDisabledUsers) {
            List<ChatChooseItem> disableUsers = getDisableUsers();
            users.add(new DisableDummyItem(false, disableUsers.size()));
            users.addAll(disableUsers);
        }

        return users;
    }

}
