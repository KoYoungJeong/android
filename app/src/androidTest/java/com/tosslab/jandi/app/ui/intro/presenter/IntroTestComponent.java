package com.tosslab.jandi.app.ui.intro.presenter;

import com.tosslab.jandi.app.ui.intro.dagger.IntroModule;

import dagger.Component;

@Component(modules = IntroModule.class)
public interface IntroTestComponent {
    void inject(IntroActivityPresenterTest test);
}
