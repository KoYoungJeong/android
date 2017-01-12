package com.tosslab.jandi.app;

import android.content.Context;

import com.facebook.stetho.Stetho;

public class StethoInitializer {
    public static void init(Context context) {
        Stetho.initializeWithDefaults(context);

    }

}
