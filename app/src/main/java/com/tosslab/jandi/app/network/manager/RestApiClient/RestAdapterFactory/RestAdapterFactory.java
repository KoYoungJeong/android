package com.tosslab.jandi.app.network.manager.RestApiClient.RestAdapterFactory;

import com.tosslab.jandi.app.JandiConstants;
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

    public static RestAdapter.Builder getDefaultBuilder() {
        return new RestAdapter.Builder()
                .setEndpoint(JandiConstants.API_URL);
    }

    public static RestAdapter getSimpleRestAdapter() {
        return getDefaultBuilder()
                .setConverter(new JacksonConverter(new ObjectMapper()))
                .build();
    }

    public static RestAdapter getAuthRestAdapter() {
        return getDefaultBuilder()
                .setRequestInterceptor(request -> {
                    request.addHeader("Authorization", TokenUtil.getRequestAuthentication().getHeaderValue());
                })
                .setConverter(new JacksonConverter(new ObjectMapper()))
                .build();
    }

}