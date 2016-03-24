package com.tosslab.jandi.app.network.client.rooms;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.AuthorizedHeader;
import com.tosslab.jandi.app.network.models.ResRoomInfo;

import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

/**
 * Created by tee on 15. 6. 16..
 */

@AuthorizedHeader
public interface RoomsApiV2Client {

    @GET("/teams/{teamId}/rooms/{roomId}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResRoomInfo getRoomInfo(@Path("teamId") long teamId, @Path("roomId") long roomId);

}
