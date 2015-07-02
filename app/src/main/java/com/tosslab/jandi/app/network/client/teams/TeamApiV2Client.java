package com.tosslab.jandi.app.network.client.teams;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.AuthorizedHeader;
import com.tosslab.jandi.app.network.models.ReqCreateAnnouncement;
import com.tosslab.jandi.app.network.models.ReqCreateNewTeam;
import com.tosslab.jandi.app.network.models.ReqInvitationMembers;
import com.tosslab.jandi.app.network.models.ReqUpdateAnnouncementStatus;
import com.tosslab.jandi.app.network.models.ResAnnouncement;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResInvitationMembers;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.network.spring.JandiV3HttpMessageConverter;

import org.androidannotations.annotations.rest.RequiresAuthentication;

import java.util.List;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;

/**
 * Created by tee on 15. 6. 16..
 */
@AuthorizedHeader
public interface TeamApiV2Client {

    @POST("/teams")
    @Headers("Accept:" + JandiV3HttpMessageConverter.APPLICATION_VERSION_FULL_NAME)
    ResTeamDetailInfo createNewTeam(@Body ReqCreateNewTeam req);

    @GET("/teams/{teamId}/members/{memberId}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResLeftSideMenu.User getMemberProfile(@Path("teamId") int teamId, @Path("memberId") int memberId);

    @POST("/teams/{teamId}/invitations")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    List<ResInvitationMembers> inviteToTeam(@Path("teamId") int teamId, @Body ReqInvitationMembers invitationMembers);

    @GET("/teams/{teamId}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResTeamDetailInfo.InviteTeam getTeamInfo(@Path("teamId") int teamId);

    @GET("/teams/{teamId}/topics/{topicId}/announcement")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResAnnouncement getAnnouncement(@Path("teamId") int teamId, @Path("topicId") int topicId);

    @POST("/teams/{teamId}/topics/{topicId}/announcement")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon createAnnouncement(@Path("teamId") int teamId, @Path("topicId") int topicId, @Body ReqCreateAnnouncement reqCreateAnnouncement);

    @PUT("/teams/{teamId}/members/{memberId}/announcement")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon updateAnnouncementStatus(@Path("teamId") int teamId, @Path("memberId") int memberId, @Body ReqUpdateAnnouncementStatus reqUpdateAnnouncementStatus);

    @DELETE("/teams/{teamId}/topics/{topicId}/announcement")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon deleteAnnouncement(@Path("teamId") int teamId, @Path("topicId") int topicId);

}
