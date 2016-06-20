package com.tosslab.jandi.app.ui.entities.disabled.model;

import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.utils.StringCompareUtil;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

@EBean
public class DisabledEntityChooseModel {
    public List<ChatChooseItem> getDisabledMembers() {

        List<ChatChooseItem> items = new ArrayList<>();
        long myId = TeamInfoLoader.getInstance().getMyId();
        Observable.from(TeamInfoLoader.getInstance().getUserList())
                .filter(user -> user.getId() != myId)
                .filter(user -> !user.isEnabled())
                .map(user -> new ChatChooseItem()
                        .entityId(user.getId())
                        .statusMessage(user.getStatusMessage())
                        .name(user.getName())
                        .starred(TeamInfoLoader.getInstance().isStarredUser(user.getId()))
                        .enabled(false)
                        .photoUrl(user.getPhotoUrl()))
                .toSortedList((lhs, rhs) -> {
                    return StringCompareUtil.compare(lhs.getName(), rhs.getName());
                })
                .collect(() -> items, List::addAll)
                .subscribe();
        return items;

    }
}
