package com.tosslab.jandi.app.network.client.privatetopic.messages;

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
public interface GroupMessageApiV2Client {

    // Private Group의 Message 리스트 정보 획득
    @GET("/privateGroups/{groupId}/messages")
    @Headers("Accept:"+ JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResMessages getGroupMessages(@Query("teamId") int teamId, @Path("groupId") int groupId,
                                 @Query("fromId") int fromId, @Query("count") int count);

    @GET("/privateGroups/{groupId}/messages?type=old")
    @Headers("Accept:"+ JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResMessages getGroupMessages(@Query("teamId") int teamId, @Path("groupId") int groupId);

    @GET("/privateGroups/{groupId}/messages/update/{lastLinkId}")
    @Headers("Accept:"+ JandiConstants.HTTP_ACCEPT_HEADER_V3)
    ResUpdateMessages getGroupMessagesUpdated(@Query("teamId") int teamId,
                                              @Path("groupId") int groupId,@Path("lastLinkId") int lastLinkId);

    @GET("/privateGroups/{groupId}/messages?type=new")
    @Headers("Accept:"+ JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResMessages getGroupMessagesUpdatedForMarker(@Query("teamId") int teamId, @Path("groupId") int groupId,
                                                 @Query("currentLinkId") int currentLinkId);

    // Updated 된 Private Group의 리스트 정보 획득
    @GET("/privateGroups/{groupId}/messages")
    @Headers("Accept:"+ JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResMessages getGroupMarkerMessages(@Query("teamId") int teamId, @Path("groupId") int groupId,
                                       @Query("currentLinkId") int currentLinkId);

    // Private Group에서의 Message 생성
    @POST("/privateGroups/{groupId}/message")
    @Headers("Accept:"+ JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon sendGroupMessage(@Body ReqSendMessage message, @Path("groupId") int groupId);

    // Private Group Message 수정
    @PUT("/privateGroups/{groupId}/messages/{messageId}")
    @Headers("Accept:"+ JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon modifyPrivateGroupMessage(@Body ReqModifyMessage message,
                                        @Path("groupId") int groupId, @Path("messageId") int messageId);

    // Private Group Message 삭제
    @DELETE("/privateGroups/{groupId}/messages/{messageId}")
    @Headers("Accept:"+ JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon deletePrivateGroupMessage(@Query("teamId") int teamId, @Path("groupId") int groupId,
                                        @Path("messageId") int messageId);

}
