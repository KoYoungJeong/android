package com.tosslab.jandi.app.network.client.privatetopic.messages;

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

@Deprecated
@Accept(JandiV2HttpMessageConverter.APPLICATION_VERSION_FULL_NAME)
public interface GroupMessageApiClient {

    void setHeader(String name, String value);

    void setAuthentication(HttpAuthentication auth);

    // Private Group의 Message 리스트 정보 획득
    @Get("/privateGroups/{groupId}/messages?teamId={teamId}&linkId={fromId}&type=old&count={count}")
    @RequiresAuthentication
    ResMessages getGroupMessages(int teamId, int groupId, int fromId, int count);

    @Get("/privateGroups/{groupId}/messages?teamId={teamId}&type=old")
    @RequiresAuthentication
    ResMessages getGroupMessages(int teamId, int groupId);

    @Get("/privateGroups/{groupId}/messages/update/{lastLinkId}?teamId={teamId}")
    @RequiresAuthentication
    @Accept(JandiV3HttpMessageConverter.APPLICATION_VERSION_FULL_NAME)
    ResUpdateMessages getGroupMessagesUpdated(int teamId, int groupId, int lastLinkId);

    @Get("/privateGroups/{groupId}/messages?teamId={teamId}&linkId={currentLinkId}&type=new")
    @RequiresAuthentication
    ResMessages getGroupMessagesUpdatedForMarker(int teamId, int groupId, int currentLinkId);

    // Updated 된 Private Group의 리스트 정보 획득
    @Get("/privateGroups/{groupId}/messages?teamId={teamId}&linkId={currentLinkId}")
    @RequiresAuthentication
    ResMessages getGroupMarkerMessages(int teamId, int groupId, int currentLinkId);

    // Private Group에서의 Message 생성
    @Post("/privateGroups/{groupId}/message")
    @RequiresAuthentication
    ResCommon sendGroupMessage(ReqSendMessage message, int groupId);

    // Private Group Message 수정
    @Put("/privateGroups/{groupId}/messages/{messageId}")
    @RequiresAuthentication
    ResCommon modifyPrivateGroupMessage(ReqModifyMessage message,
                                        int groupId, int messageId);

    // Private Group Message 삭제
    @Delete("/privateGroups/{groupId}/messages/{messageId}?teamId={teamId}")
    @RequiresAuthentication
    ResCommon deletePrivateGroupMessage(int teamId, int groupId, int messageId);

}
