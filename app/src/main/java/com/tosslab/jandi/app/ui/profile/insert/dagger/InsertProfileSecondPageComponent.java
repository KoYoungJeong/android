package com.tosslab.jandi.app.ui.profile.insert.dagger;

import com.tosslab.jandi.app.ui.profile.insert.views.InsertProfileSecondPageFragment;

import dagger.Component;

@Component(modules = InsertProfileSecondPageModule.class)
public interface InsertProfileSecondPageComponent {
    void inject(InsertProfileSecondPageFragment fragment);
}
