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
public class InnerApiRetrofitBuilder implements RetrofitBuilder {

    private static InnerApiRetrofitBuilder retrofitBuilder;
    private static InnerApiRetrofitBuilder fileUploadRetrofitBuilder;
    private final String url;

    private Retrofit retrofit;

    private InnerApiRetrofitBuilder(String url) {
        this.url = url;
    }

    synchronized public static InnerApiRetrofitBuilder getInstance() {
        if (retrofitBuilder == null) {
            retrofitBuilder = new InnerApiRetrofitBuilder(JandiConstantsForFlavors.getServiceInnerApiUrl());
        }
        return retrofitBuilder;
    }

    synchronized public static InnerApiRetrofitBuilder getInstanceForNotInnerApi() {
        if (retrofitBuilder == null) {
            retrofitBuilder = new InnerApiRetrofitBuilder(JandiConstantsForFlavors.getServiceRootUrl());
        }
        return retrofitBuilder;
    }

    synchronized public static InnerApiRetrofitBuilder getInstanceOfFileUpload() {
        if (fileUploadRetrofitBuilder == null) {
            fileUploadRetrofitBuilder = new InnerApiRetrofitBuilder(JandiConstantsForFlavors.getServiceFileUploadUrl());
        }
        return fileUploadRetrofitBuilder;
    }

    synchronized public static void reset() {
        retrofitBuilder = new InnerApiRetrofitBuilder(JandiConstantsForFlavors.getServiceInnerApiUrl());
        fileUploadRetrofitBuilder = new InnerApiRetrofitBuilder(JandiConstantsForFlavors.getServiceFileUploadUrl());
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

    synchronized public <CLIENT> CLIENT create(Class<CLIENT> clazz) {
        return getRestAdapter().create(clazz);
    }
}
