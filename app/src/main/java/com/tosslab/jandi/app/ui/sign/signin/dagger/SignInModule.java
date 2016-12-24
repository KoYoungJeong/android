package com.tosslab.jandi.app.ui.sign.signin.dagger;

import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.account.devices.DeviceApi;
import com.tosslab.jandi.app.network.client.main.LoginApi;
import com.tosslab.jandi.app.ui.sign.signin.model.SignInModel;
import com.tosslab.jandi.app.ui.sign.signin.presenter.SignInPresenter;
import com.tosslab.jandi.app.ui.sign.signin.presenter.SignInPresenterImpl;

import javax.inject.Singleton;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;

@Module
public class SignInModule {

    private SignInPresenter.View view;

    public SignInModule(SignInPresenter.View view) {
        this.view = view;
    }

    @Provides
    @Singleton
    public SignInModel provideMainSignInModel(
            Lazy<LoginApi> loginApi, Lazy<AccountApi> accountApi, Lazy<DeviceApi> deviceApi) {
        return new SignInModel(loginApi, accountApi, deviceApi);
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
