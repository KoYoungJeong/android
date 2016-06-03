package com.tosslab.jandi.app.ui.sign.signup.dagger;

import com.tosslab.jandi.app.network.client.main.SignUpApi;
import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.sign.signup.model.SignUpModel;
import com.tosslab.jandi.app.ui.sign.signup.presenter.SignUpPresenter;
import com.tosslab.jandi.app.ui.sign.signup.presenter.SignUpPresenterImpl;

import javax.inject.Singleton;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;

@Module(includes = {ApiClientModule.class})
public class SignUpModule {

    private SignUpPresenter.View view;

    public SignUpModule(SignUpPresenter.View view) {
        this.view = view;
    }

    @Provides
    @Singleton
    public SignUpModel provideMainSignUpModel(Lazy<SignUpApi> signUpApi) {
        return new SignUpModel(signUpApi);
    }

    @Provides
    SignUpPresenter.View provideViewOfMainSignUpPresenter() {
        return view;
    }

    @Provides
    SignUpPresenter provideMainSignUpPresenter(SignUpPresenterImpl mainSignUpPresenter) {
        return mainSignUpPresenter;
    }

}
