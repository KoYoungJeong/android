package com.tosslab.jandi.app.ui.profile.defaultimage.dagger;


import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.profile.defaultimage.ProfileImageSelectorActivity;

import dagger.Component;

@Component(modules = {ProfileImageSelectorModule.class, ApiClientModule.class})
public interface ProfileImageSelectorComponent {
    void inject(ProfileImageSelectorActivity activity);
}
