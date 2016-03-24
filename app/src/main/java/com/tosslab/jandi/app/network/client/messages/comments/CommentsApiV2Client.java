package com.tosslab.jandi.app.network.client.messages.comments;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.AuthorizedHeader;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.DELETEWithBody;
import com.tosslab.jandi.app.network.models.ReqModifyComment;
import com.tosslab.jandi.app.network.models.ReqSendComment;
import com.tosslab.jandi.app.network.models.ResCommon;

import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by tee on 15. 6. 16..
 */
@AuthorizedHeader
public interface CommentsApiV2Client {

    // Send Comment
    @POST("/messages/{messageId}/comment")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
    ResCommon sendMessageComment(@Path("messageId") long messageId, @Query("teamId") long teamId, @Body ReqSendComment reqSendComment);

    // Modify comment
    @PUT("/messages/{messageId}/comments/{commentId}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon modifyMessageComment(@Body ReqModifyComment comment, @Path("messageId") int messageId, @Path("commentId") int commentId);

    // Delete comment
    @DELETEWithBody("/messages/{messageId}/comments/{commentId}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon deleteMessageComment(@Query("teamId") long teamId, @Path("messageId") long messageId, @Path("commentId") long commentId);

}
