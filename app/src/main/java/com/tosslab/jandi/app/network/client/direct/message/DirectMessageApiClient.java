package com.tosslab.jandi.app.network.client.direct.message;

import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessage;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;
import com.tosslab.jandi.app.network.spring.JandiV2HttpMessageConverter;
import com.tosslab.jandi.app.network.spring.JandiV3HttpMessageConverter;
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
public interface DirectMessageApiClient {

    void setHeader(String name, String value);

    void setAuthentication(HttpAuthentication auth);

    // Direct Message 리스트 정보 획득
    @Get("/users/{userId}/messages?teamId={teamId}&linkId={fromId}&type=old")
    @RequiresAuthentication
    ResMessages getDirectMessages(int teamId, int userId, int fromId);

    @Get("/users/{userId}/messages?teamId={teamId}&type=old")
    @RequiresAuthentication
    ResMessages getDirectMessages(int teamId, int userId);

    @Get("/users/{userId}/messages/update/{timeAfter}?teamId={teamId}")
    @RequiresAuthentication
    @Accept(JandiV3HttpMessageConverter.APPLICATION_VERSION_FULL_NAME)
    ResUpdateMessages getDirectMessagesUpdated(int teamId, int userId, int timeAfter);

    // Updated 된 Direct Message 리스트 정보 획득
    @Get("/users/{userId}/messages?teamId={teamId}&linkId={currentLinkId}&type=new")
    @RequiresAuthentication
    ResMessages getDirectMessagesUpdatedForMarker(int teamId, int userId, int currentLinkId);


    // Updated 된 Direct Message 리스트 정보 획득
    @Get("/users/{userId}/messages?teamId={teamId}&linkId={currentLinkId}")
    @RequiresAuthentication
    ResMessages getDirectMarkerMessages(int teamId, int userId, int currentLinkId);

    // Direct Message 생성
    @Post("/users/{userId}/message")
    @RequiresAuthentication
    ResCommon sendDirectMessage(ReqSendMessage message, int userId);

    // Direct Message 수정
    @Put("/users/{userId}/messages/{messageId}")
    @RequiresAuthentication
    ResCommon modifyDirectMessage(ReqModifyMessage message,
                                  int userId, int messageId);

    // Direct Message 삭제
    @Delete("/users/{userId}/messages/{messageId}?teamId={teamId}")
    @RequiresAuthentication
    ResCommon deleteDirectMessage(int teamId, int userId, int messageId);

}
