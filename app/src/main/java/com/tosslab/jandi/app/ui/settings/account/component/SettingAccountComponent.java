package com.tosslab.jandi.app.ui.settings.account.component;

import com.tosslab.jandi.app.ui.settings.account.SettingAccountActivity;
import com.tosslab.jandi.app.ui.settings.account.module.SettingAccountModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by tonyjs on 16. 3. 23..
 */
@Component(modules = SettingAccountModule.class)
@Singleton
public interface SettingAccountComponent {

    void inject(SettingAccountActivity activity);

}
