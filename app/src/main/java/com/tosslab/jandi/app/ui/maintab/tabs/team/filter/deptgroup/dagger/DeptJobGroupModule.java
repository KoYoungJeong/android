package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.deptgroup.dagger;


import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.deptgroup.presenter.DeptJobGroupPresenter;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.deptgroup.presenter.DeptJobGroupPresenterImpl;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter.TeamMemberAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter.TeamMemberDataModel;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter.TeamMemberDataView;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter.ToggleCollector;

import dagger.Module;
import dagger.Provides;

@Module
public class DeptJobGroupModule {
    private final DeptJobGroupPresenter.View view;
    private final TeamMemberAdapter teamMemberAdapter;
    private final int type;
    private final String keyword;
    private final boolean selectMode;
    private final boolean pickMode;
    private final long roomId;

    public DeptJobGroupModule(DeptJobGroupPresenter.View view, TeamMemberAdapter teamMemberAdapter,
                              int type,
                              String keyword,
                              boolean selectMode, boolean pickMode, long roomId) {
        this.view = view;
        this.teamMemberAdapter = teamMemberAdapter;
        this.type = type;
        this.keyword = keyword;
        this.selectMode = selectMode;
        this.pickMode = pickMode;
        this.roomId = roomId;
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
    ToggleCollector toggleCollector() {
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
        presenter.setPickMode(pickMode);
        presenter.setRoomId(roomId);
        return presenter;
    }
}
