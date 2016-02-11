package com.tosslab.jandi.app.network.client.privatetopic.messages;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.AuthorizedHeader;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.DELETEWithBody;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessageV3;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;

import retrofit.http.Body;
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
    @GET("/privateGroups/{groupId}/messages?type=old")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResMessages getGroupMessages(@Query("teamId") long teamId, @Path("groupId") long groupId,
                                 @Query("linkId") long fromId, @Query("count") int count);

    @GET("/privateGroups/{groupId}/messages?type=old")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResMessages getGroupMessages(@Query("teamId") int teamId, @Path("groupId") int groupId);

    @GET("/privateGroups/{groupId}/messages/update/{lastLinkId}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
    ResUpdateMessages getGroupMessagesUpdated(@Query("teamId") int teamId,
                                              @Path("groupId") int groupId, @Path("lastLinkId") int lastLinkId);

    @GET("/privateGroups/{groupId}/messages?type=new")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResMessages getGroupMessagesUpdatedForMarker(@Query("teamId") long teamId, @Path("groupId") long groupId,
                                                 @Query("linkId") long currentLinkId);

    @GET("/privateGroups/{groupId}/messages?type=new")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResMessages getGroupMessagesUpdatedForMarker(@Query("teamId") long teamId, @Path("groupId") long groupId,
                                                 @Query("linkId") long currentLinkId, @Query("count") int count);

    // Updated 된 Private Group의 리스트 정보 획득
    @GET("/privateGroups/{groupId}/messages")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResMessages getGroupMarkerMessages(@Query("teamId") long teamId, @Path("groupId") long groupId,
                                       @Query("linkId") long currentLinkId);

    // Private Group에서의 Message 생성
    @POST("/privateGroups/{privateGroupId}/message")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
    ResCommon sendGroupMessage(@Path("privateGroupId") long privateGroupId, @Query("teamId") long teamId,
                               @Body ReqSendMessageV3 reqSendMessageV3);

    // Private Group Message 수정
    @PUT("/privateGroups/{groupId}/messages/{messageId}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon modifyPrivateGroupMessage(@Body ReqModifyMessage message,
                                        @Path("groupId") int groupId, @Path("messageId") int messageId);

    // Private Group Message 삭제
    @DELETEWithBody("/privateGroups/{groupId}/messages/{messageId}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon deletePrivateGroupMessage(@Query("teamId") long teamId, @Path("groupId") long groupId,
                                        @Path("messageId") long messageId);

}
