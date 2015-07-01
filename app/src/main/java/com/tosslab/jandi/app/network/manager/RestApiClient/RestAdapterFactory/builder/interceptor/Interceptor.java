package com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.interceptor;

import retrofit.RequestInterceptor;

/**
 * Created by Steve SeongUg Jung on 15. 6. 22..
 */
public interface Interceptor {

    void intercept(RequestInterceptor.RequestFacade request);
}
