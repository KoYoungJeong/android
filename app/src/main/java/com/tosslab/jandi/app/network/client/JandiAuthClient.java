package com.tosslab.jandi.app.network.client;

import android.content.Context;

import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.rest.RestService;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * Created by justinygchoi on 2014. 7. 16..
 */
@EBean
public class JandiAuthClient {

    @RestService
    JandiRestClient jandiRestClient;

    @RootContext
    Context context;

    @AfterInject
    void initAuthentication() {
        jandiRestClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
    }


    public ResConfig getConfig() throws JandiNetworkException {
        try {
            return jandiRestClient.getConfig();
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

}
