package com.tosslab.jandi.app.ui.sign.signup.dagger;

import com.tosslab.jandi.app.ui.sign.signup.SignUpActivity;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = SignUpModule.class)
@Singleton
public interface SignUpComponent {
    void inject(SignUpActivity activity);
}
