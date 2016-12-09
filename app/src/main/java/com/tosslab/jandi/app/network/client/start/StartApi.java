package com.tosslab.jandi.app.network.client.start;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.start.InitialInfo;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public class StartApi extends ApiTemplate<StartApi.Api> {
    @Inject
    public StartApi(RetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public InitialInfo getInitializeInfo(long teamId) throws RetrofitException {
        return call(() -> {
            return getApi().getInitializeInfo(teamId);
        });
    }

    interface Api {

        @GET("/start-api/teams/{teamId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<InitialInfo> getInitializeInfo(@Path("teamId") long teamId);

    }
}
