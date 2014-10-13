package com.tosslab.jandi.app.network;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.models.ReqCreateEntity;
import com.tosslab.jandi.app.network.models.ReqCreateTeam;
import com.tosslab.jandi.app.network.models.ReqInvitation;
import com.tosslab.jandi.app.network.models.ReqInviteUsers;
import com.tosslab.jandi.app.network.models.ReqLogin;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqNotificationRegister;
import com.tosslab.jandi.app.network.models.ReqNotificationSubscribe;
import com.tosslab.jandi.app.network.models.ReqNotificationTarget;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ReqSendComment;
import com.tosslab.jandi.app.network.models.ReqSendMessage;
import com.tosslab.jandi.app.network.models.ReqSetMarker;
import com.tosslab.jandi.app.network.models.ReqShareMessage;
import com.tosslab.jandi.app.network.models.ReqUnshareMessage;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResAuthToken;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResInvitation;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResMyTeam;
import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;
import com.tosslab.jandi.app.network.models.RestFileUploadResponse;

import org.androidannotations.annotations.rest.Accept;
import org.androidannotations.annotations.rest.Delete;
import org.androidannotations.annotations.rest.Get;
import org.androidannotations.annotations.rest.Post;
import org.androidannotations.annotations.rest.Put;
import org.androidannotations.annotations.rest.RequiresHeader;
import org.androidannotations.annotations.rest.Rest;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.MultiValueMap;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
@Rest(
        rootUrl = JandiConstants.SERVICE_ROOT_URL + "inner-api",
        converters = {
                JandiV1HttpMessageConverter.class,
                ByteArrayHttpMessageConverter.class,
                FormHttpMessageConverter.class,
                StringHttpMessageConverter.class },
        interceptors = { LoggerInterceptor.class }
)

@Accept(JandiV1HttpMessageConverter.APPLICATION_VERSION_FULL_NAME)
public interface JandiRestClient {
    void setHeader(String name, String value);

    // 클라이언트 Policy(+version) 정보
    @Get("/config")
    ResConfig getConfig();

    // 팀 생성 요청 이메일 전송
    @Post("/teams/new")
    ResCommon createTeam(ReqCreateTeam req);

    // 내 팀 정보 획득
    @Get("/info/teamlist/email/{userEmail}")
    ResMyTeam getTeamId(String userEmail);

    // 로그인
    @Post("/token")
    ResAuthToken loginAndReturnToken(ReqLogin login);

    // 채널, PG, DM 리스트 획득
    @Get("/leftSideMenu")
    @RequiresHeader("Authorization")
    ResLeftSideMenu getInfosForSideMenu();

    // Entity별 badge 설정
    @Post("/entities/{entityId}/marker")
    @RequiresHeader("Authorization")
    ResCommon setMarker(int entityId, ReqSetMarker reqSetMarker);

    // 프로필
    @Get("/users/{userEntityId}")
    @RequiresHeader("Authorization")
    ResLeftSideMenu.User getUserProfile(int userEntityId);

    @Put("/settings/profile")
    @RequiresHeader("Authorization")
    ResLeftSideMenu.User updateUserProfile(ReqUpdateProfile reqUpdateProfile);

    // 팀 멤버 초대
    @Post("/invitation/team")
    @RequiresHeader("Authorization")
    ResInvitation inviteTeamMember(ReqInvitation invitation);

    /************************************************************
     * 채널 관련
     * 생성 / 수정 / 삭제, 내부 메시지 생성 / 수정 / 삭제
     ************************************************************/
    // 채널에서 Message 리스트 정보 획득
    @Get("/channels/{channelId}/messages/{fromId}/{numOfPost}")
    @RequiresHeader("Authorization")
    ResMessages getChannelMessages(int channelId, int fromId, int numOfPost);

    // 채널의 업데이트 Message 리스트 정보 획득
    @Get("/channels/{channelId}/messages/update/{currentLinkId}")
    @RequiresHeader("Authorization")
    ResUpdateMessages getChannelMessagesUpdated(int channelId, int currentLinkId);

    // 채널 생성
    @Post("/channel")
    @RequiresHeader("Authorization")
    ResCommon createChannel(ReqCreateEntity channel);

    // 채널 수정
    @Put("/channels/{channelId}")
    @RequiresHeader("Authorization")
    ResCommon modifyChannel(ReqCreateEntity channel, int channelId);

    // 채널 삭제
    @Delete("/channels/{channelId}")
    @RequiresHeader("Authorization")
    ResCommon deleteChannel(int channelId);

    // 채널 Join
    @Put("/channels/{channelId}/join")
    @RequiresHeader("Authorization")
    ResCommon joinChannel(int channelId);

    // 채널 leave
    @Put("/channels/{channelId}/leave")
    @RequiresHeader("Authorization")
    ResCommon leaveChannel(int channelId);

    // 채널 invite
    @Put("/channels/{channelId}/invite")
    @RequiresHeader("Authorization")
    ResCommon inviteChannel(int channelId, ReqInviteUsers inviteUsers);

    // 채널에서 Message 생성
    @Post("/channels/{channelId}/message")
    @RequiresHeader("Authorization")
    ResCommon sendChannelMessage(ReqSendMessage message, int channelId);

    // 채널에서 Message 수정
    @Put("/channels/{channelId}/messages/{messageId}")
    @RequiresHeader("Authorization")
    ResCommon modifyChannelMessage(ReqModifyMessage message,
                                           int channelId, int messageId);

    // 채널에서 Message 삭제
    @Delete("/channels/{channelId}/messages/{messageId}")
    @RequiresHeader("Authorization")
    ResCommon deleteChannelMessage(int channelId, int messageId);


    /************************************************************
     * Direct Message 관련
     * 수정 / 삭제, 내부 메시지 생성 / 수정 / 삭제
     ************************************************************/
    // Direct Message 리스트 정보 획득
    @Get("/users/{userId}/messages/{fromId}/{numOfPost}")
    @RequiresHeader("Authorization")
    ResMessages getDirectMessages(int userId, int fromId, int numOfPost);

    // Updated 된 Direct Message 리스트 정보 획득
    @Get("/users/{userId}/messages/update/{timeAfter}")
    @RequiresHeader("Authorization")
    ResUpdateMessages getDirectMessagesUpdated(int userId, long timeAfter);

    // Direct Message 생성
    @Post("/users/{userId}/message")
    @RequiresHeader("Authorization")
    ResCommon sendDirectMessage(ReqSendMessage message, int userId);

    // Direct Message 수정
    @Put("/users/{userId}/messages/{messageId}")
    @RequiresHeader("Authorization")
    ResCommon modifyDirectMessage(ReqModifyMessage message,
                                          int userId, int messageId);

    // Direct Message 삭제
    @Delete("/users/{userId}/messages/{messageId}")
    @RequiresHeader("Authorization")
    ResCommon deleteDirectMessage(int userId, int messageId);


    /************************************************************
     * PG 관련
     * 생성 / 수정 / 삭제, 내부 메시지 생성 / 수정 / 삭제
     ************************************************************/
    // Private Group의 Message 리스트 정보 획득
    @Get("/privateGroups/{groupId}/messages/{fromId}/{numOfPost}")
    @RequiresHeader("Authorization")
    ResMessages getGroupMessages(int groupId, int fromId, int numOfPost);

    // Updated 된 Private Group의 리스트 정보 획득
    @Get("/privateGroups/{groupId}/messages/update/{timeAfter}")
    @RequiresHeader("Authorization")
    ResUpdateMessages getGroupMessagesUpdated(int groupId, long timeAfter);

    // Private Group 생성
    @Post("/privateGroup")
    @RequiresHeader("Authorization")
    ResCommon createPrivateGroup(ReqCreateEntity group);

    // Private Group 수정
    @Put("/privateGroups/{groupId}")
    @RequiresHeader("Authorization")
    ResCommon modifyGroup(ReqCreateEntity channel, int groupId);

    // Private Group 삭제
    @Delete("/privateGroups/{groupId}")
    @RequiresHeader("Authorization")
    ResCommon deleteGroup(int groupId);

    // Private Group Leave
    @Put("/privateGroups/{groupId}/leave")
    @RequiresHeader("Authorization")
    ResCommon leaveGroup(int groupId);

    // Private Group invite
    @Put("/privateGroups/{groupId}/invite")
    @RequiresHeader("Authorization")
    ResCommon inviteGroup(int groupId, ReqInviteUsers inviteUsers);

    // Private Group에서의 Message 생성
    @Post("/privateGroups/{groupId}/message")
    @RequiresHeader("Authorization")
    ResCommon sendGroupMessage(ReqSendMessage message, int groupId);

    // Private Group Message 수정
    @Put("/privateGroups/{groupId}/messages/{messageId}")
    @RequiresHeader("Authorization")
    ResCommon modifyPrivateGroupMessage(ReqModifyMessage message,
                                           int groupId, int messageId);

    // Private Group Message 삭제
    @Delete("/privateGroups/{groupId}/messages/{messageId}")
    @RequiresHeader("Authorization")
    ResCommon deletePrivateGroupMessage(int groupId, int messageId);

    /************************************************************
     * File
     ************************************************************/
    // File Upload
    @Post("/file")
    @RequiresHeader("Authorization")
    RestFileUploadResponse uploadFile(MultiValueMap data);

    // Delete file
    @Delete("/files/{fileId}")
    @RequiresHeader("Authorization")
    ResCommon deleteFile(int fileId);

    // Message Detail
    @Get("/messages/{messageId}")
    @RequiresHeader("Authorization")
    ResFileDetail getFileDetail(int messageId);

    // Share Message
    @Put("/messages/{messageId}/share")
    @RequiresHeader("Authorization")
    ResCommon shareMessage(ReqShareMessage share, int messageId);

    // Unshare Message
    @Put("/messages/{messageId}/unshare")
    @RequiresHeader("Authorization")
    ResCommon unshareMessage(ReqUnshareMessage share, int messageId);

    // Send Comment
    @Post("/messages/{messageId}/comment")
    @RequiresHeader("Authorization")
    ResCommon sendMessageComment(ReqSendComment comment, int messageId);

    // Modify comment
    @Put("/messages/{messageId}/comments/{commentId}")
    @RequiresHeader("Authorization")
    ResCommon modifyMessageComment(ReqSendComment comment, int messageId, int commentId);

    // Delete comment
    @Delete("/messages/{messageId}/comments/{commentId}")
    @RequiresHeader("Authorization")
    ResCommon deleteMessageComment(int messageId, int commentId);

    /************************************************************
     * Search
     ************************************************************/
    // File search
    @Post("/search")
    @RequiresHeader("Authorization")
    ResSearchFile searchFile(ReqSearchFile reqSearchFile);

    /************************************************************
     * Notification
     ************************************************************/
    // Notification Token 등록
    @Post("/settings/notification")
    @RequiresHeader("Authorization")
    ResCommon registerNotificationToken(ReqNotificationRegister reqNotificationRegister);

    // Notification Token 삭제
    @Delete("/settings/notifications/{deviceToken}")
    @RequiresHeader("Authorization")
    ResCommon deleteNotificationToken(String deviceToken);

    // Notification 켜고 끄기
    @Put("/settings/notifications/{deviceToken}/subscribe")
    @RequiresHeader("Authorization")
    ResCommon subscribeNotification(String deviceToken, ReqNotificationSubscribe reqNotificationSubscribe);

    // Notification Target 설정
    @Put("/settings/notification/target")
    @RequiresHeader("Authorization")
    ResCommon setNotificationTarget(ReqNotificationTarget reqNotificationTarget);
}
