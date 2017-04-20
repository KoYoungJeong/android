package com.tosslab.jandi.app.ui.settings.push.schedule.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.settings.push.schedule.SettingPushScheduleActivity;

import dagger.Component;

@Component(modules = {ApiClientModule.class, SettingPushScheduleModule.class})
public interface SettingPushScheduleComponent {
    void inject(SettingPushScheduleActivity activity);
}
