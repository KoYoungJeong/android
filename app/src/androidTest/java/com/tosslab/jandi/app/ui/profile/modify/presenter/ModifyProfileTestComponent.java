package com.tosslab.jandi.app.ui.profile.modify.presenter;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.profile.modify.dagger.ModifyProfileModule;

import dagger.Component;

@Component(modules = {ApiClientModule.class, ModifyProfileModule.class})
public interface ModifyProfileTestComponent {
    void inject(ModifyProfilePresenterImplTest test);
}
