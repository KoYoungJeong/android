package com.tosslab.jandi.app.ui.intro.signin.dagger;

import com.tosslab.jandi.app.ui.intro.signin.MainSignInActivity;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = {MainSignInModule.class})
@Singleton
public interface MainSignInComponent {
    void inject(MainSignInActivity activity);
}
