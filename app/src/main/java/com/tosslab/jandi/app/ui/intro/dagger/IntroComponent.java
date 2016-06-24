package com.tosslab.jandi.app.ui.intro.dagger;

import com.tosslab.jandi.app.ui.intro.IntroActivity;

import dagger.Component;

@Component(modules = IntroModule.class)
public interface IntroComponent {
    void inject(IntroActivity introActivity);
}
