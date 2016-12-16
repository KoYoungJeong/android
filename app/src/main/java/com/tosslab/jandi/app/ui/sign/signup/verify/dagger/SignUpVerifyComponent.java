package com.tosslab.jandi.app.ui.sign.signup.verify.dagger;


import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.sign.signup.verify.SignUpVerifyActivity;

import dagger.Component;

@Component(modules = {ApiClientModule.class, SignUpVerifyModule.class})
public interface SignUpVerifyComponent {
    void inject(SignUpVerifyActivity activity);
}
