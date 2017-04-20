package com.tosslab.jandi.app.ui.settings.push.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.settings.push.SettingsPushFragment;
import com.tosslab.jandi.app.ui.settings.push.schedule.dagger.SettingPushScheduleModule;

import dagger.Component;

@Component(modules = {ApiClientModule.class, SettingsPushModule.class, SettingPushScheduleModule.class})
public interface SettingsPushComponent {
    void inject(SettingsPushFragment fragment);
}
