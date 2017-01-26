package com.tosslab.jandi.app.ui.sign.signin.dagger;

import com.tosslab.jandi.app.ui.sign.signin.presenter.SignInPresenter;
import com.tosslab.jandi.app.ui.sign.signin.presenter.SignInPresenterImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class SignInModule {

    private SignInPresenter.View view;

    public SignInModule(SignInPresenter.View view) {
        this.view = view;
    }

    @Provides
    SignInPresenter.View provideMainSignInViewInPresenter() {
        return view;
    }

    @Provides
    @Singleton
    public SignInPresenter provideMainSignInPresenter(SignInPresenterImpl mainSignInPresenter) {
        return mainSignInPresenter;
    }

}
