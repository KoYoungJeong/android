package com.tosslab.jandi.app.network.client.messages.comments;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitAdapterBuilder;
import com.tosslab.jandi.app.network.models.ReqModifyComment;
import com.tosslab.jandi.app.network.models.ReqSendComment;
import com.tosslab.jandi.app.network.models.ResCommon;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HTTP;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class CommentApi extends ApiTemplate<CommentApi.Api> {
    public CommentApi(RetrofitAdapterBuilder retrofitAdapterBuilder) {
        super(Api.class, retrofitAdapterBuilder);
    }

    // Send Comment
    public ResCommon sendMessageComment(long messageId, long teamId, ReqSendComment reqSendComment) throws RetrofitException {
        return call(() -> getApi().sendMessageComment(messageId, teamId, reqSendComment));
    }

    // Modify comment
    public ResCommon modifyMessageComment(ReqModifyComment comment, int messageId, int commentId) throws RetrofitException {
        return call(() -> getApi().modifyMessageComment(messageId, commentId, comment));
    }

    // Delete comment
    public ResCommon deleteMessageComment(long teamId, long messageId, long commentId) throws RetrofitException {
        return call(() -> getApi().deleteMessageComment(messageId, commentId, teamId));
    }

    interface Api {

        // Send Comment
        @POST("messages/{messageId}/comment")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<ResCommon> sendMessageComment(@Path("messageId") long messageId, @Query("teamId") long teamId, @Body ReqSendComment reqSendComment);

        // Modify comment
        @PUT("messages/{messageId}/comments/{commentId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> modifyMessageComment(@Path("messageId") int messageId, @Path("commentId") int commentId, @Body ReqModifyComment comment);

        // Delete comment
        @HTTP(path = "messages/{messageId}/comments/{commentId}", hasBody = true, method = "DELETE")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> deleteMessageComment(@Path("messageId") long messageId, @Path("commentId") long commentId, @Query("teamId") long teamId);

    }
}
