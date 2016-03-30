package com.tosslab.jandi.app.services.socket.dagger;

import android.content.Context;

import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.events.EventsApi;
import com.tosslab.jandi.app.network.client.main.LoginApi;
import com.tosslab.jandi.app.network.client.messages.MessageApi;
import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.services.socket.JandiSocketServiceModel;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;

@Module(includes = ApiClientModule.class)
public class SocketServiceModule {
    private Context context;

    public SocketServiceModule(Context context) {this.context = context;}

    @Provides
    JandiSocketServiceModel provideJandiSocketServiceModel(Context context,
                                                           Lazy<AccountApi> accountApi,
                                                           Lazy<MessageApi> messageApi,
                                                           Lazy<LoginApi> loginApi,
                                                           Lazy<EventsApi> eventsApi) {
        return new JandiSocketServiceModel(context, accountApi, messageApi, loginApi, eventsApi);
    }

    @Provides
    Context provideContext() {
        return context;
    }

}
