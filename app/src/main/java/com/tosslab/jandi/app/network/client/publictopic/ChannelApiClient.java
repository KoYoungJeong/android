package com.tosslab.jandi.app.network.client.publictopic;

import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.network.spring.HttpRequestFactory;
import com.tosslab.jandi.app.network.spring.JandiV2HttpMessageConverter;
import com.tosslab.jandi.app.network.spring.LoggerInterceptor;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqDeleteTopic;
import com.tosslab.jandi.app.network.models.ReqInviteTopicUsers;
import com.tosslab.jandi.app.network.models.ReqInviteUsers;
import com.tosslab.jandi.app.network.models.ResCommon;

import org.androidannotations.annotations.rest.Accept;
import org.androidannotations.annotations.rest.Delete;
import org.androidannotations.annotations.rest.Post;
import org.androidannotations.annotations.rest.Put;
import org.androidannotations.annotations.rest.RequiresAuthentication;
import org.androidannotations.annotations.rest.Rest;
import org.springframework.http.HttpAuthentication;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;

/**
 * Created by Steve SeongUg Jung on 14. 12. 12..
 */
@Rest(
        rootUrl = JandiConstantsForFlavors.SERVICE_ROOT_URL + "inner-api",
        converters = {
                JandiV2HttpMessageConverter.class,
                ByteArrayHttpMessageConverter.class,
                FormHttpMessageConverter.class,
                StringHttpMessageConverter.class},
        requestFactory = HttpRequestFactory.class,
        interceptors = {LoggerInterceptor.class}
)

@Deprecated
@Accept(JandiV2HttpMessageConverter.APPLICATION_VERSION_FULL_NAME)
public interface ChannelApiClient {

    void setHeader(String name, String value);

    void setAuthentication(HttpAuthentication auth);


    // 채널 생성
    @Post("/channel")
    @RequiresAuthentication
    ResCommon createChannel(ReqCreateTopic channel);

    // 채널 이름 수정
    @Put("/channels/{channelId}")
    @RequiresAuthentication
    @Deprecated
    ResCommon modifyChannelName(ReqCreateTopic channel, int channelId);

    @Put("/channels/{channelId}")
    @RequiresAuthentication
    ResCommon modifyPublicTopicName(ReqCreateTopic channel, int channelId);

    // 채널 삭제
    @Delete("/channels/{channelId}")
    @RequiresAuthentication
    @Deprecated
    ResCommon deleteChannel(int channelId);

    // 채널 삭제
    @Delete("/channels/{channelId}")
    @RequiresAuthentication
    ResCommon deleteTopic(int channelId, ReqDeleteTopic reqDeleteTopic);

    // 채널 Join
    @Put("/channels/{channelId}/join")
    @RequiresAuthentication
    @Deprecated
    ResCommon joinChannel(int channelId);

    // 채널 Join
    @Put("/channels/{channelId}/join")
    @RequiresAuthentication
    ResCommon joinTopic(int channelId, ReqDeleteTopic reqDeleteTopic);

    // 채널 leave
    @Put("/channels/{channelId}/leave")
    @RequiresAuthentication
    @Deprecated
    ResCommon leaveChannel(int channelId);

    // 채널 leave
    @Put("/channels/{channelId}/leave")
    @RequiresAuthentication
    ResCommon leaveTopic(int channelId, ReqDeleteTopic reqDeleteTopic);

    // 채널 invite
    @Put("/channels/{channelId}/invite")
    @RequiresAuthentication
    ResCommon invitePublicTopic(int channelId, ReqInviteTopicUsers reqInviteTopicUsers);

}
