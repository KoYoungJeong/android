package com.tosslab.jandi.app.network.manager.RestApiClient.RestAdapterFactory;

import com.tosslab.jandi.app.ui.intro.model.JacksonConverter;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.codehaus.jackson.map.ObjectMapper;

import retrofit.RestAdapter;

/**
 * Created by tee on 15. 6. 18..
 */
public class RestAdapterFactory {

    private RestAdapterFactory() {
    }

    public static RestAdapter getSimpleRestAdapter() {
        return new RestAdapter.Builder()
                .setConverter(new JacksonConverter(new ObjectMapper()))
                .setEndpoint("http://i2.jandi.io:8888/inner-api")
                .build();
    }

    public static RestAdapter getAuthRestAdapter() {
        return new RestAdapter.Builder()
                .setRequestInterceptor(request -> {
                    request.addHeader("Authorization", TokenUtil.getRequestAuthentication().getHeaderValue());
                })
                .setConverter(new JacksonConverter(new ObjectMapper()))
                .setEndpoint("http://i2.jandi.io:8888/inner-api")
                .build();
    }

}