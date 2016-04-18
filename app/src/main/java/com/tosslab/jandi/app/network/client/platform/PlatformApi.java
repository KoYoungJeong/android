package com.tosslab.jandi.app.network.client.platform;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqUpdatePlatformStatus;
import com.tosslab.jandi.app.network.models.ResCommon;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.PUT;

public class PlatformApi extends ApiTemplate<PlatformApi.Api> {
    public PlatformApi(RetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public ResCommon updatePlatformStatus(ReqUpdatePlatformStatus reqUpdatePlatformStatus) throws RetrofitException {
        return call(() -> getApi().updatePlatformStatus(reqUpdatePlatformStatus));
    }

    interface Api {

        @PUT("platform/active")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> updatePlatformStatus(@Body ReqUpdatePlatformStatus reqUpdatePlatformStatus);

    }
}
