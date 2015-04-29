package com.tosslab.jandi.app.network.client.teams;

import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.network.models.ReqCreateNewTeam;
import com.tosslab.jandi.app.network.models.ReqInvitationMembers;
import com.tosslab.jandi.app.network.models.ResInvitationMembers;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.network.spring.JandiV2HttpMessageConverter;
import com.tosslab.jandi.app.network.spring.JandiV3HttpMessageConverter;
import com.tosslab.jandi.app.network.spring.LoggerInterceptor;

import org.androidannotations.annotations.rest.Accept;
import org.androidannotations.annotations.rest.Get;
import org.androidannotations.annotations.rest.Post;
import org.androidannotations.annotations.rest.RequiresAuthentication;
import org.androidannotations.annotations.rest.Rest;
import org.springframework.http.HttpAuthentication;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;

/**
 * Created by Steve SeongUg Jung on 14. 12. 15..
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
public interface TeamsApiClient {

    void setHeader(String name, String value);

    void setAuthentication(HttpAuthentication auth);


    @Post("/teams")
    @RequiresAuthentication
    @Accept(JandiV3HttpMessageConverter.APPLICATION_VERSION_FULL_NAME)
    ResTeamDetailInfo createNewTeam(ReqCreateNewTeam req);


    @Get("/teams/{teamId}/members/{memberId}")
    @RequiresAuthentication
    ResLeftSideMenu.User getMemberProfile(int teamId, int memberId);

    @Post("/teams/{teamId}/invitations")
    @RequiresAuthentication
    ResInvitationMembers inviteToTeam(int teamId, ReqInvitationMembers invitationMembers);


    @Get("/teams/{teamId}")
    @RequiresAuthentication
    ResTeamDetailInfo.InviteTeam getTeamInfo(int teamId);
}
