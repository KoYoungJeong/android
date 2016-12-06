package com.tosslab.jandi.app.ui.team.select.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.team.select.adapter.TeamSelectListAdapter;
import com.tosslab.jandi.app.ui.team.select.adapter.datamodel.TeamSelectListAdapterDataModel;
import com.tosslab.jandi.app.ui.team.select.adapter.viewmodel.TeamSelectListAdapterViewModel;
import com.tosslab.jandi.app.ui.team.select.presenter.TeamSelectListPresenter;
import com.tosslab.jandi.app.ui.team.select.presenter.TeamSelectListPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tee on 2016. 9. 27..
 */

@Module(includes = {ApiClientModule.class})
public class TeamSelectListModule {

    private TeamSelectListPresenter.View view;
    private TeamSelectListAdapter adapter;

    public TeamSelectListModule(TeamSelectListPresenter.View view, TeamSelectListAdapter adapter) {
        this.view = view;
        this.adapter = adapter;
    }

    @Provides
    public TeamSelectListPresenter.View provideTeamChoiceListViewInPresenter() {
        return view;
    }

    @Provides
    public TeamSelectListAdapterDataModel provideTeamSelectListAdapterDataModel() {
        return adapter;
    }

    @Provides
    public TeamSelectListAdapterViewModel provideTeamSelectListAdapterViewModel() {
        return adapter;
    }

    @Provides
    public TeamSelectListPresenter proviceTeamChoiceListPresenter(TeamSelectListPresenterImpl presenter) {
        return presenter;
    }

}
