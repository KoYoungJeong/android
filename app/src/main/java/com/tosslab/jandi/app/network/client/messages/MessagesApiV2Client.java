package com.tosslab.jandi.app.network.client.messages;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.AuthorizedHeader;
import com.tosslab.jandi.app.network.models.ReqShareMessage;
import com.tosslab.jandi.app.network.models.ReqUnshareMessage;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.util.List;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by tee on 15. 6. 16..
 */
@AuthorizedHeader
public interface MessagesApiV2Client {

    // Message Detail
    @GET("/messages/{messageId}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResFileDetail getFileDetail(@Query("teamId") long teamId, @Path("messageId") long messageId);

    // Share Message
    @PUT("/messages/{messageId}/share")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon shareMessage(@Body ReqShareMessage share, @Path("messageId") long messageId);

    // Unshare Message
    @PUT("/messages/{messageId}/unshare")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon unshareMessage(@Body ReqUnshareMessage share, @Path("messageId") long messageId);

    @GET("/teams/{teamId}/rooms/{roomId}/messages/updatedList")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
    List<ResMessages.Link> getRoomUpdateMessage(@Path("teamId") long teamId,
                                                @Path("roomId") long roomId,
                                                @Query("linkId") long currentLinkId);

}