package com.tosslab.jandi.app.push.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.push.PushInterfaceActivity;

import dagger.Component;

@Component(modules = ApiClientModule.class)
public interface PushInterfaceComponent {
    void inject(PushInterfaceActivity activity);
}
