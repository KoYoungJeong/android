package com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.okhttp;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import okhttp3.OkHttpClient;

public class StethoAppender {

    public static OkHttpClient.Builder add(OkHttpClient.Builder builder) {
        return builder.addNetworkInterceptor(new StethoInterceptor());
    }
}
