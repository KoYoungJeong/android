package com.tosslab.jandi.app.ui.settings.push.dagger;

import com.tosslab.jandi.app.ui.settings.push.SettingsPushFragment;

import dagger.Component;

@Component(modules = SettingsPushModule.class)
public interface SettingsPushComponent {
    void inject(SettingsPushFragment fragment);
}
