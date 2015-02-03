package com.tosslab.jandi.app.network.client.privatetopic;

import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqInviteTopicUsers;
import com.tosslab.jandi.app.network.models.ReqTeam;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.spring.JandiV2HttpMessageConverter;
import com.tosslab.jandi.app.network.spring.LoggerInterceptor;

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
        interceptors = {LoggerInterceptor.class}
)

@Accept(JandiV2HttpMessageConverter.APPLICATION_VERSION_FULL_NAME)
public interface GroupApiClient {


    void setHeader(String name, String value);

    void setAuthentication(HttpAuthentication auth);

    // Private Group 생성
    @Post("/privateGroup")
    @RequiresAuthentication
    ResCommon createPrivateGroup(ReqCreateTopic group);

    // Private Group 수정
    @Put("/privateGroups/{groupId}")
    @RequiresAuthentication
    ResCommon modifyGroup(ReqCreateTopic channel, int groupId);

    // Private Group 삭제
    @Delete("/privateGroups/{groupId}?teamId={teamId}")
    @RequiresAuthentication
    ResCommon deleteGroup(int teamId, int groupId);

    // Private Group Leave
    @Put("/privateGroups/{groupId}/leave")
    @RequiresAuthentication
    ResCommon leaveGroup(int groupId, ReqTeam team);

    // Private Group invite
    @Put("/privateGroups/{groupId}/invite")
    @RequiresAuthentication
    ResCommon inviteGroup(int groupId, ReqInviteTopicUsers inviteUsers);
}
