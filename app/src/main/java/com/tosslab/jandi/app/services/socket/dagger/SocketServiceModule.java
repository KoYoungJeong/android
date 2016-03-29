package com.tosslab.jandi.app.services.socket.dagger;

import android.content.Context;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.services.socket.JandiSocketServiceModel;

import dagger.Module;
import dagger.Provides;

@Module(includes = ApiClientModule.class)
public class SocketServiceModule {
    private Context context;

    public SocketServiceModule(Context context) {this.context = context;}

    @Provides
    JandiSocketServiceModel provideJandiSocketServiceModel(Context context) {
        return new JandiSocketServiceModel(context);
    }

    @Provides
    Context provideContext() {
        return context;
    }

}
