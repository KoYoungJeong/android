package com.tosslab.jandi.app.ui.team.create.teaminfo.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.team.create.teaminfo.presenter.InsertTeamInfoPresenter;
import com.tosslab.jandi.app.ui.team.create.teaminfo.presenter.InsertTeamInfoPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tee on 16. 6. 24..
 */

@Module(includes = ApiClientModule.class)
public class InsertTeamInfoModule {

    private InsertTeamInfoPresenter.View view;

    public InsertTeamInfoModule(InsertTeamInfoPresenter.View view) {
        this.view = view;
    }

    @Provides
    InsertTeamInfoPresenter.View provideViewOfTeamInsertInfoPresenter() {
        return view;
    }

    @Provides
    InsertTeamInfoPresenter ProvideTeamInsertInfoPresenter(
            InsertTeamInfoPresenterImpl presenter) {
        return presenter;
    }

}
