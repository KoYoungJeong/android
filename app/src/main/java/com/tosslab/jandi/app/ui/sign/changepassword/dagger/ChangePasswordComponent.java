package com.tosslab.jandi.app.ui.sign.changepassword.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.sign.changepassword.ChangePasswordActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by tee on 2017. 4. 11..
 */

@Component(modules = {ApiClientModule.class, ChangePasswordModule.class})
@Singleton
public interface ChangePasswordComponent {
    void inject(ChangePasswordActivity activity);
}
