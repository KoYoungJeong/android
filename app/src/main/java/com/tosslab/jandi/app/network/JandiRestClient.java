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
import com.tosslab.jandi.app.network.models.ReqTeam;
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

}
