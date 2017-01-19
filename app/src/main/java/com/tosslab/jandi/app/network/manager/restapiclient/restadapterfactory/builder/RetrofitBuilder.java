package com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.decor.JacksonConverter;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by Steve SeongUg Jung on 15. 6. 22..
 */
public class RetrofitBuilder {

    private static RetrofitBuilder retrofitBuilder;
    private static RetrofitBuilder fileUploadRetrofitBuilder;
    private final String url;

    private Retrofit retrofit;

    private RetrofitBuilder(String url) {
        this.url = url;
    }

    synchronized public static RetrofitBuilder getInstance() {
        if (retrofitBuilder == null) {
            retrofitBuilder = new RetrofitBuilder(JandiConstantsForFlavors.getServiceInnerApiUrl());
        }
        return retrofitBuilder;
    }

    synchronized public static RetrofitBuilder getInstanceOfFileUpload() {
        if (fileUploadRetrofitBuilder == null) {
            fileUploadRetrofitBuilder = new RetrofitBuilder(JandiConstantsForFlavors.getServiceFileUploadUrl());
        }
        return fileUploadRetrofitBuilder;
    }

    private Retrofit getRestAdapter() {

        if (retrofit == null) {
            retrofit = initRetrofit();
        }

        return retrofit;
    }

    private Retrofit initRetrofit() {

        Retrofit.Builder retofitBuilder = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(JacksonConverter.create());

        OkHttpClient okHttpClient = JandiApplication.getOkHttpClient();
        retofitBuilder.client(okHttpClient);

        return retofitBuilder.build();
    }

    synchronized public static void reset() {
        retrofitBuilder = new RetrofitBuilder(JandiConstantsForFlavors.getServiceInnerApiUrl());
        fileUploadRetrofitBuilder = new RetrofitBuilder(JandiConstantsForFlavors.getServiceFileUploadUrl());
    }

    synchronized public <CLIENT> CLIENT create(Class<CLIENT> clazz) {
        return getRestAdapter().create(clazz);
    }
}
