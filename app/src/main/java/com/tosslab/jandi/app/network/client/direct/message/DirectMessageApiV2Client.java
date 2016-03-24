package com.tosslab.jandi.app.network.client.direct.message;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.AuthorizedHeader;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.DELETEWithBody;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessageV3;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by tee on 15. 6. 16..
 */
@AuthorizedHeader
public interface DirectMessageApiV2Client {
    // Direct Message 리스트 정보 획득
    @GET("/users/{userId}/messages?type=old")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResMessages getDirectMessages(@Query("teamId") long teamId, @Path("userId") long userId,
                                  @Query("linkId") long fromId, @Query("count") int count);

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
    ResMessages getDirectMessagesUpdatedForMarker(@Query("teamId") long teamId, @Path("userId") long userId,
                                                  @Query("linkId") long currentLinkId);

    @GET("/users/{userId}/messages?type=new")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResMessages getDirectMessagesUpdatedForMarker(@Query("teamId") long teamId, @Path("userId") long userId,
                                                  @Query("linkId") long currentLinkId, @Query("count") int count);

    // Updated 된 Direct Message 리스트 정보 획득
    @GET("/users/{userId}/messages")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResMessages getDirectMarkerMessages(@Query("teamId") long teamId, @Path("userId") long userId,
                                        @Query("linkId") long currentLinkId);

    // Direct Message 생성
    @POST("/users/{userId}/message")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
    ResCommon sendDirectMessage(@Path("userId") long userId, @Query("teamId") long teamId,
                                @Body ReqSendMessageV3 reqSendMessageV3);

    // Direct Message 수정
    @PUT("/users/{userId}/messages/{messageId}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon modifyDirectMessage(ReqModifyMessage message,
                                  @Path("userId") int userId, @Path("messageId") int messageId);

    // Direct Message 삭제
    @DELETEWithBody("/users/{userId}/messages/{messageId}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon deleteDirectMessage(@Query("teamId") long teamId, @Path("userId") long userId,
                                  @Path("messageId") long messageId);

}