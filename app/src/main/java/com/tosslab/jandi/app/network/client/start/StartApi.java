package com.tosslab.jandi.app.network.client.start;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.network.models.ResStartAccountInfo;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public class StartApi extends ApiTemplate<StartApi.Api> {
    @Inject
    public StartApi(InnerApiRetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public String getRawInitializeInfo(long teamId) throws RetrofitException {
        return call(() -> getApi().getRawInitializeInfo(teamId));
    }

    public ResStartAccountInfo getAccountInitializeInfo() throws RetrofitException {
        return call(() -> getApi().getRawAccountInitializeInfo());
    }

    interface Api {

        @GET("/start-api/teams/{teamId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<String> getRawInitializeInfo(@Path("teamId") long teamId);

        @GET("/start-api/account")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResStartAccountInfo> getRawAccountInitializeInfo();

    }
}
