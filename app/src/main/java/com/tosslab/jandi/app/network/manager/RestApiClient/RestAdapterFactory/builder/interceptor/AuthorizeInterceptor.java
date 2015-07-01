package com.tosslab.jandi.app.network.manager.RestApiClient.RestAdapterFactory.builder.interceptor;

import com.tosslab.jandi.app.utils.TokenUtil;

import retrofit.RequestInterceptor;

/**
 * Created by Steve SeongUg Jung on 15. 6. 22..
 */
public class AuthorizeInterceptor implements Interceptor {
    @Override
    public void intercept(RequestInterceptor.RequestFacade request) {
        request.addHeader("Authorization", TokenUtil.getRequestAuthentication().getHeaderValue());
    }
}
