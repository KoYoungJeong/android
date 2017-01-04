package com.tosslab.jandi.app.ui.entities.disabled.model;

import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.utils.StringCompareUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;

public class DisabledEntityChooseModel {
    @Inject
    public DisabledEntityChooseModel() { }

    public List<ChatChooseItem> getDisabledMembers() {

        List<ChatChooseItem> items = new ArrayList<>();
        long myId = TeamInfoLoader.getInstance().getMyId();
        Observable.from(TeamInfoLoader.getInstance().getUserList())
                .filter(user -> user.getId() != myId)
                .filter(User::isDisabled)
                .map(ChatChooseItem::create)
                .toSortedList((lhs, rhs) -> {
                    return StringCompareUtil.compare(lhs.getName(), rhs.getName());
                })
                .collect(() -> items, List::addAll)
                .subscribe(it -> {}, Throwable::printStackTrace);
        return items;

    }
}
