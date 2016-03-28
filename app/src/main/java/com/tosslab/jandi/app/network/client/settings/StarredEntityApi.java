package com.tosslab.jandi.app.network.client.settings;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqTeam;
import com.tosslab.jandi.app.network.models.ResCommon;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HTTP;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class StarredEntityApi extends ApiTemplate<StarredEntityApi.Api> {
    public StarredEntityApi() {
        super(Api.class);
    }

    ResCommon enableFavorite(ReqTeam reqTeam, long entityId) throws RetrofitException {
        return call(() -> getApi().enableFavorite(reqTeam, entityId));
    }

    ResCommon disableFavorite(long teamId, long entityId) throws RetrofitException {
        return call(() -> getApi().disableFavorite(teamId, entityId));
    }

    interface Api {

        @POST("/settings/starred/entities/{entityId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> enableFavorite(@Body ReqTeam reqTeam, @Path("entityId") long entityId);

        @HTTP(path = "/settings/starred/entities/{entityId}", hasBody = true, method = "DELETE")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> disableFavorite(@Query("teamId") long teamId, @Path("entityId") long entityId);

    }
}
