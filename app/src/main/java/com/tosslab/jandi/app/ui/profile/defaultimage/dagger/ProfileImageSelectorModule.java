package com.tosslab.jandi.app.ui.profile.defaultimage.dagger;


import com.tosslab.jandi.app.ui.profile.defaultimage.presenter.ProfileImageSelectorPresenter;

import dagger.Module;
import dagger.Provides;

@Module
public class ProfileImageSelectorModule {
    private final ProfileImageSelectorPresenter.View view;

    public ProfileImageSelectorModule(ProfileImageSelectorPresenter.View view) {
        this.view = view;
    }

    @Provides
    ProfileImageSelectorPresenter.View view() {
        return view;
    }
}
