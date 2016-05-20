package com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.decor.JacksonConverter;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * Created by Steve SeongUg Jung on 15. 6. 22..
 */
public class RetrofitBuilder {

    private static RetrofitBuilder retrofitBuilder;

    private Retrofit retrofit;

    private RetrofitBuilder() {}

    synchronized public static RetrofitBuilder getInstance() {
        if (retrofitBuilder == null) {
            retrofitBuilder = new RetrofitBuilder();
        }
        return retrofitBuilder;
    }

    private Retrofit getRestAdapter() {

        if (retrofit == null) {
            retrofit = initRetrofit();
        }

        return retrofit;
    }

    private Retrofit initRetrofit() {

        Retrofit.Builder retofitBuilder = new Retrofit.Builder()
                .baseUrl(JandiConstantsForFlavors.SERVICE_INNER_API_URL)
                .addConverterFactory(JacksonConverter.create());

        OkHttpClient okHttpClient = JandiApplication.getOkHttpClient();
        retofitBuilder.client(okHttpClient);

        return retofitBuilder.build();
    }

    synchronized public <CLIENT> CLIENT create(Class<CLIENT> clazz) {
        return getRestAdapter().create(clazz);
    }
}
