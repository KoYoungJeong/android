package com.tosslab.jandi.app.network.client.main;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitAdapterBuilder;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public class LeftSideApi extends ApiTemplate<LeftSideApi.Api> {

    public LeftSideApi(RetrofitAdapterBuilder retrofitAdapterBuilder) {
        super(Api.class, retrofitAdapterBuilder);
    }

    public ResLeftSideMenu getInfosForSideMenu(long teamId) throws RetrofitException {
        return call(() -> getApi().getInfosForSideMenu(teamId));
    }

    interface Api {
        // 채널, PG, DM 리스트 획득
        @GET("leftSideMenu")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResLeftSideMenu> getInfosForSideMenu(@Query("teamId") long teamId);


    }
}
