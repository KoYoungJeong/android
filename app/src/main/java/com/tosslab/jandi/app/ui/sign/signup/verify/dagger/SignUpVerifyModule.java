package com.tosslab.jandi.app.ui.sign.signup.verify.dagger;


import com.tosslab.jandi.app.ui.sign.signup.verify.view.SignUpVerifyView;

import dagger.Module;
import dagger.Provides;

@Module
public class SignUpVerifyModule {
    private final SignUpVerifyView view;


    public SignUpVerifyModule(SignUpVerifyView view) {
        this.view = view;
    }

    @Provides
    SignUpVerifyView view() {
        return view;
    }
}
