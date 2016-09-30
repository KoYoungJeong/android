package com.tosslab.jandi.app.ui.profile.account.dagger;

import com.tosslab.jandi.app.network.client.main.LoginApi;
import com.tosslab.jandi.app.network.client.settings.AccountProfileApi;
import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.profile.account.model.SettingAccountProfileModel;
import com.tosslab.jandi.app.ui.profile.account.presenter.SettingAccountProfilePresenter;
import com.tosslab.jandi.app.ui.profile.account.presenter.SettingAccountProfilePresenterImpl;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;

/**
 * Created by tee on 2016. 9. 30..
 */

@Module(includes = ApiClientModule.class)
public class SettingAccountProfileModule {

    SettingAccountProfilePresenter.View view;

    public SettingAccountProfileModule(SettingAccountProfilePresenter.View view) {
        this.view = view;
    }

    @Provides
    SettingAccountProfilePresenter.View provideView() {
        return view;
    }

    @Provides
    SettingAccountProfileModel provideSettingAccountProfileModel(Lazy<LoginApi> loginApi,
                                                                 Lazy<AccountProfileApi> accountProfileApi) {
        return new SettingAccountProfileModel(loginApi, accountProfileApi);
    }

    @Provides
    SettingAccountProfilePresenter provideSettingAccountProfilePresenter(
            SettingAccountProfilePresenterImpl presenter) {
        return presenter;
    }

}
