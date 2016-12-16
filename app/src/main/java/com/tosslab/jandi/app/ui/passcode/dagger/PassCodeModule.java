package com.tosslab.jandi.app.ui.passcode.dagger;

import com.tosslab.jandi.app.ui.passcode.presenter.PassCodePresenter;

import dagger.Module;
import dagger.Provides;

@Module
public class PassCodeModule {
    private final PassCodePresenter.View view;

    public PassCodeModule(PassCodePresenter.View view) {
        this.view = view;
    }

    @Provides
    PassCodePresenter.View view() {
        return view;
    }
}
