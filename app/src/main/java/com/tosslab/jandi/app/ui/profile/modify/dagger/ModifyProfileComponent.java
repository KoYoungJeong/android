package com.tosslab.jandi.app.ui.profile.modify.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.profile.modify.view.ModifyProfileActivity;

import dagger.Component;

@Component(modules = {ModifyProfileModule.class, ApiClientModule.class})
public interface ModifyProfileComponent {
    void inject(ModifyProfileActivity activity);
}
