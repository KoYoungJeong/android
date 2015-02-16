package com.tosslab.jandi.app.ui.entities.chats.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.ui.entities.chats.to.ChatChooseItem;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
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

    public List<ChatChooseItem> getChatListWithoutMe() {
        List<FormattedEntity> formattedUsersWithoutMe = EntityManager.getInstance(context).getFormattedUsersWithoutMe();

        List<ChatChooseItem> chatChooseItems = new ArrayList<ChatChooseItem>();

        Iterator<ChatChooseItem> iterator = Observable.from(formattedUsersWithoutMe)
                .filter(entity -> TextUtils.equals(entity.getUser().status, "enabled"))
                .map(formattedEntity -> {
                    ChatChooseItem chatChooseItem = new ChatChooseItem();

                    chatChooseItem.entityId(formattedEntity.getId())
                            .email(formattedEntity.getUserEmail())
                            .name(formattedEntity.getName())
                            .starred(formattedEntity.isStarred)
                            .photoUrl(formattedEntity.getUserLargeProfileUrl());

                    return chatChooseItem;
                })
                .toBlocking()
                .getIterator();

        while (iterator.hasNext()) {
            chatChooseItems.add(iterator.next());
        }


        return chatChooseItems;
    }

    public List<ChatChooseItem> getChatListWithoutMe(String name) {


        List<FormattedEntity> formattedUsersWithoutMe = EntityManager.getInstance(context).getFormattedUsersWithoutMe();

        List<ChatChooseItem> chatChooseItems = new ArrayList<ChatChooseItem>();

        Iterator<ChatChooseItem> iterator = Observable.from(formattedUsersWithoutMe)
                .filter(entity -> TextUtils.equals(entity.getUser().status, "enabled"))
                .filter(formattedEntity -> !TextUtils.isEmpty(formattedEntity.getName()) && formattedEntity.getName().toLowerCase().contains(name.toLowerCase()))
                .map(formattedEntity -> {
                    ChatChooseItem chatChooseItem = new ChatChooseItem();

                    chatChooseItem.entityId(formattedEntity.getId())
                            .email(formattedEntity.getUserEmail())
                            .name(formattedEntity.getName())
                            .starred(formattedEntity.isStarred)
                            .photoUrl(formattedEntity.getUserLargeProfileUrl());

                    return chatChooseItem;
                })
                .toBlocking()
                .getIterator();

        while (iterator.hasNext()) {
            chatChooseItems.add(iterator.next());
        }


        return chatChooseItems;

    }

    public int getTeamId() {
        return JandiAccountDatabaseManager.getInstance(context).getSelectedTeamInfo().getTeamId();
    }

    public boolean isStarred(int entityId) {
        return EntityManager.getInstance(context).getEntityById(entityId).isStarred;
    }
}
