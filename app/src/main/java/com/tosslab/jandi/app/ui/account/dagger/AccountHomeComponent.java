package com.tosslab.jandi.app.ui.account.dagger;

import com.tosslab.jandi.app.ui.account.AccountHomeActivity;

import dagger.Component;

@Component(modules = AccountHomeModule.class)
public interface AccountHomeComponent {
    void inject(AccountHomeActivity activity);
}
