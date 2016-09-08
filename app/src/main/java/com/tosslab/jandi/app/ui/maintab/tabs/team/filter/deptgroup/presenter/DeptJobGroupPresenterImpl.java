package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.deptgroup.presenter;

import android.text.TextUtils;

import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.DeptJobFragment;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter.TeamMemberDataModel;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter.ToggleCollector;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.domain.TeamMemberItem;

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
    private int type;
    private String keyword;
    private boolean selectMode;
    private boolean pickMode;

    @Inject
    public DeptJobGroupPresenterImpl(View view, TeamMemberDataModel teamMemberDataModel, ToggleCollector toggledUser) {
        this.view = view;
        this.teamMemberDataModel = teamMemberDataModel;
        this.toggledUser = toggledUser;
    }

    @Override
    public void onCreate() {
        Observable.from(TeamInfoLoader.getInstance().getUserList())
                .filter(User::isEnabled)
                .filter(user -> {
                    if (pickMode || selectMode) {
                        return user.getId() != TeamInfoLoader.getInstance().getMyId();
                    }
                    return true;
                })
                .filter(filterKeyword(type, keyword))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map((user1) -> {
                    TeamMemberItem teamMemberItem = new TeamMemberItem(user1, keyword);
                    teamMemberItem.setNameOfSpan(user1.getName());
                    return teamMemberItem;
                })
                .subscribe(it -> {
                    teamMemberDataModel.add(it);
                    view.refreshDataView();
                });
    }

    private Func1<? super User, Boolean> filterKeyword(int type, String keyword) {
        return user -> {
            if (type == DeptJobFragment.EXTRA_TYPE_JOB) {
                return !TextUtils.isEmpty(user.getPosition())
                        && user.getPosition().contains(keyword);
            } else {
                return !TextUtils.isEmpty(user.getDivision())
                        && user.getDivision().contains(keyword);
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
            if (toggledUser.containsId(user.getEntityId())) {
                toggledUser.removeId(user.getEntityId());
            } else {
                toggledUser.addId(user.getEntityId());
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
}
