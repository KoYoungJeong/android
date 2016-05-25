package com.tosslab.jandi.app.ui.intro.signup.dagger;

import com.tosslab.jandi.app.ui.intro.signup.presenter.MainSignUpImpl;
import com.tosslab.jandi.app.ui.intro.signup.presenter.MainSignUpPresenter;

import dagger.Module;
import dagger.Provides;

@Module
public class MainSignUpModule {

    private MainSignUpPresenter.View view;

    public MainSignUpModule(MainSignUpPresenter.View view) {
        this.view = view;
    }

    @Provides
    MainSignUpPresenter provideMainSignUpPresenter(MainSignUpImpl mainSignUpPresenter) {
        return mainSignUpPresenter;
    }

    @Provides
    MainSignUpPresenter.View provideViewOfMainSignUpPresenter() {
        return view;
    }

}
