package com.tosslab.jandi.app.ui.maintab.team.filter.deptgroup.presenter;

import android.text.TextUtils;

import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.maintab.team.filter.dept.DeptJobFragment;
import com.tosslab.jandi.app.ui.maintab.team.filter.member.adapter.TeamMemberDataModel;
import com.tosslab.jandi.app.ui.maintab.team.filter.member.domain.TeamMemberItem;

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

    @Inject
    public DeptJobGroupPresenterImpl(View view, TeamMemberDataModel teamMemberDataModel) {
        this.view = view;
        this.teamMemberDataModel = teamMemberDataModel;
    }

    @Override
    public void onCreate() {
        Observable.from(TeamInfoLoader.getInstance().getUserList())
                .filter(User::isEnabled)
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
        view.moveMemberProfile(item.getChatChooseItem().getEntityId());
    }

    public void setTypeAndKeyword(int type, String keyword) {
        this.type = type;
        this.keyword = keyword;
    }
}
