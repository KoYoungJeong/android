package com.tosslab.jandi.app.network.client.settings;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqTeam;
import com.tosslab.jandi.app.network.models.ResCommon;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HTTP;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class StarredEntityApi extends ApiTemplate<StarredEntityApi.Api> {
    @Inject
    public StarredEntityApi(RetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public ResCommon enableFavorite(ReqTeam reqTeam, long entityId) throws RetrofitException {
        return call(() -> getApi().enableFavorite(entityId, reqTeam));
    }

    public ResCommon disableFavorite(long teamId, long entityId) throws RetrofitException {
        return call(() -> getApi().disableFavorite(entityId, teamId));
    }

    interface Api {

        @POST("settings/starred/entities/{entityId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> enableFavorite(@Path("entityId") long entityId, @Body ReqTeam reqTeam);

        @HTTP(path = "settings/starred/entities/{entityId}", hasBody = true, method = "DELETE")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> disableFavorite(@Path("entityId") long entityId, @Query("teamId") long teamId);

    }
}
