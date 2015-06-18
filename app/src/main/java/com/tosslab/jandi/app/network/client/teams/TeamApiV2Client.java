package com.tosslab.jandi.app.network.client.teams;

import com.tosslab.jandi.app.network.models.ReqCreateNewTeam;
import com.tosslab.jandi.app.network.models.ReqInvitationMembers;
import com.tosslab.jandi.app.network.models.ResInvitationMembers;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.network.spring.JandiV3HttpMessageConverter;

import org.androidannotations.annotations.rest.Accept;
import org.androidannotations.annotations.rest.Get;
import org.androidannotations.annotations.rest.Post;
import org.androidannotations.annotations.rest.RequiresAuthentication;

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
    ResLeftSideMenu.User getMemberProfile(@Path("teamId") int teamId,@Path("memberId") int memberId);

    @POST("/teams/{teamId}/invitations")
    List<ResInvitationMembers> inviteToTeam(@Path("teamId") int teamId,@Body ReqInvitationMembers invitationMembers);


    @GET("/teams/{teamId}")
    ResTeamDetailInfo.InviteTeam getTeamInfo(@Path("teamId") int teamId);

}
