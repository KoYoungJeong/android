package com.tosslab.jandi.app.ui.maintab.team.filter.deptgroup.dagger;


import com.tosslab.jandi.app.ui.maintab.team.filter.deptgroup.presenter.DeptJobGroupPresenter;
import com.tosslab.jandi.app.ui.maintab.team.filter.deptgroup.presenter.DeptJobGroupPresenterImpl;
import com.tosslab.jandi.app.ui.maintab.team.filter.member.adapter.TeamMemberAdapter;
import com.tosslab.jandi.app.ui.maintab.team.filter.member.adapter.TeamMemberDataModel;
import com.tosslab.jandi.app.ui.maintab.team.filter.member.adapter.TeamMemberDataView;

import dagger.Module;
import dagger.Provides;

@Module
public class DeptJobGroupModule {
    private final DeptJobGroupPresenter.View view;
    private final TeamMemberAdapter teamMemberAdapter;
    private final int type;
    private final String keyword;
    private final boolean selectMode;

    public DeptJobGroupModule(DeptJobGroupPresenter.View view, TeamMemberAdapter teamMemberAdapter,
                              int type,
                              String keyword,
                              boolean selectMode) {
        this.view = view;
        this.teamMemberAdapter = teamMemberAdapter;
        this.type = type;
        this.keyword = keyword;
        this.selectMode = selectMode;
    }

    @Provides
    TeamMemberDataModel teamMemberDataModel() {
        return teamMemberAdapter;
    }

    @Provides
    TeamMemberDataView teamMemberDataView() {
        return teamMemberAdapter;
    }

    @Provides
    DeptJobGroupPresenter.View view() {
        return view;
    }

    @Provides
    DeptJobGroupPresenter presenter(DeptJobGroupPresenterImpl presenter) {
        presenter.setTypeAndKeyword(type, keyword);
        presenter.setSelectMode(selectMode);
        return presenter;
    }
}
