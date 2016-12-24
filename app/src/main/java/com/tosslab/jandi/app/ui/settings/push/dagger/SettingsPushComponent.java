package com.tosslab.jandi.app.ui.settings.push.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.settings.push.SettingsPushFragment;

import dagger.Component;

@Component(modules = {ApiClientModule.class, SettingsPushModule.class})
public interface SettingsPushComponent {
    void inject(SettingsPushFragment fragment);
}
