package com.tosslab.jandi.app.ui.profile.insert.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.profile.insert.views.InsertProfileFirstPageFragment;

import dagger.Component;

@Component(modules = {ApiClientModule.class, InsertProfileFirstPageModule.class})
public interface InsertProfileFirstPageComponent {
    void inject(InsertProfileFirstPageFragment fragment);
}
