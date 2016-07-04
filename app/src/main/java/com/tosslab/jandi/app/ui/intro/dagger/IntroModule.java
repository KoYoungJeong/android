package com.tosslab.jandi.app.ui.intro.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.intro.presenter.IntroActivityPresenter;

import dagger.Module;
import dagger.Provides;

@Module(includes = ApiClientModule.class)
public class IntroModule {
    private IntroActivityPresenter.View view;

    public IntroModule(IntroActivityPresenter.View view) {this.view = view;}

    @Provides
    IntroActivityPresenter.View provideView() {
        return view;
    }
}
