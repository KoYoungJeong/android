package com.tosslab.jandi.app.ui.settings.account.dagger;

import com.tosslab.jandi.app.ui.settings.account.presenter.SettingAccountPresenter;
import com.tosslab.jandi.app.ui.settings.account.presenter.SettingAccountPresenterImpl;
import com.tosslab.jandi.app.ui.settings.account.view.SettingAccountView;

import dagger.Module;
import dagger.Provides;

@Module
public class SettingAccountModule {

    private final SettingAccountView view;

    public SettingAccountModule(SettingAccountView view) {
        this.view = view;
    }

    @Provides
    SettingAccountView provideAccountView() {
        return view;
    }

    @Provides
    SettingAccountPresenter presenter(SettingAccountPresenterImpl presenter) {
        return presenter;
    }
}
