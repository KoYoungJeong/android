package com.tosslab.jandi.app.ui.settings.main.dagger;

import com.tosslab.jandi.app.ui.settings.main.presenter.SettingsPresenter;
import com.tosslab.jandi.app.ui.settings.main.presenter.SettingsPresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class SettingsModule {

    private SettingsPresenter.View view;

    public SettingsModule(SettingsPresenter.View view) {this.view = view;}

    @Provides
    SettingsPresenter.View provideView() {
        return view;
    }

    @Provides
    SettingsPresenter provideSettingsPresenter(SettingsPresenterImpl settingsPresenter) {
        return settingsPresenter;
    }
}
