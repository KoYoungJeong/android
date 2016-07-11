package com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.okhttp;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class LoggingAppender {

    public static OkHttpClient.Builder add(OkHttpClient.Builder builder) {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(logging);

        return builder;
    }
}
