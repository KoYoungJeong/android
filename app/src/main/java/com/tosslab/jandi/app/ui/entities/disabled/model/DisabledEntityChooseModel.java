package com.tosslab.jandi.app.ui.entities.disabled.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.utils.StringCompareUtil;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

@EBean
public class DisabledEntityChooseModel {
    public List<ChatChooseItem> getDisabledMembers() {
        List<FormattedEntity> formattedUsersWithoutMe = EntityManager.getInstance().getFormattedUsersWithoutMe();

        List<ChatChooseItem> items = new ArrayList<>();

        Observable.from(formattedUsersWithoutMe)
                .filter(entity -> !TextUtils.equals(entity.getUser().status, "enabled"))
                .map(formattedEntity -> new ChatChooseItem()
                        .entityId(formattedEntity.getId())
                        .statusMessage(formattedEntity.getUserStatusMessage())
                        .name(formattedEntity.getName())
                        .starred(formattedEntity.isStarred)
                        .enabled(false)
                        .photoUrl(formattedEntity.getUserLargeProfileUrl()))
                .toSortedList((lhs, rhs) -> {
                    return StringCompareUtil.compare(lhs.getName(), rhs.getName());
                })
                .collect(() -> items, List::addAll)
                .subscribe();
        return items;

    }
}
