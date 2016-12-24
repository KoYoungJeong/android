package com.tosslab.jandi.app.ui.maintab.navigation.dagger;

import com.tosslab.jandi.app.ui.maintab.navigation.adapter.NavigationAdapter;
import com.tosslab.jandi.app.ui.maintab.navigation.adapter.model.NavigationDataModel;
import com.tosslab.jandi.app.ui.maintab.navigation.adapter.view.NavigationDataView;
import com.tosslab.jandi.app.ui.maintab.navigation.presenter.NavigationPresenter;
import com.tosslab.jandi.app.ui.maintab.navigation.presenter.NavigationPresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class NavigationModule {

    private final NavigationAdapter navigationAdapter;
    private final NavigationPresenter.View navigationView;

    public NavigationModule(NavigationAdapter navigationAdapter,
                            NavigationPresenter.View navigationView) {
        this.navigationAdapter = navigationAdapter;
        this.navigationView = navigationView;
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
