package com.tosslab.jandi.app.ui.sign.signup.dagger;

import com.tosslab.jandi.app.ui.sign.signup.presenter.SignUpPresenter;
import com.tosslab.jandi.app.ui.sign.signup.presenter.SignUpPresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class SignUpModule {

    private SignUpPresenter.View view;

    public SignUpModule(SignUpPresenter.View view) {
        this.view = view;
    }

    @Provides
    SignUpPresenter.View view() {
        return view;
    }

    @Provides
    SignUpPresenter presenter(SignUpPresenterImpl mainSignUpPresenter) {
        return mainSignUpPresenter;
    }

}
