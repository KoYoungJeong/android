package com.tosslab.jandi.app.ui.intro.presenter;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.intro.dagger.IntroModule;

import dagger.Component;

@Component(modules = {ApiClientModule.class, IntroModule.class})
public interface IntroTestComponent {
    void inject(IntroActivityPresenterTest test);
}
