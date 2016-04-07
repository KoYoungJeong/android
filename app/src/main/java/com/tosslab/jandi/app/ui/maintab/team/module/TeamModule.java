package com.tosslab.jandi.app.ui.maintab.team.module;

import com.tosslab.jandi.app.ui.maintab.team.model.TeamModel;
import com.tosslab.jandi.app.ui.maintab.team.presenter.TeamPresenter;
import com.tosslab.jandi.app.ui.maintab.team.presenter.TeamPresenterImpl;
import com.tosslab.jandi.app.ui.maintab.team.view.TeamView;
import com.tosslab.jandi.app.ui.members.adapter.searchable.SearchableMemberListAdapter;
import com.tosslab.jandi.app.ui.members.model.MemberSearchableDataModel;
import com.tosslab.jandi.app.ui.members.view.MemberSearchableDataView;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tonyjs on 16. 3. 15..
 */
@Module
public class TeamModule {

    private final SearchableMemberListAdapter searchableMemberListAdapter;
    private final TeamView view;

    public TeamModule(TeamView view, SearchableMemberListAdapter adapter) {
        this.view = view;
        this.searchableMemberListAdapter = adapter;
    }

    @Provides
    public MemberSearchableDataModel provideMemberSearchableDataModel() {
        return searchableMemberListAdapter;
    }

    @Provides
    public MemberSearchableDataView provideMemberSearchableDataView() {
        return searchableMemberListAdapter;
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
