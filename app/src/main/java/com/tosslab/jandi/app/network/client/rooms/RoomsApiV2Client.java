package com.tosslab.jandi.app.network.client.rooms;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.AuthorizedHeader;
import com.tosslab.jandi.app.network.models.ResRoomInfo;

import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Path;

/**
 * Created by tee on 15. 6. 16..
 */

@AuthorizedHeader
public interface RoomsApiV2Client {

    @GET("/teams/{teamId}/rooms/{roomId}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResRoomInfo getRoomInfo(@Path("teamId") long teamId, @Path("roomId") long roomId);

}
