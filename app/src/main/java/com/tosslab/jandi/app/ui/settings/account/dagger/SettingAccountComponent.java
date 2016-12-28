package com.tosslab.jandi.app.ui.settings.account.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.settings.account.SettingAccountActivity;

import dagger.Component;

@Component(modules = {ApiClientModule.class, SettingAccountModule.class})
public interface SettingAccountComponent {
    void inject(SettingAccountActivity activity);
}
