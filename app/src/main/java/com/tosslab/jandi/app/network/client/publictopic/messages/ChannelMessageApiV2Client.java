package com.tosslab.jandi.app.network.client.publictopic.messages;

import com.tosslab.jandi.app.network.manager.RestApiClient.annotation.AuthorizedHeader;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessage;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;
import com.tosslab.jandi.app.network.spring.JandiV3HttpMessageConverter;

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
public interface ChannelMessageApiV2Client {

    // 채널에서 Message 리스트 정보 획득
    @GET("/channels/{channelId}/messages")
    ResMessages getPublicTopicMessages(@Query("teamId") int teamId,@Path("channelId") int channelId,
                                       @Query("fromId") int fromId, @Query("count") int count);

    @GET("/channels/{channelId}/messages?type=old")
    ResMessages getPublicTopicMessages(@Query("teamId") int teamId,@Path("channelId") int channelId);

    // 채널의 업데이트 Message 리스트 정보 획득
    @GET("/channels/{channelId}/messages/update/{currentLinkId}")
    @Headers("Accept :" + JandiV3HttpMessageConverter.APPLICATION_VERSION_FULL_NAME)
    ResUpdateMessages getPublicTopicUpdatedMessages(@Query("teamId") int teamId, @Path("channelId") int channelId,
                                                    @Path("currentLinkId") int currentLinkId);

    @GET("/channels/{channelId}/messages?type=new")
    ResMessages getPublicTopicUpdatedMessagesForMarker(@Query("teamId") int teamId, @Path("channelId") int channelId,
                                                       @Query("currentLinkId") int currentLinkId);


    @GET("/channels/{channelId}/messages")
    ResMessages getPublicTopicMarkerMessages(@Query("teamId") int teamId, @Path("channelId") int channelId,
                                             @Query("currentLinkId") int currentLinkId);

    // 채널에서 Message 생성
    @POST("/channels/{channelId}/message")
    ResCommon sendPublicTopicMessage(@Body ReqSendMessage message, @Path("channelId") int channelId);

    // 채널에서 Message 수정
    @PUT("/channels/{channelId}/messages/{messageId}")
    ResCommon modifyPublicTopicMessage(@Body ReqModifyMessage message, @Path("channelId") int channelId,
                                       @Path("messageId") int messageId);

    // 채널에서 Message 삭제
    @DELETE("/channels/{channelId}/messages/{messageId}")
    ResCommon deletePublicTopicMessage(@Query("teamId") int teamId, @Path("channelId") int channelId,
                                       @Path("messageId") int messageId);

}
