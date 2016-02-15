package com.tosslab.jandi.app.ui.entities.chats.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.lists.BotEntity;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.ui.entities.chats.domain.DisableDummyItem;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Func2;

@EBean
public class ChatChooseModel {

    @RootContext
    Context context;

    private Func2<ChatChooseItem, ChatChooseItem, Integer> getChatItemComparator() {
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

                    if (lhs.isBot()) {
                        return -1;
                    } else if (rhs.isBot()) {
                        return 1;
                    } else {
                        return lhs.getName().compareTo(rhs.getName());
                    }
                } else {
                    return -1;
                }
            } else {
                if (rhs.isStarred()) {
                    return 1;
                } else {
                    if (lhs.isBot()) {
                        return -1;
                    } else if (rhs.isBot()) {
                        return 1;
                    } else {
                        return lhs.getName().compareTo(rhs.getName());
                    }
                }
            }
        };
    }

    public List<ChatChooseItem> getChatListWithoutMe(String name) {

        List<FormattedEntity> formattedUsersWithoutMe = EntityManager.getInstance().getFormattedUsersWithoutMe();

        List<ChatChooseItem> chatChooseItems = new ArrayList<>();

        Observable.from(formattedUsersWithoutMe)
                .filter(formattedEntity -> !TextUtils.isEmpty(formattedEntity.getName()) && formattedEntity.getName().toLowerCase().contains(name.toLowerCase()))
                .map(formattedEntity -> {
                    ChatChooseItem chatChooseItem = new ChatChooseItem();

                    chatChooseItem.entityId(formattedEntity.getId())
                            .statusMessage(formattedEntity.getUserStatusMessage())
                            .name(formattedEntity.getName())
                            .starred(formattedEntity.isStarred)
                            .enabled(TextUtils.equals(formattedEntity.getUser().status, "enabled"))
                            .photoUrl(formattedEntity.getUserLargeProfileUrl());

                    return chatChooseItem;
                })
                .mergeWith(Observable.create(subscriber -> {
                    // 잔디봇이 포함되어 있고 잔디봇의 이름이 포함되어 있는 경우 추가한다
                    if (hasJandiBot()) {
                        ChatChooseItem jandiBot = getJandiBot();
                        if (jandiBot.getName().toLowerCase().contains(name.toLowerCase())) {
                            subscriber.onNext(jandiBot);
                        }
                        subscriber.onCompleted();
                    }
                }))
                .toSortedList(getChatItemComparator())
                .collect(() -> chatChooseItems, List::addAll)
                .subscribe();


        return chatChooseItems;

    }

    public long getTeamId() {
        return AccountRepository.getRepository().getSelectedTeamId();
    }

    private boolean hasDisabledUsers() {

        List<FormattedEntity> formattedUsersWithoutMe = EntityManager.getInstance().getFormattedUsersWithoutMe();


        return Observable.from(formattedUsersWithoutMe)
                .filter(entity -> !TextUtils.equals(entity.getUser().status, "enabled"))
                .map(entity -> true)
                .firstOrDefault(false)
                .toBlocking()
                .first();
    }

    public List<ChatChooseItem> getUsers() {

        List<ChatChooseItem> users = getEnableUsers();

        boolean hasDisabledUsers = hasDisabledUsers();
        if (hasDisabledUsers) {
            users.add(new DisableDummyItem());
        }

        return users;
    }

    private List<ChatChooseItem> getEnableUsers() {
        List<FormattedEntity> formattedUsersWithoutMe = EntityManager.getInstance().getFormattedUsersWithoutMe();

        List<ChatChooseItem> chatChooseItems = new ArrayList<>();

        Observable.from(formattedUsersWithoutMe)
                .filter(entity -> TextUtils.equals(entity.getUser().status, "enabled"))
                .map(formattedEntity -> {
                    ChatChooseItem chatChooseItem = new ChatChooseItem();

                    chatChooseItem.entityId(formattedEntity.getId())
                            .statusMessage(formattedEntity.getUserStatusMessage())
                            .name(formattedEntity.getName())
                            .starred(formattedEntity.isStarred)
                            .enabled(true)
                            .owner(formattedEntity.isTeamOwner())
                            .photoUrl(formattedEntity.getUserLargeProfileUrl());

                    return chatChooseItem;
                })
                .mergeWith(Observable.create(subscriber -> {
                    if (hasJandiBot()) {
                        subscriber.onNext(getJandiBot());
                    }

                    subscriber.onCompleted();
                }))
                .toSortedList(getChatItemComparator())
                .collect(() -> chatChooseItems, List::addAll)
                .subscribe();


        return chatChooseItems;
    }

    private ChatChooseItem getJandiBot() {
        BotEntity jandiBot = ((BotEntity) EntityManager.getInstance().getJandiBot());
        return new ChatChooseItem()
                .enabled(jandiBot.isEnabled())
                .isBot(true)
                .entityId(jandiBot.getId())
                .name(jandiBot.getName())
                .owner(false)
                .photoUrl(jandiBot.getUserMediumProfileUrl())
                .starred(jandiBot.isStarred);
    }

    private boolean hasJandiBot() {
        return EntityManager.getInstance().hasJandiBot();
    }

}
