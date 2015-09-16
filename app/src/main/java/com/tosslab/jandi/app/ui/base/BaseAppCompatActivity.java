package com.tosslab.jandi.app.ui.base;

import android.support.v7.app.AppCompatActivity;

import com.facebook.appevents.AppEventsLogger;

public class BaseAppCompatActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        AppEventsLogger.deactivateApp(this);
        super.onPause();
    }
}
