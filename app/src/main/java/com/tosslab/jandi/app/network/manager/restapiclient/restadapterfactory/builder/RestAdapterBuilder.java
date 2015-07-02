package com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder;

import com.tosslab.jandi.app.BuildConfig;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.decor.RequestInterceptor;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.decor.ResponseConverter;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.decor.RestAdapterDecor;

import java.util.ArrayList;
import java.util.List;

import retrofit.RestAdapter;

/**
 * Created by Steve SeongUg Jung on 15. 6. 22..
 */
public class RestAdapterBuilder<CLIENT> {

    private final Class<CLIENT> clazz;
    private List<RestAdapterDecor> restAdapterDecors;

    private RestAdapterBuilder(Class<CLIENT> clazz) {
        this.clazz = clazz;

        restAdapterDecors = new ArrayList<RestAdapterDecor>();
        restAdapterDecors.add(new RequestInterceptor(clazz));
        restAdapterDecors.add(new ResponseConverter(clazz));

    }

    public static <CLIENT> RestAdapterBuilder<CLIENT> newInstance(Class<CLIENT> clazz) {
        return new RestAdapterBuilder<CLIENT>(clazz);
    }

    protected RestAdapter.Builder getRestAdapter() {

        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(JandiConstantsForFlavors.SERVICE_INNER_API_URL);

        if (BuildConfig.DEBUG) {
            builder.setLogLevel(RestAdapter.LogLevel.BASIC);
        }

        for (RestAdapterDecor restAdapterDecor : restAdapterDecors) {
            builder = restAdapterDecor.addRestAdapterProperty(builder);
        }

        return builder;
    }

    public CLIENT create() {
        return getRestAdapter().build().create(clazz);
    }


}
