package com.tosslab.jandi.app.ui.maintab.team.filter.deptgroup.presenter;

import android.text.TextUtils;

import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.ui.maintab.team.filter.dept.DeptJobFragment;
import com.tosslab.jandi.app.ui.maintab.team.filter.member.adapter.TeamMemberDataModel;
import com.tosslab.jandi.app.ui.maintab.team.filter.member.domain.TeamMemberItem;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class DeptJobGroupPresenterImpl implements DeptJobGroupPresenter {
    private final View view;
    private final TeamMemberDataModel teamMemberDataModel;
    private int type;
    private String keyword;
    private boolean selectMode;
    private Set<Long> toggledUser;
    private boolean pickMode;

    @Inject
    public DeptJobGroupPresenterImpl(View view, TeamMemberDataModel teamMemberDataModel) {
        this.view = view;
        this.teamMemberDataModel = teamMemberDataModel;
    }

    @Override
    public void onCreate() {
        Observable.from(TeamInfoLoader.getInstance().getUserList())
                .filter(User::isEnabled)
                .filter(user -> {
                    if (pickMode) {
                        return user.getId() != TeamInfoLoader.getInstance().getMyId();
                    }
                    return true;
                })
                .filter(filterKeyword(type, keyword))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(TeamMemberItem::new)
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
    public void onDestroy() {

    }

    @Override
    public void onMemberClick(int position) {
        TeamMemberItem item = teamMemberDataModel.getItem(position);
        ChatChooseItem user = item.getChatChooseItem();
        if (!selectMode || pickMode) {
            view.pickUser(user.getEntityId());
        } else {
            if (toggledUser.contains(user.getEntityId())) {
                user.setIsChooseItem(false);
                toggledUser.remove(user.getEntityId());
            } else {
                user.setIsChooseItem(true);
                toggledUser.add(user.getEntityId());
            }

            view.refreshDataView();
            view.updateToggledUser(toggledUser.size());
        }
    }

    @Override
    public void onUnselectClick() {
        toggledUser.clear();
        for (int idx = 0,size = teamMemberDataModel.getSize(); idx < size; idx++) {
            teamMemberDataModel.getItem(idx).getChatChooseItem().setIsChooseItem(false);
        }
        view.updateToggledUser(toggledUser.size());
        view.refreshDataView();
    }

    @Override
    public void onAddClick() {

        Long[] tempIds = new Long[toggledUser.size()];
        long[] ids = new long[toggledUser.size()];
        toggledUser.toArray(tempIds);

        for (int idx = 0; idx < tempIds.length; idx++) {
            ids[idx] = tempIds[idx];
        }

        view.comeWithResult(ids);
    }

    public void setTypeAndKeyword(int type, String keyword) {
        this.type = type;
        this.keyword = keyword;
    }

    public void setSelectMode(boolean selectMode) {
        this.selectMode = selectMode;
        if (selectMode) {
            toggledUser = new HashSet<>();
        }
    }

    public void setPickMode(boolean pickMode) {
        this.pickMode = pickMode;
    }
}
