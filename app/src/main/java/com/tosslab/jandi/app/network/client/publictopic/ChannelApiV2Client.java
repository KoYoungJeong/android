package com.tosslab.jandi.app.network.client.publictopic;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqDeleteTopic;
import com.tosslab.jandi.app.network.models.ReqInviteTopicUsers;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessage;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;
import com.tosslab.jandi.app.network.spring.JandiV3HttpMessageConverter;

import org.androidannotations.annotations.rest.Delete;
import org.androidannotations.annotations.rest.Post;
import org.androidannotations.annotations.rest.Put;
import org.androidannotations.annotations.rest.RequiresAuthentication;

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

public interface ChannelApiV2Client {

    // 채널 생성
    @POST("/channel")
    @Headers("Accept :"+ JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon createChannel(@Body ReqCreateTopic channel);

    @PUT("/channels/{channelId}")
    @Headers("Accept :"+ JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon modifyPublicTopicName(@Body ReqCreateTopic channel, @Path("channelId") int channelId);

    // 채널 삭제
    @DELETE("/channels/{channelId}")
    @Headers("Accept :"+ JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon deleteTopic(@Path("channelId") int channelId, @Body ReqDeleteTopic reqDeleteTopic);

    // 채널 Join
    @PUT("/channels/{channelId}/join")
    @Headers("Accept :"+ JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon joinTopic(@Path("channelId") int channelId, @Body ReqDeleteTopic reqDeleteTopic);

    // 채널 leave
    @PUT("/channels/{channelId}/leave")
    @Headers("Accept :"+ JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon leaveTopic(@Path("channelId") int channelId, @Body ReqDeleteTopic reqDeleteTopic);

    // 채널 invite
    @PUT("/channels/{channelId}/invite")
    @Headers("Accept :"+ JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon invitePublicTopic(@Path("channelId") int channelId, @Body ReqInviteTopicUsers reqInviteTopicUsers);
}
