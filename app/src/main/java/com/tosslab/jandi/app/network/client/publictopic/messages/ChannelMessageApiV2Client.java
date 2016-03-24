package com.tosslab.jandi.app.network.client.publictopic.messages;

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
public interface ChannelMessageApiV2Client {

    // 채널에서 Message 리스트 정보 획득
    @GET("/channels/{channelId}/messages?type=old")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResMessages getPublicTopicMessages(@Query("teamId") long teamId, @Path("channelId") long channelId,
                                       @Query("linkId") long fromId, @Query("count") int count);

    @GET("/channels/{channelId}/messages?type=old")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResMessages getPublicTopicMessages(@Query("teamId") int teamId, @Path("channelId") int channelId);

    // 채널의 업데이트 Message 리스트 정보 획득
    @GET("/channels/{channelId}/messages/update/{linkId}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
    ResUpdateMessages getPublicTopicUpdatedMessages(@Query("teamId") int teamId, @Path("channelId") int channelId,
                                                    @Path("linkId") int currentLinkId);

    @GET("/channels/{channelId}/messages?type=new")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResMessages getPublicTopicUpdatedMessagesForMarker(@Query("teamId") long teamId, @Path("channelId") long channelId,
                                                       @Query("linkId") long currentLinkId);

    @GET("/channels/{channelId}/messages?type=new")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResMessages getPublicTopicUpdatedMessagesForMarker(@Query("teamId") long teamId, @Path("channelId") long channelId,
                                                       @Query("linkId") long currentLinkId, @Query("count") int count);


    @GET("/channels/{channelId}/messages")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResMessages getPublicTopicMarkerMessages(@Query("teamId") long teamId, @Path("channelId") long channelId,
                                             @Query("linkId") long currentLinkId);

    // 채널에서 Message 생성
    @POST("/channels/{channelId}/message")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
    ResCommon sendPublicTopicMessage(@Path("channelId") long channelId, @Query("teamId") long teamId,
                                     @Body ReqSendMessageV3 reqSendMessageV3);

    // 채널에서 Message 수정
    @PUT("/channels/{channelId}/messages/{messageId}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon modifyPublicTopicMessage(@Body ReqModifyMessage message, @Path("channelId") int channelId,
                                       @Path("messageId") int messageId);

    // 채널에서 Message 삭제
    @DELETEWithBody("/channels/{channelId}/messages/{messageId}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon deletePublicTopicMessage(@Query("teamId") long teamId, @Path("channelId") long channelId,
                                       @Path("messageId") long messageId);

}
