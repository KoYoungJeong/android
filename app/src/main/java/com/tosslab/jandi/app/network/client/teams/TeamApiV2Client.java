package com.tosslab.jandi.app.network.client.teams;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.AuthorizedHeader;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.DELETEWithBody;
import com.tosslab.jandi.app.network.models.ReqCreateAnnouncement;
import com.tosslab.jandi.app.network.models.ReqCreateFolder;
import com.tosslab.jandi.app.network.models.ReqCreateNewTeam;
import com.tosslab.jandi.app.network.models.ReqInvitationMembers;
import com.tosslab.jandi.app.network.models.ReqMember;
import com.tosslab.jandi.app.network.models.ReqNull;
import com.tosslab.jandi.app.network.models.ReqOwner;
import com.tosslab.jandi.app.network.models.ReqRegistFolderItem;
import com.tosslab.jandi.app.network.models.ReqUpdateAnnouncementStatus;
import com.tosslab.jandi.app.network.models.ReqUpdateFolder;
import com.tosslab.jandi.app.network.models.ReqUpdateTopicPushSubscribe;
import com.tosslab.jandi.app.network.models.ResAnnouncement;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResCreateFolder;
import com.tosslab.jandi.app.network.models.ResFolder;
import com.tosslab.jandi.app.network.models.ResFolderItem;
import com.tosslab.jandi.app.network.models.ResInvitationMembers;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResRegistFolderItem;
import com.tosslab.jandi.app.network.models.ResStarMentioned;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.network.models.ResUpdateFolder;
import com.tosslab.jandi.app.network.models.commonobject.StarMentionedMessageObject;

import java.util.List;

import retrofit.http.Body;
import retrofit.http.DELETE;
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
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
    ResTeamDetailInfo createNewTeam(@Body ReqCreateNewTeam req);

    @GET("/teams/{teamId}/members/{memberId}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResLeftSideMenu.User getMemberProfile(@Path("teamId") long teamId, @Path("memberId") long memberId);

    @POST("/teams/{teamId}/invitations")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    List<ResInvitationMembers> inviteToTeam(@Path("teamId") long teamId, @Body ReqInvitationMembers invitationMembers);

    @DELETE("/teams/{teamId}/members/{memberId}/invitation")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon cancelInvitationUser(@Path("teamId") long teamId, @Path("memberId") long memberId);

    @GET("/teams/{teamId}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResTeamDetailInfo.InviteTeam getTeamInfo(@Path("teamId") long teamId);

    @GET("/teams/{teamId}/topics/{topicId}/announcement")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResAnnouncement getAnnouncement(@Path("teamId") long teamId, @Path("topicId") long topicId);

    @POST("/teams/{teamId}/topics/{topicId}/announcement")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon createAnnouncement(@Path("teamId") long teamId, @Path("topicId") long topicId, @Body ReqCreateAnnouncement reqCreateAnnouncement);

    @PUT("/teams/{teamId}/members/{memberId}/announcement")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon updateAnnouncementStatus(@Path("teamId") long teamId, @Path("memberId") long memberId, @Body ReqUpdateAnnouncementStatus reqUpdateAnnouncementStatus);

    @DELETEWithBody("/teams/{teamId}/topics/{topicId}/announcement")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon deleteAnnouncement(@Path("teamId") long teamId, @Path("topicId") long topicId);

    @GET("/teams/{teamId}/messages/{messageId}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResMessages.OriginalMessage getMessage(@Path("teamId") long teamId, @Path("messageId") long messageId);

    @PUT("/teams/{teamId}/rooms/{roomId}/subscribe")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon updateTopicPushSubscribe(@Path("teamId") long teamId, @Path("roomId") long topicId, @Body ReqUpdateTopicPushSubscribe reqUpdateTopicPushSubscribe);


    @POST("/teams/{teamId}/messages/{messageId}/starred")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    StarMentionedMessageObject registStarredMessage(@Path("teamId") long teamId, @Path("messageId") long messageId, @Body ReqNull reqNull);

    @DELETEWithBody("/teams/{teamId}/messages/{messageId}/starred")
    ResCommon unregistStarredMessage(@Path("teamId") long teamId, @Path("messageId") long messageId);

    @GET("/teams/{teamId}/messages/mentioned")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResStarMentioned getMentionedMessages(@Path("teamId") long teamId,
                                          @Query("messageId") long messageId, @Query("count") int count);

    @GET("/teams/{teamId}/messages/starred")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResStarMentioned getStarredMessages(@Path("teamId") long teamId, @Query("starredId") long starredId,
                                        @Query("count") int count, @Query("type") String type);

    @POST("/teams/{teamId}/folders")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCreateFolder createFolder(@Path("teamId") long teamId, @Body ReqCreateFolder reqCreateFolder);

    @DELETE("/teams/{teamId}/folders/{folderId}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon deleteFolder(@Path("teamId") long teamId, @Path("folderId") long folderId);

    @PUT("/teams/{teamId}/folders/{folderId}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResUpdateFolder updateFolder(@Path("teamId") long teamId, @Path("folderId") long folderId,
                                 @Body ReqUpdateFolder reqUpdateFolder);

    @GET("/teams/{teamId}/folders")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    List<ResFolder> getFolders(@Path("teamId") long teamId);

    @GET("/teams/{teamId}/folders/items")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    List<ResFolderItem> getFolderItems(@Path("teamId") long teamId);

    @POST("/teams/{teamId}/folders/{folderId}/items")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResRegistFolderItem registFolderItem(@Path("teamId") long teamId, @Path("folderId") long folderId,
                                         @Body ReqRegistFolderItem reqRegistFolderItem);

    @DELETE("/teams/{teamId}/folders/{folderId}/items/{itemId}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon deleteFolderItem(@Path("teamId") long teamId, @Path("folderId") long folderId,
                               @Path("itemId") long itemId);

    @PUT("/teams/{teamId}/topics/{topicId}/kickout")
    ResCommon kickUserFromTopic(@Path("teamId") long teamId, @Path("topicId") long topicId, @Body ReqMember member);

    @PUT("/teams/{teamId}/topics/{topicId}/admin")
    ResCommon assignToTopicOwner(@Path("teamId") long teamId, @Path("topicId") long topicId, @Body ReqOwner owner);

    @PUT("/teams/{teamId}/files/{fileId}/externalShared")
    ResMessages.FileMessage enableFileExternalLink(@Path("teamId") long teamId, @Path("fileId") long fileId, @Body ReqNull reqNull);

    @DELETE("/teams/{teamId}/files/{fileId}/externalShared")
    ResMessages.FileMessage disableFileExternalLink(@Path("teamId") long teamId, @Path("fileId") long fileId);
}
