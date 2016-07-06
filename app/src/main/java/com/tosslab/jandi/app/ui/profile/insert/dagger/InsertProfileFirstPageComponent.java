package com.tosslab.jandi.app.ui.profile.insert.dagger;

import com.tosslab.jandi.app.ui.profile.insert.views.InsertProfileFirstPageFragment;

import dagger.Component;

@Component(modules = InsertProfileFirstPageModule.class)
public interface InsertProfileFirstPageComponent {
    void inject(InsertProfileFirstPageFragment fragment);
}
