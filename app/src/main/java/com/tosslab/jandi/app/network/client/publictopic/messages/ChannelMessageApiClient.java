package com.tosslab.jandi.app.network.client.publictopic.messages;

import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessage;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.spring.JandiV2HttpMessageConverter;
import com.tosslab.jandi.app.network.spring.LoggerInterceptor;

import org.androidannotations.annotations.rest.Accept;
import org.androidannotations.annotations.rest.Delete;
import org.androidannotations.annotations.rest.Get;
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
        interceptors = {LoggerInterceptor.class}
)

@Accept(JandiV2HttpMessageConverter.APPLICATION_VERSION_FULL_NAME)
public interface ChannelMessageApiClient {

    void setHeader(String name, String value);

    void setAuthentication(HttpAuthentication auth);

    // 채널에서 Message 리스트 정보 획득
    @Get("/channels/{channelId}/messages?teamId={teamId}&linkId={fromId}&type=old")
    @RequiresAuthentication
    ResMessages getPublicTopicMessages(int teamId, int channelId, int fromId);

    @Get("/channels/{channelId}/messages?teamId={teamId}&type=old")
    @RequiresAuthentication
    ResMessages getPublicTopicMessages(int teamId, int channelId);

    // 채널의 업데이트 Message 리스트 정보 획득
    @Get("/channels/{channelId}/messages?teamId={teamId}&linkId={currentLinkId}&type=new")
    @RequiresAuthentication
    ResMessages getPublicTopicUpdatedMessages(int teamId, int channelId, int currentLinkId);

    // 채널의 업데이트 Message 리스트 정보 획득
    @Get("/channels/{channelId}/messages?teamId={teamId}&linkId={currentLinkId}")
    @RequiresAuthentication
    ResMessages getPublicTopicMarkerMessages(int teamId, int channelId, int currentLinkId);

    // 채널에서 Message 생성
    @Post("/channels/{channelId}/message")
    @RequiresAuthentication
    ResCommon sendPublicTopicMessage(ReqSendMessage message, int channelId);

    // 채널에서 Message 수정
    @Put("/channels/{channelId}/messages/{messageId}")
    @RequiresAuthentication
    ResCommon modifyPublicTopicMessage(ReqModifyMessage message, int channelId, int messageId);

    // 채널에서 Message 삭제
    @Delete("/channels/{channelId}/messages/{messageId}?teamId={teamId}")
    @RequiresAuthentication
    ResCommon deletePublicTopicMessage(int teamId, int channelId, int messageId);
}
