package com.tosslab.jandi.app.ui.profile.account.dagger;

import com.tosslab.jandi.app.ui.profile.account.SettingAccountProfileActivity;

import dagger.Component;

/**
 * Created by tee on 2016. 9. 30..
 */

@Component(modules = SettingAccountProfileModule.class)
public interface SettingAccountProfileComponent {
    void inject(SettingAccountProfileActivity activity);
}
