package com.tosslab.jandi.app.network.client.teams;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.AuthorizedHeader;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.DELETEWithBody;
import com.tosslab.jandi.app.network.models.ReqCreateAnnouncement;
import com.tosslab.jandi.app.network.models.ReqCreateNewTeam;
import com.tosslab.jandi.app.network.models.ReqDeleteTopic;
import com.tosslab.jandi.app.network.models.ReqInvitationMembers;
import com.tosslab.jandi.app.network.models.ReqUpdateAnnouncementStatus;
import com.tosslab.jandi.app.network.models.ResAnnouncement;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResInvitationMembers;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMentioned;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResStarred;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.network.spring.JandiV3HttpMessageConverter;

import java.util.List;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

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

    @DELETEWithBody("/teams/{teamId}/topics/{topicId}/announcement")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon deleteAnnouncement(@Path("teamId") int teamId, @Path("topicId") int topicId);

    @GET("/teams/{teamId}/messages/{messageId}")
    ResMessages.OriginalMessage getMessage(@Path("teamId") int teamId, @Path("messageId") int messageId);

    @POST("/teams/{teamId}/messages/{messageId}/starred")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResStarred registStarredMessage(@Path("teamId") int teamId, @Path("messageId") int messageId, @Body ReqDeleteTopic reqDeleteTopic);

    @DELETEWithBody("/teams/{teamId}/messages/{messageId}/starred")
    ResCommon unregistStarredMessage(@Path("teamId") int teamId, @Path("messageId") int messageId);

    @GET("/teams/{teamId}/messages/mentioned")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResMentioned getMentionedMessages(@Path("teamId") int teamId,
                                      @Query("page") int page, @Query("perPage") int perPage);

    @GET("/teams/{teamId}/messages/starred")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResMentioned getStarredMessages(@Path("teamId") int teamId, @Query("type") String type,
                                    @Query("page") int page, @Query("perPage") int perPage);

}
