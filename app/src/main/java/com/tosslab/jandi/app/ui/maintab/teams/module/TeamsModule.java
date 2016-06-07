package com.tosslab.jandi.app.ui.maintab.teams.module;

import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.invitation.InvitationApi;
import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.maintab.teams.model.TeamsModel;
import com.tosslab.jandi.app.ui.maintab.teams.presenter.TeamsPresenter;
import com.tosslab.jandi.app.ui.maintab.teams.presenter.TeamsPresenterImpl;
import com.tosslab.jandi.app.ui.maintab.teams.view.TeamsView;

import javax.inject.Singleton;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;

/**
 * Created by tonyjs on 16. 3. 21..
 */
@Module(includes = ApiClientModule.class)
@Singleton
public class TeamsModule {

    private final TeamsView view;

    public TeamsModule(TeamsView view) {
        this.view = view;
    }

    @Provides
    @Singleton
    public TeamsModel provideTeamsModel(Lazy<AccountApi> accountApi,
                                        Lazy<StartApi> startApi,
                                        Lazy<InvitationApi> invitationApi) {
        return new TeamsModel(accountApi, startApi, invitationApi);
    }

    @Provides
    public TeamsView provideTeamsView() {
        return view;
    }

    @Provides
    public TeamsPresenter provideTeamsPresenter(TeamsModel model, TeamsView view) {
        return new TeamsPresenterImpl(model, view);
    }
}
