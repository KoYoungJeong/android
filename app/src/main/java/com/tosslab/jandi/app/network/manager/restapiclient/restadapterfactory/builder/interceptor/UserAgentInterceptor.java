package com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.interceptor;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.utils.UserAgentUtil;

import retrofit.RequestInterceptor;

/**
 * Created by Steve SeongUg Jung on 15. 7. 13..
 */
public class UserAgentInterceptor implements Interceptor {

    @Override
    public void intercept(RequestInterceptor.RequestFacade request) {
        String userAgent = UserAgentUtil.getDefaultUserAgent(JandiApplication.getContext());
        request.addHeader("User-Agent", userAgent);
    }
}
