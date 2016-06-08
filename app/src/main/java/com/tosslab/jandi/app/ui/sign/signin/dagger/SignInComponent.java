package com.tosslab.jandi.app.ui.sign.signin.dagger;

import com.tosslab.jandi.app.ui.sign.signin.SignInActivity;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = {SignInModule.class})
@Singleton
public interface SignInComponent {
    void inject(SignInActivity activity);
}
