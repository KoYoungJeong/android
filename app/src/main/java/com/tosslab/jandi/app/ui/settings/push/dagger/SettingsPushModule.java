package com.tosslab.jandi.app.ui.settings.push.dagger;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.network.dagger.ApiClientModule;

import dagger.Module;
import dagger.Provides;

@Module(includes = ApiClientModule.class)
public class SettingsPushModule {

    @Provides
    SharedPreferences provideSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(JandiApplication.getContext());
    }
}
