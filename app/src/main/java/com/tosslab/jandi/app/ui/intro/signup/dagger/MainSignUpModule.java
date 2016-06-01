package com.tosslab.jandi.app.ui.intro.signup.dagger;

import com.tosslab.jandi.app.network.client.main.SignUpApi;
import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.intro.signup.model.MainSignUpModel;
import com.tosslab.jandi.app.ui.intro.signup.presenter.MainSignUpPresenter;
import com.tosslab.jandi.app.ui.intro.signup.presenter.MainSignUpPresenterImpl;

import javax.inject.Singleton;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;

@Module(includes = {ApiClientModule.class})
public class MainSignUpModule {

    private MainSignUpPresenter.View view;

    public MainSignUpModule(MainSignUpPresenter.View view) {
        this.view = view;
    }

    @Provides
    @Singleton
    public MainSignUpModel provideMainSignUpModel(Lazy<SignUpApi> signUpApi) {
        return new MainSignUpModel(signUpApi);
    }

    @Provides
    MainSignUpPresenter.View provideViewOfMainSignUpPresenter() {
        return view;
    }

    @Provides
    @Singleton
    MainSignUpPresenter provideMainSignUpPresenter(MainSignUpPresenterImpl mainSignUpPresenter) {
        return mainSignUpPresenter;
    }


}
