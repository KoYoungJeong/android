package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.deptgroup.presenter;

import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.authority.Level;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.room.Room;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.DeptJobFragment;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter.TeamMemberDataModel;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter.ToggleCollector;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.domain.TeamMemberItem;
import com.tosslab.jandi.app.utils.StringCompareUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class DeptJobGroupPresenterImpl implements DeptJobGroupPresenter {
    final View view;
    final TeamMemberDataModel teamMemberDataModel;
    final ToggleCollector toggledUser;
    private final String undefinedMember;
    private int type;
    private String keyword;
    private boolean selectMode;
    private boolean pickMode;
    private long roomId;

    @Inject
    public DeptJobGroupPresenterImpl(View view, TeamMemberDataModel teamMemberDataModel, ToggleCollector toggledUser) {
        this.view = view;
        this.teamMemberDataModel = teamMemberDataModel;
        this.toggledUser = toggledUser;
        undefinedMember = JandiApplication.getContext().getString(R.string.jandi_undefined_member);
    }

    @Override
    public void onCreate() {

        Observable.from(getUserList())
                .filter(User::isEnabled)
                .filter(user -> {
                    if (!selectMode && roomId < 0) {
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
                .filter(filterKeyword(type, keyword))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .map((user1) -> {
                    TeamMemberItem teamMemberItem = new TeamMemberItem(user1, keyword);
                    teamMemberItem.setNameOfSpan(user1.getName());
                    return teamMemberItem;
                })
                .toSortedList((teamMemberItem, teamMemberItem2) -> {
                    return StringCompareUtil.compare(teamMemberItem.getName(), teamMemberItem2.getName());
                })
                .subscribe(users -> {
                    teamMemberDataModel.clear();
                    teamMemberDataModel.addAll(users);
                    view.refreshDataView();
                }, Throwable::printStackTrace);
    }

    private List<User> getUserList() {
        if (TeamInfoLoader.getInstance().getMyLevel() == Level.Guest) {
            List<User> userList = new ArrayList<>();

            Observable.from(TeamInfoLoader.getInstance().getTopicList())
                    .filter(topic -> topic.isJoined())
                    .concatMap(topic -> Observable.from(topic.getMembers()))
                    .distinct()
                    .subscribe(memberId -> {
                        userList.add(TeamInfoLoader.getInstance().getUser(memberId));
                    });
            return userList;
        } else {
            return TeamInfoLoader.getInstance().getUserList();
        }
    }

    private Func1<? super User, Boolean> filterKeyword(int type, String keyword) {
        return user -> {
            if (type == DeptJobFragment.EXTRA_TYPE_JOB) {
                if (!TextUtils.isEmpty(user.getPosition())) {
                    return user.getPosition().equals(keyword);
                } else {
                    return TextUtils.equals(keyword, undefinedMember);
                }
            } else {
                if (!TextUtils.isEmpty(user.getDivision())) {

                    return user.getDivision().equals(keyword);
                } else {
                    return TextUtils.equals(keyword, undefinedMember);
                }
            }
        };
    }

    @Override
    public void onMemberClick(int position) {
        TeamMemberItem item = teamMemberDataModel.getItem(position);
        ChatChooseItem user = item.getChatChooseItem();
        if (!selectMode || pickMode) {
            view.pickUser(user.getEntityId());
        } else {
            AnalyticsValue.Screen screen = type == DeptJobFragment.EXTRA_TYPE_DEPT
                    ? AnalyticsValue.Screen.InviteTeamMembers_Department
                    : AnalyticsValue.Screen.InviteTeamMembers_JobTitle;

            if (toggledUser.containsId(user.getEntityId())) {
                toggledUser.removeId(user.getEntityId());
                AnalyticsUtil.sendEvent(screen, AnalyticsValue.Action.UnselectMember);
            } else {
                toggledUser.addId(user.getEntityId());
                AnalyticsUtil.sendEvent(screen, AnalyticsValue.Action.SelectMember);
            }

            view.refreshDataView();
            view.updateToggledUser(toggledUser.count());
        }
    }

    @Override
    public void onUnselectClick() {
        toggledUser.clearIds();
        view.updateToggledUser(toggledUser.count());
        view.refreshDataView();
    }

    @Override
    public void onAddClick() {
        List<Long> ids1 = toggledUser.getIds();
        long[] ids = new long[toggledUser.count()];

        int size = ids1.size();
        for (int idx = 0; idx < size; idx++) {
            ids[idx] = ids1.get(idx);
        }

        view.comeWithResult(ids);
    }

    @Override
    public void addToggleOfAll() {
        for (int idx = 0, size = teamMemberDataModel.getSize(); idx < size; idx++) {
            ChatChooseItem chatChooseItem = teamMemberDataModel.getItem(idx).getChatChooseItem();
            toggledUser.addId(chatChooseItem.getEntityId());
        }

        view.refreshDataView();
        view.updateToggledUser(toggledUser.count());
    }

    @Override
    public void onRefresh() {
        onCreate();
    }

    public void setTypeAndKeyword(int type, String keyword) {
        this.type = type;
        this.keyword = keyword;
    }

    public void setSelectMode(boolean selectMode) {
        this.selectMode = selectMode;
    }

    public void setPickMode(boolean pickMode) {
        this.pickMode = pickMode;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }
}
