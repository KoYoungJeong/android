package com.tosslab.jandi.app.ui.maintab.team.module;

import com.tosslab.jandi.app.ui.maintab.team.model.TeamModel;
import com.tosslab.jandi.app.ui.maintab.team.presenter.TeamPresenter;
import com.tosslab.jandi.app.ui.maintab.team.presenter.TeamPresenterImpl;
import com.tosslab.jandi.app.ui.maintab.team.view.TeamView;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tonyjs on 16. 3. 15..
 */
@Module
public class TeamModule {

    private final TeamView view;

    public TeamModule(TeamView view) {
        this.view = view;
    }

    @Provides
    @Singleton
    public TeamModel provideTeamModel() {
        return new TeamModel();
    }

    @Provides
    public TeamView provideTeamView() {
        return view;
    }

    @Provides
    public TeamPresenter provideTeamPresenter(TeamPresenterImpl presenter) {
        return presenter;
    }

}
