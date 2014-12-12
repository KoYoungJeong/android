package com.tosslab.jandi.app.network.client.privatetopic.messages;

import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.network.JandiV2HttpMessageConverter;
import com.tosslab.jandi.app.network.LoggerInterceptor;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessage;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;

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
public interface GroupMessageApiClient {

    void setHeader(String name, String value);

    void setAuthentication(HttpAuthentication auth);

    // Private Group의 Message 리스트 정보 획득
    @Get("/privateGroups/{groupId}/messages/{fromId}/{numOfPost}")
    @RequiresAuthentication
    ResMessages getGroupMessages(int groupId, int fromId, int numOfPost);

    // Updated 된 Private Group의 리스트 정보 획득
    @Get("/privateGroups/{groupId}/messages/update/{timeAfter}")
    @RequiresAuthentication
    ResUpdateMessages getGroupMessagesUpdated(int groupId, long timeAfter);

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
    @Delete("/privateGroups/{groupId}/messages/{messageId}")
    @RequiresAuthentication
    ResCommon deletePrivateGroupMessage(int groupId, int messageId);

}
