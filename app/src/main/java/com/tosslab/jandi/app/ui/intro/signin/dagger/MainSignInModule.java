package com.tosslab.jandi.app.ui.intro.signin.dagger;

import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.account.devices.DeviceApi;
import com.tosslab.jandi.app.network.client.main.LoginApi;
import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.intro.signin.model.MainSignInModel;
import com.tosslab.jandi.app.ui.intro.signin.presenter.MainSignInPresenter;
import com.tosslab.jandi.app.ui.intro.signin.presenter.MainSignInPresenterImpl;

import javax.inject.Singleton;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;

@Module(includes = {ApiClientModule.class})
public class MainSignInModule {

    private MainSignInPresenter.View view;

    public MainSignInModule(MainSignInPresenter.View view) {
        this.view = view;
    }

    @Provides
    @Singleton
    public MainSignInModel provideMainSignInModel(
            Lazy<LoginApi> loginApi, Lazy<AccountApi> accountApi, Lazy<DeviceApi> deviceApi) {
        return new MainSignInModel(loginApi, accountApi, deviceApi);
    }

    @Provides
    MainSignInPresenter.View provideMainSignInViewInPresenter() {
        return view;
    }

    @Provides
    @Singleton
    public MainSignInPresenter provideMainSignInPresenter(MainSignInPresenterImpl mainSignInPresenter) {
        return mainSignInPresenter;
    }

}
