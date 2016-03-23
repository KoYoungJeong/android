package com.tosslab.jandi.app.ui.maintab.teams.module;

import com.tosslab.jandi.app.ui.maintab.teams.model.TeamsModel;
import com.tosslab.jandi.app.ui.maintab.teams.presenter.TeamsPresenter;
import com.tosslab.jandi.app.ui.maintab.teams.presenter.TeamsPresenterImpl;
import com.tosslab.jandi.app.ui.maintab.teams.view.TeamsView;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tonyjs on 16. 3. 21..
 */
@Module
@Singleton
public class TeamsModule {

    private final TeamsView view;

    public TeamsModule(TeamsView view) {
        this.view = view;
    }

    @Provides
    @Singleton
    public TeamsModel provideTeamsModel() {
        return new TeamsModel();
    }

    @Provides
    public TeamsView provideTeamsView() {
        return view;
    }

    @Provides
    public TeamsPresenter provideTeamsPresenter(TeamsPresenterImpl presenter) {
        return presenter;
    }
}
