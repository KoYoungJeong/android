package com.tosslab.jandi.app.network.client.settings;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.manager.RestApiClient.annotation.AuthorizedHeader;
import com.tosslab.jandi.app.network.models.ReqTeam;
import com.tosslab.jandi.app.network.models.ResCommon;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by tee on 15. 6. 16..
 */
@AuthorizedHeader
public interface StarredEntityApiV2Client {

    @POST("/settings/starred/entities/{entityId}")
    @Headers("Accept :" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon enableFavorite(@Body ReqTeam reqTeam, @Path("entityId") int entityId);

    @DELETE("/settings/starred/entities/{entityId}")
    @Headers("Accept :" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon disableFavorite(@Query("teamId") int teamId, @Path("entityId") int entityId);

}
