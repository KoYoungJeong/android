package com.tosslab.jandi.app.ui.entities.disabled.dagger;


import com.tosslab.jandi.app.ui.entities.disabled.presenter.DisabledEntityChoosePresenter;
import com.tosslab.jandi.app.ui.entities.disabled.presenter.DisabledEntityChoosePresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class DisabledEntityChooseModule {
    private final DisabledEntityChoosePresenter.View view;

    public DisabledEntityChooseModule(DisabledEntityChoosePresenter.View view) {
        this.view = view;
    }

    @Provides
    DisabledEntityChoosePresenter.View view() {
        return view;
    }

    @Provides
    DisabledEntityChoosePresenter presenter(DisabledEntityChoosePresenterImpl presenter) {
        return presenter;
    }
}
