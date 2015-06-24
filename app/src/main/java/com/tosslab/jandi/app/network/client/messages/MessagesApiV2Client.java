package com.tosslab.jandi.app.network.client.messages;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.manager.RestApiClient.annotation.AuthorizedHeader;
import com.tosslab.jandi.app.network.models.ReqShareMessage;
import com.tosslab.jandi.app.network.models.ReqUnshareMessage;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResFileDetail;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by tee on 15. 6. 16..
 */
@AuthorizedHeader
public interface MessagesApiV2Client {

    // Message Detail
    @GET("/messages/{messageId}")
    @Headers("Accept :"+ JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResFileDetail getFileDetail(@Query("teamId") int teamId, @Path("messageId") int messageId);

    // Share Message
    @PUT("/messages/{messageId}/share")
    @Headers("Accept :"+ JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon shareMessage(@Body ReqShareMessage share, @Path("messageId") int messageId);

    // Unshare Message
    @PUT("/messages/{messageId}/unshare")
    @Headers("Accept :"+ JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon unshareMessage(@Body ReqUnshareMessage share, @Path("messageId") int messageId);

}