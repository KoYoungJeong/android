package com.tosslab.jandi.app.ui.sign.changepassword.dagger;

import com.tosslab.jandi.app.ui.sign.changepassword.presenter.ChangePasswordPresenter;
import com.tosslab.jandi.app.ui.sign.changepassword.presenter.ChangePasswordPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tee on 2017. 4. 11..
 */

@Module
public class ChangePasswordModule {
    private ChangePasswordPresenter.View view;

    public ChangePasswordModule(ChangePasswordPresenter.View view) {
        this.view = view;
    }

    @Provides
    ChangePasswordPresenter.View providResetPasswordPresenter() {
        return view;
    }

    @Provides
    public ChangePasswordPresenter provideMainSignInPresenter(ChangePasswordPresenterImpl resetPasswordPresenter) {
        return resetPasswordPresenter;
    }
}