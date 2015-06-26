package com.tosslab.jandi.app.network.client.direct.message;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.manager.RestApiClient.annotation.AuthorizedHeader;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessage;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by tee on 15. 6. 16..
 */
@AuthorizedHeader
public interface DirectMessageApiV2Client {
    // Direct Message 리스트 정보 획득
    @GET("/users/{userId}/messages?type=old")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResMessages getDirectMessages(@Query("teamId") int teamId, @Path("userId") int userId,
                                  @Query("fromId") int fromId, @Query("count") int count);

    @GET("/users/{userId}/messages?type=old")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResMessages getDirectMessages(@Query("teamId") int teamId, @Path("userId") int userId);

    @GET("/users/{userId}/messages/update/{timeAfter}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
    ResUpdateMessages getDirectMessagesUpdated(@Query("teamId") int teamId, @Path("userId") int userId,
                                               @Path("timeAfter") int timeAfter);

    // Updated 된 Direct Message 리스트 정보 획득
    @GET("/users/{userId}/messages?type=new")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResMessages getDirectMessagesUpdatedForMarker(@Query("teamId") int teamId, @Path("userId") int userId,
                                                  @Query("currentLinkId") int currentLinkId);

    // Updated 된 Direct Message 리스트 정보 획득
    @GET("/users/{userId}/messages")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResMessages getDirectMarkerMessages(@Query("teamId") int teamId, @Path("userId") int userId,
                                        @Query("currentLinkId") int currentLinkId);

    // Direct Message 생성
    @POST("/users/{userId}/message")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon sendDirectMessage(@Body ReqSendMessage message, @Path("userId") int userId);

    // Direct Message 수정
    @PUT("/users/{userId}/messages/{messageId}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon modifyDirectMessage(ReqModifyMessage message,
                                  @Path("userId") int userId, @Path("messageId") int messageId);

    // Direct Message 삭제
    @DELETE("/users/{userId}/messages/{messageId}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon deleteDirectMessage(@Query("teamId") int teamId, @Path("userId") int userId,
                                  @Path("messageId") int messageId);

}