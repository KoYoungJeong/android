package com.tosslab.jandi.app.ui.maintab.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity;

import dagger.Component;

@Component(modules = {ApiClientModule.class, MainTabModule.class})
public interface MainTabComponent {

    void inject(MainTabActivity activity);
}
