package com.tosslab.jandi.app.ui.profile.modify.property.namestatus.dagger;


import com.tosslab.jandi.app.ui.profile.modify.property.namestatus.presenter.NameStatusPresenter;

import dagger.Module;
import dagger.Provides;

@Module
public class NameStatusModule {
    private NameStatusPresenter.View view;

    public NameStatusModule(NameStatusPresenter.View view) {this.view = view;}

    @Provides
    NameStatusPresenter.View provideView() {
        return view;
    }
}
