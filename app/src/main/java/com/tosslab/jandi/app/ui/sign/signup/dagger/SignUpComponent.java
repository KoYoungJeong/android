package com.tosslab.jandi.app.ui.sign.signup.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.sign.signup.SignUpActivity;

import dagger.Component;

@Component(modules = {ApiClientModule.class, SignUpModule.class})
public interface SignUpComponent {
    void inject(SignUpActivity activity);
}
