package com.tosslab.jandi.app.ui.settings.account.module;

import com.tosslab.jandi.app.ui.settings.account.model.SettingAccountModel;
import com.tosslab.jandi.app.ui.settings.account.presenter.SettingAccountPresenter;
import com.tosslab.jandi.app.ui.settings.account.presenter.SettingAccountPresenterImpl;
import com.tosslab.jandi.app.ui.settings.account.view.SettingAccountView;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tonyjs on 16. 3. 23..
 */
@Module
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
    @Singleton
    public SettingAccountModel provideSettingAccountModel() {
        return new SettingAccountModel();
    }

    @Provides
    public SettingAccountPresenter provideSettingAccountPresenter(SettingAccountPresenterImpl presenter) {
        return presenter;
    }

}
