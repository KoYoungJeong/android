package com.tosslab.jandi.app;

import android.content.Context;

import com.facebook.stetho.Stetho;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

public class StethoInitializer {
    public static void init(Context context) {
        Stetho.initialize(Stetho.newInitializerBuilder(context)
                .enableDumpapp(Stetho.defaultDumperPluginsProvider(context))
                .enableWebKitInspector(RealmInspectorModulesProvider.builder(context).build())
                .build());
//        Stetho.initializeWithDefaults(context);

    }

}
