package com.tosslab.jandi.app.network.client.direct.message;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class DirectMessageApi extends ApiTemplate<DirectMessageApi.Api> {
    public DirectMessageApi(RetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public ResMessages getDirectMessages(long teamId, long userId,
                                         long fromId, int count) throws RetrofitException {
        return call(() -> getApi().getDirectMessages(userId, teamId, fromId, count));
    }

    @Deprecated
    public ResMessages getDirectMessages(long teamId, long userId) throws RetrofitException {
        return call(() -> getApi().getDirectMessages(userId, teamId));
    }

    // Updated 된 Direct Message 리스트 정보 획득
    public ResMessages getDirectMessagesUpdatedForMarker(long teamId, long userId,
                                                         long currentLinkId) throws RetrofitException {
        return call(() -> getApi().getDirectMessagesUpdatedForMarker(userId, teamId, currentLinkId));
    }

    public ResMessages getDirectMessagesUpdatedForMarker(long teamId, long userId,
                                                         long currentLinkId, int count) throws RetrofitException {
        return call(() -> getApi().getDirectMessagesUpdatedForMarker(userId, teamId, currentLinkId, count));
    }

    // Updated 된 Direct Message 리스트 정보 획득
    public ResMessages getDirectMarkerMessages(long teamId, long userId,
                                               long currentLinkId, int count) throws RetrofitException {
        return call(() -> getApi().getDirectMarkerMessages(userId, teamId, currentLinkId, count));
    }

    // Direct Message 수정
    @Deprecated
    public ResCommon modifyDirectMessage(ReqModifyMessage message,
                                         long userId, long messageId) throws RetrofitException {
        return call(() -> getApi().modifyDirectMessage(userId, messageId, message));
    }

    // Direct Message 삭제
    public ResCommon deleteDirectMessage(long teamId, long userId,
                                         long messageId) throws RetrofitException {
        return call(() -> getApi().deleteDirectMessage(userId, messageId, teamId));
    }

    interface Api {
        // Direct Message 리스트 정보 획득
        @GET("users/{userId}/messages?type=old")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResMessages> getDirectMessages(@Path("userId") long userId, @Query("teamId") long teamId,
                                            @Query("linkId") long fromId, @Query("count") int count);

        @GET("users/{userId}/messages?type=old")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResMessages> getDirectMessages(@Path("userId") long userId, @Query("teamId") long teamId);

        // Updated 된 Direct Message 리스트 정보 획득
        @GET("users/{userId}/messages?type=new")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResMessages> getDirectMessagesUpdatedForMarker(@Path("userId") long userId, @Query("teamId") long teamId,
                                                            @Query("linkId") long currentLinkId);

        @GET("users/{userId}/messages?type=new")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResMessages> getDirectMessagesUpdatedForMarker(@Path("userId") long userId, @Query("teamId") long teamId,
                                                            @Query("linkId") long currentLinkId, @Query("count") int count);

        // Updated 된 Direct Message 리스트 정보 획득
        @GET("users/{userId}/messages")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResMessages> getDirectMarkerMessages(@Path("userId") long userId, @Query("teamId") long teamId,
                                                  @Query("linkId") long currentLinkId, @Query("count") int count);

        // Direct Message 수정
        @PUT("users/{userId}/messages/{messageId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> modifyDirectMessage(@Path("userId") long userId, @Path("messageId") long messageId, @Body ReqModifyMessage message);

        // Direct Message 삭제
        @HTTP(path = "users/{userId}/messages/{messageId}", hasBody = true, method = "DELETE")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> deleteDirectMessage(@Path("userId") long userId, @Path("messageId") long messageId, @Query("teamId") long teamId);

    }
}
