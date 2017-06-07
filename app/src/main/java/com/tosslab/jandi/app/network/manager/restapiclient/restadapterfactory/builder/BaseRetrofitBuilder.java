package com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.decor.JacksonConverter;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by tee on 2017. 5. 30..
 */

public class BaseRetrofitBuilder implements RetrofitBuilder {

    private static BaseRetrofitBuilder retrofitBuilder;
    private final String url;

    private Retrofit retrofit;

    private BaseRetrofitBuilder(String url) {
        this.url = url;
    }

    synchronized public static BaseRetrofitBuilder getInstance() {
        if (retrofitBuilder == null) {
            retrofitBuilder = new BaseRetrofitBuilder(JandiConstantsForFlavors.getServiceRootUrl());
        }
        return retrofitBuilder;
    }

    synchronized public static void reset() {
        retrofitBuilder = new BaseRetrofitBuilder(JandiConstantsForFlavors.getServiceRootUrl());
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
