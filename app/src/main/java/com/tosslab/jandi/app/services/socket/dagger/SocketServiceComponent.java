package com.tosslab.jandi.app.services.socket.dagger;

import com.tosslab.jandi.app.services.socket.JandiSocketService;

import dagger.Component;

@Component(modules = {SocketServiceModule.class} )
public interface SocketServiceComponent {
    void inject(JandiSocketService service);
}
