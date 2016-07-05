package com.tosslab.jandi.app.ui.socketevent.dagger;

import com.tosslab.jandi.app.services.socket.dagger.SocketServiceModule;
import com.tosslab.jandi.app.ui.socketevent.SocketInitActivity;

import dagger.Component;

@Component(modules = SocketServiceModule.class)
public interface SocketInitComponent {
    void inject(SocketInitActivity activity);
}
