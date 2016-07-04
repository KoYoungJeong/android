package com.tosslab.jandi.app.services.socket.dagger;

import android.content.Context;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;

import dagger.Module;
import dagger.Provides;

@Module(includes = ApiClientModule.class)
public class SocketServiceModule {
    private Context context;

    public SocketServiceModule(Context context) {this.context = context;}

    @Provides
    Context provideContext() {
        return context;
    }

}
