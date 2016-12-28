package com.tosslab.jandi.app.ui.settings.main.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.settings.main.view.SettingsFragment;

import dagger.Component;

@Component(modules = {ApiClientModule.class, SettingsModule.class})
public interface SettingsComponent {
    void inject(SettingsFragment fragment);
}
