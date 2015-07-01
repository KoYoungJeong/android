package com.tosslab.jandi.app.network.manager.RestApiClient.RestAdapterFactory.builder.decor;


import com.tosslab.jandi.app.network.manager.RestApiClient.RestAdapterFactory.builder.interceptor.AuthorizeInterceptor;
import com.tosslab.jandi.app.network.manager.RestApiClient.RestAdapterFactory.builder.interceptor.Interceptor;
import com.tosslab.jandi.app.network.manager.RestApiClient.annotation.AuthorizedHeader;

import java.util.ArrayList;
import java.util.List;

import retrofit.RestAdapter;

/**
 * Created by Steve SeongUg Jung on 15. 6. 23..
 */
public class RequestInterceptor implements RestAdapterDecor {

    private List<Interceptor> interceptors;

    public <T> RequestInterceptor(Class<T> clazz) {

        interceptors = new ArrayList<Interceptor>();

        AuthorizedHeader authorizedHeader = clazz.getAnnotation(AuthorizedHeader.class);
        if (authorizedHeader != null && authorizedHeader.required()) {
            interceptors.add(new AuthorizeInterceptor());
        }
    }

    @Override
    public RestAdapter.Builder addRestAdapterProperty(RestAdapter.Builder builder) {
        return builder.setRequestInterceptor(request -> {
            for (Interceptor interceptor : interceptors) {
                interceptor.intercept(request);
            }
        });
    }
}
