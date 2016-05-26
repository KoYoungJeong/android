package com.tosslab.jandi.app.ui.intro.signup.dagger;

import dagger.Component;

@Component(modules = MainSignUpModule.class)
interface MainSignUpComponent {
    void inject(MainSignUpComponent activity);
}
