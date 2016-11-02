package com.tosslab.jandi.app.network.client.messages;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqMentionMarkerUpdate;
import com.tosslab.jandi.app.network.models.ReqNull;
import com.tosslab.jandi.app.network.models.ReqSetMarker;
import com.tosslab.jandi.app.network.models.ReqShareMessage;
import com.tosslab.jandi.app.network.models.ReqUnshareMessage;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResStarMentioned;
import com.tosslab.jandi.app.network.models.ResStarredMessage;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class MessageApi extends ApiTemplate<MessageApi.Api> {

    public MessageApi(RetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public ResCommon setMarker(long entityId, ReqSetMarker reqSetMarker) throws RetrofitException {
        return call(() -> getApi().setMarker(entityId, reqSetMarker));
    }

    // Message Detail
    public ResFileDetail getFileDetail(long teamId, long messageId) throws RetrofitException {
        return call(() -> getApi().getFileDetail(messageId, teamId));
    }

    // Share Message
    public ResCommon shareMessage(ReqShareMessage share, long messageId) throws RetrofitException {
        return call(() -> getApi().shareMessage(messageId, share));
    }

    // Unshare Message
    public ResCommon unshareMessage(ReqUnshareMessage share, long messageId) throws RetrofitException {
        return call(() -> getApi().unshareMessage(messageId, share));
    }

    @Deprecated
    public List<ResMessages.Link> getRoomUpdateMessage(long teamId,
                                                long roomId,
                                                long currentLinkId) throws RetrofitException {
        return call(() -> getApi().getRoomUpdateMessage(teamId, roomId, currentLinkId));
    }

    public ResMessages.OriginalMessage getMessage(long teamId, long messageId) throws RetrofitException {
        return call(() -> getApi().getMessage(teamId, messageId));
    }


    public ResStarMentioned getStarredMessages(long teamId, long starredId,
                                        int count, String type) throws RetrofitException {
        return call(() -> getApi().getStarredMessages(teamId, starredId, count, type));
    }

    public ResStarredMessage registStarredMessage(long teamId, long messageId, ReqNull reqNull) throws RetrofitException {
        return call(() -> getApi().registStarredMessage(teamId, messageId, reqNull));
    }

    public ResCommon unregistStarredMessage(long teamId, long messageId) throws RetrofitException {
        return call(() -> getApi().unregistStarredMessage(teamId, messageId));
    }

    public ResStarMentioned getMentionedMessages(long teamId,
                                          long messageId, int count) throws RetrofitException {
        return call(() -> getApi().getMentionedMessages(teamId, messageId, count));
    }

    public ResCommon updateMentionMarker(long teamId, ReqMentionMarkerUpdate req) throws RetrofitException {
        return call(() -> getApi().updateMentionMarker(teamId, req));
    }

    interface Api {
        @POST("entities/{entityId}/marker")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> setMarker(@Path("entityId") long entityId, @Body ReqSetMarker reqSetMarker);

        // Message Detail
        @GET("messages/{messageId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResFileDetail> getFileDetail(@Path("messageId") long messageId, @Query("teamId") long teamId);

        // Share Message
        @PUT("messages/{messageId}/share")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> shareMessage(@Path("messageId") long messageId, @Body ReqShareMessage share);

        // Unshare Message
        @PUT("messages/{messageId}/unshare")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> unshareMessage(@Path("messageId") long messageId, @Body ReqUnshareMessage share);

        @Deprecated
        @GET("teams/{teamId}/rooms/{roomId}/messages/updatedList")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<List<ResMessages.Link>> getRoomUpdateMessage(@Path("teamId") long teamId,
                                                          @Path("roomId") long roomId,
                                                          @Query("linkId") long currentLinkId);

        @GET("teams/{teamId}/messages/{messageId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResMessages.OriginalMessage> getMessage(@Path("teamId") long teamId, @Path("messageId") long messageId);


        @GET("teams/{teamId}/messages/starred")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResStarMentioned> getStarredMessages(@Path("teamId") long teamId, @Query("starredId") long starredId,
                                                  @Query("count") int count, @Query("type") String type);

        @POST("teams/{teamId}/messages/{messageId}/starred")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResStarredMessage> registStarredMessage(@Path("teamId") long teamId, @Path("messageId") long messageId, @Body ReqNull reqNull);

        @DELETE("teams/{teamId}/messages/{messageId}/starred")
        Call<ResCommon> unregistStarredMessage(@Path("teamId") long teamId, @Path("messageId") long messageId);

        @GET("teams/{teamId}/messages/mentioned")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<ResStarMentioned> getMentionedMessages(@Path("teamId") long teamId,
                                                    @Query("messageId") long messageId, @Query("count") int count);

        @PUT("teams/{teamId}/messages/mentioned/marker")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> updateMentionMarker(@Path("teamId") long teamId, @Body ReqMentionMarkerUpdate req);
    }
}
