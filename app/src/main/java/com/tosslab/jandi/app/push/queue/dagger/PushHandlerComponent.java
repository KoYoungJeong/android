package com.tosslab.jandi.app.push.queue.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.push.queue.PushHandler;

import dagger.Component;

@Component(modules = {ApiClientModule.class, PushHandlerModule.class})
public interface PushHandlerComponent {
    void inject(PushHandler pushHandler);
}
