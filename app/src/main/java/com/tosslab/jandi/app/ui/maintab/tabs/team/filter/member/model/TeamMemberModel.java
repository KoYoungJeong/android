package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.model;


import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.tosslab.jandi.app.network.client.privatetopic.GroupApi;
import com.tosslab.jandi.app.network.client.publictopic.ChannelApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqInviteTopicUsers;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.room.Room;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter.ToggleCollector;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.domain.TeamDisabledMemberItem;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.domain.TeamMemberItem;

import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import rx.Observable;

public class TeamMemberModel {

    private final Lazy<ChannelApi> channelApi;
    private final Lazy<GroupApi> groupApi;

    @Inject
    TeamMemberModel(Lazy<ChannelApi> channelApi, Lazy<GroupApi> groupApi) {
        this.channelApi = channelApi;
        this.groupApi = groupApi;
    }

    public Observable<TeamMemberItem> getFilteredUser(String keyword, boolean selectMode, long roomId) {
        return Observable.from(TeamInfoLoader.getInstance().getUserList())
                .filter(User::isEnabled)
                .filter(user -> user.getName().toLowerCase().contains(keyword))
                .filter(user -> {
                    if (!selectMode && roomId < 0) {
                        // bot 아닌 것만 통과
                        return !user.isBot();
                    }

                    if (user.getId() == TeamInfoLoader.getInstance().getMyId()) {
                        return false;
                    }

                    // 멀티 셀렉트 모드인 경우 봇은 제외
                    if (roomId > 0 && user.isBot()) {
                        return false;
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

    @NonNull
    public Observable<ResCommon> deferInvite(ToggleCollector toggledIds, long roomId) {
        return Observable.defer(() -> {
            List<Long> userIds = toggledIds.getIds();
            long teamId = TeamInfoLoader.getInstance().getTeamId();
            ResCommon resCommon;
            try {
                if (TeamInfoLoader.getInstance().isPublicTopic(roomId)) {
                    resCommon = channelApi.get().invitePublicTopic(roomId, new ReqInviteTopicUsers(userIds, teamId));
                } else {
                    resCommon = groupApi.get().inviteGroup(roomId, new ReqInviteTopicUsers(userIds, teamId));
                }
                return Observable.just(resCommon);
            } catch (RetrofitException e) {
                return Observable.error(e);
            }

        });
    }

}
