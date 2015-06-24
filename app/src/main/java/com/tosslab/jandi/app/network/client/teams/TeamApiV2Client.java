package com.tosslab.jandi.app.network.client.teams;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.models.ReqCreateNewTeam;
import com.tosslab.jandi.app.network.models.ReqInvitationMembers;
import com.tosslab.jandi.app.network.models.ResInvitationMembers;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.network.spring.JandiV3HttpMessageConverter;

import java.util.List;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by tee on 15. 6. 16..
 */
public interface TeamApiV2Client {

    @POST("/teams")
    @Headers("Accept :" + JandiV3HttpMessageConverter.APPLICATION_VERSION_FULL_NAME)
    ResTeamDetailInfo createNewTeam(@Body ReqCreateNewTeam req);

    @GET("/teams/{teamId}/members/{memberId}")
    @Headers("Accept :" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResLeftSideMenu.User getMemberProfile(@Path("teamId") int teamId, @Path("memberId") int memberId);

    @POST("/teams/{teamId}/invitations")
    @Headers("Accept :" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    List<ResInvitationMembers> inviteToTeam(@Path("teamId") int teamId, @Body ReqInvitationMembers invitationMembers);

    @GET("/teams/{teamId}")
    @Headers("Accept :" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResTeamDetailInfo.InviteTeam getTeamInfo(@Path("teamId") int teamId);

}
