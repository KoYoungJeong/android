package com.tosslab.jandi.app.ui.profile.account.dagger;

import com.tosslab.jandi.app.ui.profile.account.presenter.SettingAccountProfilePresenter;
import com.tosslab.jandi.app.ui.profile.account.presenter.SettingAccountProfilePresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class SettingAccountProfileModule {

    SettingAccountProfilePresenter.View view;

    public SettingAccountProfileModule(SettingAccountProfilePresenter.View view) {
        this.view = view;
    }

    @Provides
    SettingAccountProfilePresenter.View view() {
        return view;
    }

    @Provides
    SettingAccountProfilePresenter presenter(SettingAccountProfilePresenterImpl presenter) {
        return presenter;
    }

}
