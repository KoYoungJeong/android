package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.model;


import android.text.TextUtils;

import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.room.Room;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.domain.TeamDisabledMemberItem;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.domain.TeamMemberItem;

import rx.Observable;

public class TeamMemberModel {
    public Observable<TeamMemberItem> getFilteredUser(String keyword, boolean selectMode, long roomId) {
        return Observable.from(TeamInfoLoader.getInstance().getUserList())
                .filter(User::isEnabled)
                .filter(user -> user.getName().toLowerCase().contains(keyword))
                .filter(user -> {
                    if (!selectMode || roomId < 0) {
                        return true;
                    }

                    Room room = TeamInfoLoader.getInstance().getRoom(roomId);
                    if (room != null) {
                        return !room.getMembers().contains(user.getId());
                    }

                    return true;
                })
                .map((user1) -> new TeamMemberItem(user1, keyword))
                .concatWith(Observable.defer(() -> {
                    // 검색어 없을 때
                    // 선택 모드
                    // 1인 pick 모드
                    return Observable.just(TextUtils.isEmpty(keyword) && selectMode && roomId < 0)
                            .filter(pickmode -> pickmode)
                            .flatMap(ttt -> Observable.from(TeamInfoLoader.getInstance().getUserList()))
                            .map(User::isEnabled) // enabled 상태 받음
                            .takeFirst(enabled -> !enabled) // disabled 인 상태 필터
                            .map(disabld -> new TeamDisabledMemberItem(null, keyword));
                }));
    }
}
