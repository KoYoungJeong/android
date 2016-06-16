package com.tosslab.jandi.app.ui.profile.modify.presenter;

import com.tosslab.jandi.app.ui.profile.modify.dagger.ModifyProfileModule;

import dagger.Component;

@Component(modules = ModifyProfileModule.class)
public interface ModifyProfileTestComponent {
    void inject(ModifyProfilePresenterImplTest test);
}
