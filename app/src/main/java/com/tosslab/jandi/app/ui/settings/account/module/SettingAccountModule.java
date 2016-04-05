package com.tosslab.jandi.app.ui.settings.account.module;

import com.tosslab.jandi.app.network.client.settings.AccountProfileApi;
import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.settings.account.model.SettingAccountModel;
import com.tosslab.jandi.app.ui.settings.account.presenter.SettingAccountPresenter;
import com.tosslab.jandi.app.ui.settings.account.presenter.SettingAccountPresenterImpl;
import com.tosslab.jandi.app.ui.settings.account.view.SettingAccountView;

import javax.inject.Singleton;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;

/**
 * Created by tonyjs on 16. 3. 23..
 */
@Module(includes = ApiClientModule.class)
@Singleton
public class SettingAccountModule {

    private final SettingAccountView view;

    public SettingAccountModule(SettingAccountView view) {
        this.view = view;
    }

    @Provides
    public SettingAccountView provideAccountView() {
        return view;
    }

    @Provides
    public SettingAccountModel provideSettingAccountModel(Lazy<AccountProfileApi> accountProfileApi) {
        return new SettingAccountModel(accountProfileApi);
    }

    @Provides
    public SettingAccountPresenter provideSettingAccountPresenter(SettingAccountModel model, SettingAccountView view) {
        return new SettingAccountPresenterImpl(model, view);
    }

}
