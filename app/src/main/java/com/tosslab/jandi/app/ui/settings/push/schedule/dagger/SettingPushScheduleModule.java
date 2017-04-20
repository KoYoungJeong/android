package com.tosslab.jandi.app.ui.settings.push.schedule.dagger;

import com.tosslab.jandi.app.ui.settings.push.schedule.presenter.SettingPushSchedulePresenter;
import com.tosslab.jandi.app.ui.settings.push.schedule.presenter.SettingPushSchedulePresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class SettingPushScheduleModule {

    private SettingPushSchedulePresenter.View view;

    public SettingPushScheduleModule(SettingPushSchedulePresenter.View view) {
        this.view = view;
    }

    public SettingPushScheduleModule() {
    }

    @Provides
    SettingPushSchedulePresenter.View provideView() {
        return view;
    }

    @Provides
    SettingPushSchedulePresenter provideSettingsPresenter(
            SettingPushSchedulePresenterImpl SettingPushSchedulePresenter) {
        return SettingPushSchedulePresenter;
    }

}
