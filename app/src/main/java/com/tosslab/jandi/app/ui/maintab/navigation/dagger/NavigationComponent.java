package com.tosslab.jandi.app.ui.maintab.navigation.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.maintab.navigation.NavigationFragment;

import dagger.Component;

@Component(modules = {ApiClientModule.class, NavigationModule.class})
public interface NavigationComponent {

    void inject(NavigationFragment fragment);

}
