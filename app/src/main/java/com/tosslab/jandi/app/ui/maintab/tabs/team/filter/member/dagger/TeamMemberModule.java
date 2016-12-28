package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.dagger;


import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter.TeamMemberDataModel;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter.ToggleCollector;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.presenter.TeamMemberPresenter;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.presenter.TeamMemberPresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class TeamMemberModule {
    private final TeamMemberPresenter.View view;
    private final TeamMemberDataModel teamMemberDataModel;
    private final ToggleCollector toggleCollector;
    private final boolean selectMode;
    private final long roomId;

    public TeamMemberModule(TeamMemberPresenter.View view,
                            TeamMemberDataModel teamMemberDataModel,
                            ToggleCollector toggleCollector,
                            boolean selectMode,
                            long roomId) {
        this.view = view;
        this.teamMemberDataModel = teamMemberDataModel;
        this.toggleCollector = toggleCollector;
        this.selectMode = selectMode;
        this.roomId = roomId;
    }

    @Provides
    TeamMemberDataModel provideTeamMemberDataModel() {
        return teamMemberDataModel;
    }

    @Provides
    TeamMemberPresenter.View provideView() {
        return view;
    }

    @Provides
    ToggleCollector provideToggleCollector() {
        return toggleCollector;
    }

    @Provides
    TeamMemberPresenter providePresenter(TeamMemberPresenterImpl presenter) {
        presenter.setSelectMode(selectMode);
        presenter.setRoomId(roomId);
        return presenter;
    }
}
