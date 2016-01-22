package com.tosslab.jandi.app.network.client.publictopic;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.AuthorizedHeader;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.DELETEWithBody;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqDeleteTopic;
import com.tosslab.jandi.app.network.models.ReqInviteTopicUsers;
import com.tosslab.jandi.app.network.models.ResCommon;

import retrofit.http.Body;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by tee on 15. 6. 16..
 */

@AuthorizedHeader
public interface ChannelApiV2Client {

    // 채널 생성
    @POST("/channel")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
    ResCommon createChannel(@Query("teamId") long teamId, @Body ReqCreateTopic channel);

    @PUT("/channels/{channelId}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
    ResCommon modifyPublicTopicName(@Query("teamId") long teamId,
                                    @Body ReqCreateTopic channel,
                                    @Path("channelId") long channelId);

    // 채널 삭제
    @DELETEWithBody("/channels/{channelId}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon deleteTopic(@Path("channelId") long channelId, @Body ReqDeleteTopic reqDeleteTopic);

    // 채널 Join
    @PUT("/channels/{channelId}/join")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon joinTopic(@Path("channelId") long channelId, @Body ReqDeleteTopic reqDeleteTopic);

    // 채널 leave
    @PUT("/channels/{channelId}/leave")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon leaveTopic(@Path("channelId") long channelId, @Body ReqDeleteTopic reqDeleteTopic);

    // 채널 invite
    @PUT("/channels/{channelId}/invite")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon invitePublicTopic(@Path("channelId") long channelId, @Body ReqInviteTopicUsers reqInviteTopicUsers);
}
