package com.tosslab.jandi.app.network.client.messages.comments;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.models.ReqSendComment;
import com.tosslab.jandi.app.network.models.ResCommon;

import org.androidannotations.annotations.rest.Delete;
import org.androidannotations.annotations.rest.Post;
import org.androidannotations.annotations.rest.Put;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by tee on 15. 6. 16..
 */
public interface CommentsApiV2Client {

    // Send Comment
    @POST("/messages/{messageId}/comment")
    @Headers("Accept :"+ JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon sendMessageComment(@Body ReqSendComment comment, @Path("messageId") int messageId);

    // Modify comment
    @PUT("/messages/{messageId}/comments/{commentId}")
    @Headers("Accept :"+ JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon modifyMessageComment(@Body ReqSendComment comment, @Path("messageId") int messageId,@Path("commentId") int commentId);

    // Delete comment
    @DELETE("/messages/{messageId}/comments/{commentId}")
    @Headers("Accept :"+ JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon deleteMessageComment(@Query("teamId") int teamId, @Path("messageId") int messageId,@Path("commentId") int commentId);

}
