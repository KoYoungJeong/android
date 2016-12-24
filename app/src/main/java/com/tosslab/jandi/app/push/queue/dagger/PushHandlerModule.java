package com.tosslab.jandi.app.push.queue.dagger;

import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.push.receiver.JandiPushReceiverModel;

import dagger.Module;
import dagger.Provides;

@Module
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
    JandiPushReceiverModel provideJandiPushReceiverModel(AudioManager am, NotificationManager nm) {
        return new JandiPushReceiverModel(am, nm);
    }

}
