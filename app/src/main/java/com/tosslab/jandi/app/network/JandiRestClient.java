package com.tosslab.jandi.app.network;

import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqAccountActivate;
import com.tosslab.jandi.app.network.models.ReqCreateNewTeam;
import com.tosslab.jandi.app.network.models.ReqCreateTeam;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqDeleteTopic;
import com.tosslab.jandi.app.network.models.ReqInvitation;
import com.tosslab.jandi.app.network.models.ReqInvitationConfirm;
import com.tosslab.jandi.app.network.models.ReqInvitationMembers;
import com.tosslab.jandi.app.network.models.ReqInviteTopicUsers;
import com.tosslab.jandi.app.network.models.ReqInviteUsers;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqNotificationRegister;
import com.tosslab.jandi.app.network.models.ReqNotificationSubscribe;
import com.tosslab.jandi.app.network.models.ReqNotificationTarget;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ReqSendComment;
import com.tosslab.jandi.app.network.models.ReqSendMessage;
import com.tosslab.jandi.app.network.models.ReqSetMarker;
import com.tosslab.jandi.app.network.models.ReqShareMessage;
import com.tosslab.jandi.app.network.models.ReqSignUpInfo;
import com.tosslab.jandi.app.network.models.ReqUnshareMessage;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResInvitation;
import com.tosslab.jandi.app.network.models.ResInvitationMembers;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResMyTeam;
import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;
import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;
import com.tosslab.jandi.app.network.models.RestFileUploadResponse;

import org.androidannotations.annotations.rest.Accept;
import org.androidannotations.annotations.rest.Delete;
import org.androidannotations.annotations.rest.Get;
import org.androidannotations.annotations.rest.Post;
import org.androidannotations.annotations.rest.Put;
import org.androidannotations.annotations.rest.RequiresAuthentication;
import org.androidannotations.annotations.rest.RequiresHeader;
import org.androidannotations.annotations.rest.Rest;
import org.springframework.http.HttpAuthentication;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.MultiValueMap;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 5. 27..
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
public interface JandiRestClient {
    void setHeader(String name, String value);

    void setAuthentication(HttpAuthentication auth);

    // 클라이언트 Policy(+version) 정보
    @Get("/config")
    ResConfig getConfig();

    // 팀 생성 요청 이메일 전송
    @Deprecated
    @Post("/teams/new")
    ResCommon createTeam(ReqCreateTeam req);

    @Post("/teams")
    @RequiresAuthentication
    ResTeamDetailInfo createNewTeam(ReqCreateNewTeam req);

    // 내 팀 정보 획득
    @Get("/info/teamlist/email/{userEmail}")
    ResMyTeam getTeamId(String userEmail);

    // 로그인
    @Post("/token")
    @RequiresHeader("Content-Type")
    ResAccessToken getAccessToken(ReqAccessToken login);

    @Get("/account")
    @RequiresAuthentication
    ResAccountInfo getAccountInfo();

    @Get("/account/invitations")
    @RequiresAuthentication
    List<ResPendingTeamInfo> getMyPendingInvitations();

    @Post("/accounts")
    ResAccountInfo signUpAccount(ReqSignUpInfo signUpInfo);

    @Post("/accounts/activate")
    ResAccountInfo activateAccount(ReqAccountActivate reqAccountActivate);

    // 채널, PG, DM 리스트 획득
    @Get("/leftSideMenu?teamId={teamId}")
    @RequiresAuthentication
    ResLeftSideMenu getInfosForSideMenu(int teamId);

    // Entity별 badge 설정
    @Post("/entities/{entityId}/marker")
    @RequiresAuthentication
    ResCommon setMarker(int entityId, ReqSetMarker reqSetMarker);

    // 프로필
    @Get("/users/{userEntityId}")
    @RequiresAuthentication
    ResLeftSideMenu.User getUserProfile(int userEntityId);

    @Put("/settings/profile")
    @RequiresAuthentication
    ResLeftSideMenu.User updateUserProfile(ReqUpdateProfile reqUpdateProfile);

    // 팀 멤버 초대
    @Post("/invitation/team")
    @RequiresAuthentication
    @Deprecated
    ResInvitation inviteTeamMember(ReqInvitation invitation);

    /**
     * 여러명 초대하기.
     */
    @Post("/invitations")
    @RequiresAuthentication
    List<ResInvitationMembers> inviteMembers(ReqInvitationMembers invitation);

    /**
     * 초대 수락하기.
     */
    @Put("/invitations")
    @RequiresAuthentication
    List<ResTeamDetailInfo> confirmInvitation(ReqInvitationConfirm reqInvitationConfirm);

    /**
     * 초대 거절하기
     */
    @Put("/invitations")
    @RequiresAuthentication
    List<ResPendingTeamInfo> declineInvitation(ReqInvitationConfirm reqInvitationConfirm);

    /**
     * *********************************************************
     * 즐겨찾기
     * **********************************************************
     */
    @Post("/settings/starred/entities/{entityId}")
    @RequiresAuthentication
    ResCommon enableFavorite(int entityId);

    @Delete("/settings/starred/entities/{entityId}")
    @RequiresAuthentication
    ResCommon disableFavorite(int entityId);

    /**
     * *********************************************************
     * 채널 관련
     * 생성 / 수정 / 삭제, 내부 메시지 생성 / 수정 / 삭제
     * **********************************************************
     */
    // 채널에서 Message 리스트 정보 획득
    @Get("/channels/{channelId}/messages/{fromId}/{numOfPost}")
    @RequiresAuthentication
    ResMessages getChannelMessages(int channelId, int fromId, int numOfPost);

    // 채널의 업데이트 Message 리스트 정보 획득
    @Get("/channels/{channelId}/messages/update/{currentLinkId}")
    @RequiresAuthentication
    ResUpdateMessages getChannelMessagesUpdated(int channelId, int currentLinkId);

    // 채널 생성
    @Post("/channel")
    @RequiresAuthentication
    ResCommon createChannel(ReqCreateTopic channel);

    // 채널 이름 수정
    @Put("/channels/{channelId}")
    @RequiresAuthentication
    ResCommon modifyChannelName(ReqCreateTopic channel, int channelId);

    // 채널 삭제
    @Delete("/channels/{channelId}")
    @RequiresAuthentication
    @Deprecated
    ResCommon deleteChannel(int channelId);

    // 채널 삭제
    @Delete("/channels/{channelId}")
    @RequiresAuthentication
    ResCommon deleteTopic(int channelId, ReqDeleteTopic reqDeleteTopic);

    // 채널 Join
    @Put("/channels/{channelId}/join")
    @RequiresAuthentication
    @Deprecated
    ResCommon joinChannel(int channelId);

    // 채널 Join
    @Put("/channels/{channelId}/join")
    @RequiresAuthentication
    ResCommon joinTopic(int channelId, ReqDeleteTopic reqDeleteTopic);

    // 채널 leave
    @Put("/channels/{channelId}/leave")
    @RequiresAuthentication
    @Deprecated
    ResCommon leaveChannel(int channelId);

    // 채널 leave
    @Put("/channels/{channelId}/leave")
    @RequiresAuthentication
    ResCommon leaveTopic(int channelId, ReqDeleteTopic reqDeleteTopic);

    // 채널 invite
    @Put("/channels/{channelId}/invite")
    @RequiresAuthentication
    @Deprecated
    ResCommon inviteChannel(int channelId, ReqInviteUsers inviteUsers);

    // 채널 invite
    @Put("/channels/{channelId}/invite")
    @RequiresAuthentication
    ResCommon inviteTopic(int channelId, ReqInviteTopicUsers reqInviteTopicUsers);

    // 채널에서 Message 생성
    @Post("/channels/{channelId}/message")
    @RequiresAuthentication
    ResCommon sendChannelMessage(ReqSendMessage message, int channelId);

    // 채널에서 Message 수정
    @Put("/channels/{channelId}/messages/{messageId}")
    @RequiresAuthentication
    ResCommon modifyChannelMessage(ReqModifyMessage message,
                                   int channelId, int messageId);

    // 채널에서 Message 삭제
    @Delete("/channels/{channelId}/messages/{messageId}")
    @RequiresAuthentication
    ResCommon deleteChannelMessage(int channelId, int messageId);


    /**
     * *********************************************************
     * Direct Message 관련
     * 수정 / 삭제, 내부 메시지 생성 / 수정 / 삭제
     * **********************************************************
     */
    // Direct Message 리스트 정보 획득
    @Get("/users/{userId}/messages/{fromId}/{numOfPost}")
    @RequiresAuthentication
    ResMessages getDirectMessages(int userId, int fromId, int numOfPost);

    // Updated 된 Direct Message 리스트 정보 획득
    @Get("/users/{userId}/messages/update/{timeAfter}")
    @RequiresAuthentication
    ResUpdateMessages getDirectMessagesUpdated(int userId, long timeAfter);

    // Direct Message 생성
    @Post("/users/{userId}/message")
    @RequiresAuthentication
    ResCommon sendDirectMessage(ReqSendMessage message, int userId);

    // Direct Message 수정
    @Put("/users/{userId}/messages/{messageId}")
    @RequiresAuthentication
    ResCommon modifyDirectMessage(ReqModifyMessage message,
                                  int userId, int messageId);

    // Direct Message 삭제
    @Delete("/users/{userId}/messages/{messageId}")
    @RequiresAuthentication
    ResCommon deleteDirectMessage(int userId, int messageId);


    /**
     * *********************************************************
     * PG 관련
     * 생성 / 수정 / 삭제, 내부 메시지 생성 / 수정 / 삭제
     * **********************************************************
     */
    // Private Group의 Message 리스트 정보 획득
    @Get("/privateGroups/{groupId}/messages/{fromId}/{numOfPost}")
    @RequiresAuthentication
    ResMessages getGroupMessages(int groupId, int fromId, int numOfPost);

    // Updated 된 Private Group의 리스트 정보 획득
    @Get("/privateGroups/{groupId}/messages/update/{timeAfter}")
    @RequiresAuthentication
    ResUpdateMessages getGroupMessagesUpdated(int groupId, long timeAfter);

    // Private Group 생성
    @Post("/privateGroup")
    @RequiresAuthentication
    ResCommon createPrivateGroup(ReqCreateTopic group);

    // Private Group 수정
    @Put("/privateGroups/{groupId}")
    @RequiresAuthentication
    ResCommon modifyGroup(ReqCreateTopic channel, int groupId);

    // Private Group 삭제
    @Delete("/privateGroups/{groupId}")
    @RequiresAuthentication
    ResCommon deleteGroup(int groupId);

    // Private Group Leave
    @Put("/privateGroups/{groupId}/leave")
    @RequiresAuthentication
    ResCommon leaveGroup(int groupId);

    // Private Group invite
    @Put("/privateGroups/{groupId}/invite")
    @RequiresAuthentication
    ResCommon inviteGroup(int groupId, ReqInviteUsers inviteUsers);

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

    /**
     * *********************************************************
     * File
     * **********************************************************
     */
    // File Upload
    @Post("/file")
    @RequiresAuthentication
    RestFileUploadResponse uploadFile(MultiValueMap data);

    // Delete file
    @Delete("/files/{fileId}")
    @RequiresAuthentication
    ResCommon deleteFile(int fileId);

    // Message Detail
    @Get("/messages/{messageId}")
    @RequiresAuthentication
    ResFileDetail getFileDetail(int messageId);

    // Share Message
    @Put("/messages/{messageId}/share")
    @RequiresAuthentication
    ResCommon shareMessage(ReqShareMessage share, int messageId);

    // Unshare Message
    @Put("/messages/{messageId}/unshare")
    @RequiresAuthentication
    ResCommon unshareMessage(ReqUnshareMessage share, int messageId);

    // Send Comment
    @Post("/messages/{messageId}/comment")
    @RequiresAuthentication
    ResCommon sendMessageComment(ReqSendComment comment, int messageId);

    // Modify comment
    @Put("/messages/{messageId}/comments/{commentId}")
    @RequiresAuthentication
    ResCommon modifyMessageComment(ReqSendComment comment, int messageId, int commentId);

    // Delete comment
    @Delete("/messages/{messageId}/comments/{commentId}")
    @RequiresAuthentication
    ResCommon deleteMessageComment(int messageId, int commentId);

    /**
     * *********************************************************
     * Search
     * **********************************************************
     */
    // File search
    @Post("/search")
    @RequiresAuthentication
    ResSearchFile searchFile(ReqSearchFile reqSearchFile);

    /**
     * *********************************************************
     * Notification
     * **********************************************************
     */
    // Notification Token 등록
    @Put("/settings/notifications")
    @RequiresAuthentication
    ResCommon registerNotificationToken(ReqNotificationRegister reqNotificationRegister);

    // Notification Token 삭제
    @Delete("/settings/notifications/{deviceToken}")
    @RequiresAuthentication
    ResCommon deleteNotificationToken(String deviceToken);

    // Notification 켜고 끄기
    @Put("/settings/notifications/{deviceToken}/subscribe")
    @RequiresAuthentication
    ResCommon subscribeNotification(String deviceToken, ReqNotificationSubscribe reqNotificationSubscribe);

    // Notification Target 설정
    @Put("/settings/notification/target")
    @RequiresAuthentication
    ResCommon setNotificationTarget(ReqNotificationTarget reqNotificationTarget);
}
