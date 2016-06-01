package com.tosslab.jandi.app.ui.intro.signup.dagger;

import com.tosslab.jandi.app.ui.intro.signup.MainSignUpActivity;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = MainSignUpModule.class)
@Singleton
public interface MainSignUpComponent {
    void inject(MainSignUpActivity activity);
}
