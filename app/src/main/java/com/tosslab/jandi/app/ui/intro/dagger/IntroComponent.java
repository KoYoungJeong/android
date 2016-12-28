package com.tosslab.jandi.app.ui.intro.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.intro.IntroActivity;

import dagger.Component;

@Component(modules = {ApiClientModule.class, IntroModule.class})
public interface IntroComponent {
    void inject(IntroActivity introActivity);
}
