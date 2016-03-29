package com.tosslab.jandi.app.network.client.main;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResConfig;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;

public class ConfigApi extends ApiTemplate<ConfigApi.Api> {

    public ConfigApi() {
        super(ConfigApi.Api.class);
    }

    public ResConfig getConfig() throws RetrofitException {
        return call(() -> getApi().getConfig());
    }

    interface Api {
        // 클라이언트 Policy(+version) 정보
        @GET("/config")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<ResConfig> getConfig();
    }
}
