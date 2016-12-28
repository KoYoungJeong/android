package com.tosslab.jandi.app.ui.profile.insert.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.profile.insert.views.InsertProfileSecondPageFragment;

import dagger.Component;

@Component(modules = {ApiClientModule.class, InsertProfileSecondPageModule.class})
public interface InsertProfileSecondPageComponent {
    void inject(InsertProfileSecondPageFragment fragment);
}
