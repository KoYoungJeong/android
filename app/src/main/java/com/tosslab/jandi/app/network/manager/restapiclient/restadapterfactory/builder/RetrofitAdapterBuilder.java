package com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder;

import com.tosslab.jandi.app.BuildConfig;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.decor.ResponseConverter;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.decor.RestAdapterDecor;
import com.tosslab.jandi.app.utils.TokenUtil;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

/**
 * Created by Steve SeongUg Jung on 15. 6. 22..
 */
public class RetrofitAdapterBuilder<CLIENT> {

    private final Class<CLIENT> clazz;
    private List<RestAdapterDecor> restAdapterDecors;

    private RetrofitAdapterBuilder(Class<CLIENT> clazz) {
        this.clazz = clazz;

        restAdapterDecors = new ArrayList<>();
        restAdapterDecors.add(new ResponseConverter());

    }

    public static <CLIENT> RetrofitAdapterBuilder<CLIENT> newInstance(Class<CLIENT> clazz) {
        return new RetrofitAdapterBuilder<>(clazz);
    }

    protected Retrofit getRestAdapter() {

        Retrofit.Builder retofitBuilder = new Retrofit.Builder()
                .baseUrl(JandiConstantsForFlavors.SERVICE_INNER_API_URL);


        OkHttpClient.Builder okhttpClientBuilder = new OkHttpClient.Builder()
                .authenticator((route, response) ->
                        response.request().newBuilder()
                                .header("Authorization", TokenUtil.getRequestAuthentication())
                                .build());

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            okhttpClientBuilder.addInterceptor(logging);

        }

        retofitBuilder.client(okhttpClientBuilder.build());
        for (RestAdapterDecor restAdapterDecor : restAdapterDecors) {
            retofitBuilder = restAdapterDecor.addRestAdapterProperty(retofitBuilder);
        }

        return retofitBuilder.build();
    }

    public CLIENT create() {
        return getRestAdapter().create(clazz);
    }


}
