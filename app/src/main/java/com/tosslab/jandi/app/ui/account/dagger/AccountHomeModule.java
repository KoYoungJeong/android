package com.tosslab.jandi.app.ui.account.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.account.presenter.AccountHomePresenter;
import com.tosslab.jandi.app.ui.account.presenter.AccountHomePresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module(includes = ApiClientModule.class)
public class AccountHomeModule {

    private final AccountHomePresenter.View view;

    public AccountHomeModule(AccountHomePresenter.View view) {
        this.view = view;
    }

    @Provides
    AccountHomePresenter.View provideView() {
        return view;
    }

    @Provides
    AccountHomePresenter provideAccountHomePresenter(AccountHomePresenterImpl presenter) {
        return presenter;
    }
}
