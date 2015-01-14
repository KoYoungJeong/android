package com.tosslab.jandi.app.network.client.chat;

import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.network.models.ResChat;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.spring.HttpRequestFactory;
import com.tosslab.jandi.app.network.spring.JandiV2HttpMessageConverter;
import com.tosslab.jandi.app.network.spring.LoggerInterceptor;

import org.androidannotations.annotations.rest.Accept;
import org.androidannotations.annotations.rest.Delete;
import org.androidannotations.annotations.rest.Get;
import org.androidannotations.annotations.rest.RequiresAuthentication;
import org.androidannotations.annotations.rest.Rest;
import org.springframework.http.HttpAuthentication;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 1. 13..
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
@Accept(JandiV2HttpMessageConverter.APPLICATION_VERSION_FULL_NAME)
public interface ChatsApiClient {
    void setHeader(String name, String value);

    void setAuthentication(HttpAuthentication auth);


    @Get("/members/{memberId}/chats")
    @RequiresAuthentication
    List<ResChat> getChatList(int memberId);

    @Delete("/members/{memberId}/chats/{entityId}")
    @RequiresAuthentication
    ResCommon deleteChat(int memberId, int entityId);

}
