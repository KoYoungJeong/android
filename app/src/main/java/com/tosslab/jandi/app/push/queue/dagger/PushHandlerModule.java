package com.tosslab.jandi.app.push.queue.dagger;

import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.network.client.main.LeftSideApi;
import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.push.receiver.JandiPushReceiverModel;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;

@Module(includes = ApiClientModule.class)
public class PushHandlerModule {

    @Provides
    AudioManager provideAudioManager() {
        return (AudioManager) JandiApplication.getContext().getSystemService(Context.AUDIO_SERVICE);
    }

    @Provides
    NotificationManager provideNotificationManager() {
        return (NotificationManager) JandiApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Provides
    JandiPushReceiverModel provideJandiPushReceiverModel(AudioManager am, Lazy<LeftSideApi> api, NotificationManager nm) {
        return new JandiPushReceiverModel(am, api, nm);
    }

}
