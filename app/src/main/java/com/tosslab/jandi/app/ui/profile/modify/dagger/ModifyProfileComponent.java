package com.tosslab.jandi.app.ui.profile.modify.dagger;

import com.tosslab.jandi.app.ui.profile.modify.view.ModifyProfileActivity;

import dagger.Component;

@Component(modules = ModifyProfileModule.class)
public interface ModifyProfileComponent {
    void inject(ModifyProfileActivity activity);
}
