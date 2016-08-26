package com.tosslab.jandi.app.ui.maintab.navigation.module;

import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.invitation.InvitationApi;
import com.tosslab.jandi.app.network.client.main.LoginApi;
import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.maintab.navigation.adapter.NavigationAdapter;
import com.tosslab.jandi.app.ui.maintab.navigation.adapter.model.NavigationDataModel;
import com.tosslab.jandi.app.ui.maintab.navigation.adapter.view.NavigationDataView;
import com.tosslab.jandi.app.ui.maintab.navigation.model.NavigationModel;
import com.tosslab.jandi.app.ui.maintab.navigation.presenter.NavigationPresenter;
import com.tosslab.jandi.app.ui.maintab.navigation.presenter.NavigationPresenterImpl;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;

/**
 * Created by tonyjs on 2016. 8. 17..
 */
@Module(includes = ApiClientModule.class)
public class NavigationModule {

    private final NavigationAdapter navigationAdapter;
    private final NavigationPresenter.View navigationView;

    public NavigationModule(NavigationAdapter navigationAdapter,
                            NavigationPresenter.View navigationView) {
        this.navigationAdapter = navigationAdapter;
        this.navigationView = navigationView;
    }

    @Provides
    public NavigationModel providesNavigationModel(Lazy<AccountApi> accountApi,
                                                   Lazy<StartApi> startApi,
                                                   Lazy<InvitationApi> invitationApi,
                                                   Lazy<LoginApi> loginApi) {
        return new NavigationModel(accountApi, startApi, invitationApi, loginApi);
    }

    @Provides
    public NavigationPresenter providesNavigationPresenter(NavigationPresenterImpl navigationPresenter) {
        return navigationPresenter;
    }

    @Provides
    public NavigationDataView providesNavigationDataView() {
        return navigationAdapter;
    }

    @Provides
    public NavigationDataModel providesNavigationDataModel() {
        return navigationAdapter;
    }

    @Provides
    public NavigationPresenter.View providesNavigationView() {
        return navigationView;
    }

}
