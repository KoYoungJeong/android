package com.tosslab.jandi.app.ui.maintab.dagger;

import com.tosslab.jandi.app.network.client.main.ConfigApi;
import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.ui.maintab.model.MainTabModel;
import com.tosslab.jandi.app.ui.maintab.presenter.MainTabPresenter;
import com.tosslab.jandi.app.ui.maintab.presenter.MainTabPresenterImpl;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;

@Module
public class MainTabModule {

    private final MainTabPresenter.View mainTabView;

    public MainTabModule(MainTabPresenter.View mainTabView) {
        this.mainTabView = mainTabView;
    }

    @Provides
    public MainTabPresenter.View providesMainTabView() {
        return mainTabView;
    }

    @Provides
    public MainTabModel providesMainTabModel(Lazy<ConfigApi> configApi, Lazy<StartApi> startApi) {
        return new MainTabModel(configApi, startApi);
    }

    @Provides
    public MainTabPresenter providesMainTabPresenter(MainTabPresenterImpl mainTabPresenter) {
        return mainTabPresenter;
    }

}
