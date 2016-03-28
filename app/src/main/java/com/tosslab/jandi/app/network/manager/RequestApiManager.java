package com.tosslab.jandi.app.network.manager;

import com.tosslab.jandi.app.network.manager.apiexecutor.Executor;
import com.tosslab.jandi.app.network.manager.apiexecutor.PoolableRequestApiExecutor;
import com.tosslab.jandi.app.network.manager.apiloader.RestApiLoader;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqAccountActivate;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqAccountVerification;
import com.tosslab.jandi.app.network.models.ReqChangePassword;
import com.tosslab.jandi.app.network.models.ReqConfirmEmail;
import com.tosslab.jandi.app.network.models.ReqCreateAnnouncement;
import com.tosslab.jandi.app.network.models.ReqCreateFolder;
import com.tosslab.jandi.app.network.models.ReqCreateNewTeam;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqDeleteTopic;
import com.tosslab.jandi.app.network.models.ReqDeviceToken;
import com.tosslab.jandi.app.network.models.ReqInvitationAcceptOrIgnore;
import com.tosslab.jandi.app.network.models.ReqInvitationMembers;
import com.tosslab.jandi.app.network.models.ReqInviteTopicUsers;
import com.tosslab.jandi.app.network.models.ReqMember;
import com.tosslab.jandi.app.network.models.ReqModifyComment;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqModifyTopicAutoJoin;
import com.tosslab.jandi.app.network.models.ReqModifyTopicDescription;
import com.tosslab.jandi.app.network.models.ReqModifyTopicName;
import com.tosslab.jandi.app.network.models.ReqNotificationRegister;
import com.tosslab.jandi.app.network.models.ReqNotificationTarget;
import com.tosslab.jandi.app.network.models.ReqOwner;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ReqRegistFolderItem;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ReqSendComment;
import com.tosslab.jandi.app.network.models.ReqSendMessageV3;
import com.tosslab.jandi.app.network.models.ReqSetMarker;
import com.tosslab.jandi.app.network.models.ReqShareMessage;
import com.tosslab.jandi.app.network.models.ReqSignUpInfo;
import com.tosslab.jandi.app.network.models.ReqSubscibeToken;
import com.tosslab.jandi.app.network.models.ReqTeam;
import com.tosslab.jandi.app.network.models.ReqUnshareMessage;
import com.tosslab.jandi.app.network.models.ReqUpdateAnnouncementStatus;
import com.tosslab.jandi.app.network.models.ReqUpdateFolder;
import com.tosslab.jandi.app.network.models.ReqUpdatePlatformStatus;
import com.tosslab.jandi.app.network.models.ReqUpdatePrimaryEmailInfo;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ReqUpdateTopicPushSubscribe;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountActivate;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResAnnouncement;
import com.tosslab.jandi.app.network.models.ResAvatarsInfo;
import com.tosslab.jandi.app.network.models.ResChat;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.network.models.ResCreateFolder;
import com.tosslab.jandi.app.network.models.ResEventHistory;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResFolder;
import com.tosslab.jandi.app.network.models.ResFolderItem;
import com.tosslab.jandi.app.network.models.ResInvitationMembers;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessageSearch;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResMyTeam;
import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;
import com.tosslab.jandi.app.network.models.ResRegistFolderItem;
import com.tosslab.jandi.app.network.models.ResRoomInfo;
import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.network.models.ResStarMentioned;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.network.models.ResUpdateFolder;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;
import com.tosslab.jandi.app.network.models.commonobject.StarMentionedMessageObject;
import com.tosslab.jandi.app.network.models.sticker.ReqSendSticker;
import com.tosslab.jandi.app.network.models.validation.ResValidation;

import java.util.List;


public class RequestApiManager  {

    private static final RequestApiManager requestApiManager = new RequestApiManager();

    private RequestApiManager() {
    }

    public static final RequestApiManager getInstance() {
        return requestApiManager;
    }

    public <RESULT> RESULT requestApiExecute(Executor<RESULT> executor) {
        PoolableRequestApiExecutor requestApiexecutor = PoolableRequestApiExecutor.obtain();

        try {
            return requestApiexecutor.execute(executor);
        } catch (Exception e) {
            throw e;
        } finally {
            requestApiexecutor.recycle();
        }

    }

    public ResAccountInfo registerNotificationTokenByAccountDeviceApi(ReqNotificationRegister reqNotificationRegister) {
        return requestApiExecute(RestApiLoader.getInstance().loadRegisterNotificationTokenByAccountDeviceApi(reqNotificationRegister));
    }

    public ResAccountInfo deleteNotificationTokenByAccountDeviceApi(ReqDeviceToken reqDeviceToken) {
        return requestApiExecute(RestApiLoader.getInstance().loadDeleteNotificationTokenByAccountDeviceApi(reqDeviceToken));
    }

    public ResAccountInfo subscribeStateNotificationByAccountDeviceApi(ReqSubscibeToken reqDeviceToken) {
        return requestApiExecute(RestApiLoader.getInstance().loadSubscribeStateNotificationByAccountDeviceApi(reqDeviceToken));
    }

    public ResCommon getNotificationBadgeByAccountDeviceApi(ReqNotificationTarget reqNotificationTarget) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetNotificationBadgeByAccountDeviceApi(reqNotificationTarget));
    }

    public ResAccountInfo requestAddEmailByAccountEmailApi(ReqAccountEmail reqAccountEmail) {
        return requestApiExecute(RestApiLoader.getInstance().loadRequestAddEmailByAccountEmailsApi(reqAccountEmail));
    }

    public ResAccountInfo confirmEmailByAccountEmailApi(ReqConfirmEmail reqConfirmEmail) {
        return requestApiExecute(RestApiLoader.getInstance().loadConfirmEmailByAccountEmailsApi(reqConfirmEmail));
    }

    public ResAccountInfo deleteEmailByAccountEmailApi(ReqAccountEmail reqConfirmEmail) {
        return requestApiExecute(RestApiLoader.getInstance().loadExecutorDeleteEmailByAccountEmailsApi(reqConfirmEmail));
    }

    public ResCommon resetPasswordByAccountPasswordApi(ReqAccountEmail reqAccountEmail) {
        return requestApiExecute(RestApiLoader.getInstance().loadResetPasswordByAccountPasswordApi(reqAccountEmail));
    }

    public ResCommon changePasswordByAccountPasswordApi(ReqChangePassword reqConfirmEmail) {
        return requestApiExecute(RestApiLoader.getInstance().loadChangePasswordByAccountEmailsApi(reqConfirmEmail));
    }

    public ResConfig getConfigByMainRest() {
        return requestApiExecute(RestApiLoader.getInstance().loadGetConfigByMainRestApi());
    }

    public ResMyTeam getTeamIdByMainRest(String userEmail) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetTeamIdByMainRestApi(userEmail));
    }

    public ResAccessToken getAccessTokenByMainRest(ReqAccessToken login) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetAccessTokenByMainRestApi(login));
    }

    public ResCommon signUpAccountByMainRest(ReqSignUpInfo signUpInfo) {
        return requestApiExecute(RestApiLoader.getInstance().loadSignUpAccountByMainRestApi(signUpInfo));
    }

    public ResAccountActivate activateAccountByMainRest(ReqAccountActivate reqAccountActivate) {
        return requestApiExecute(RestApiLoader.getInstance().loadActivateAccountByMainRestApi(reqAccountActivate));
    }

    public ResCommon accountVerificationByMainRest(ReqAccountVerification reqAccountVerification) {
        return requestApiExecute(RestApiLoader.getInstance().loadAccountVerificationByMainRestApi(reqAccountVerification));
    }

    public ResAccountInfo getAccountInfoByMainRest() {
        return requestApiExecute(RestApiLoader.getInstance().loadGetAccountInfoByMainRestApi());
    }

    public ResAccountInfo updatePrimaryEmailByMainRest(ReqUpdatePrimaryEmailInfo updatePrimaryEmailInfo) {
        return requestApiExecute(RestApiLoader.getInstance().loadUpdatePrimaryEmailByMainRestApi(updatePrimaryEmailInfo));
    }

    public ResLeftSideMenu getInfosForSideMenuByMainRest(long teamId) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetInfosForSideMenuByMainRestApi(teamId));
    }

    public ResCommon setMarkerByMainRest(long entityId, ReqSetMarker reqSetMarker) {
        return requestApiExecute(RestApiLoader.getInstance().loadSetMarkerByMainRestApi(entityId, reqSetMarker));
    }

    public ResSearchFile searchFileByMainRest(ReqSearchFile reqSearchFile) {
        return requestApiExecute(RestApiLoader.getInstance().loadSearchFileByMainRestApi(reqSearchFile));
    }

    public ResAccountInfo changeNameByAccountProfileApi(ReqProfileName reqProfileName) {
        return requestApiExecute(RestApiLoader.getInstance().loadChangeNameByAccountProfileApi(reqProfileName));
    }

    public ResAccountInfo changePrimaryEmailByAccountProfileApi(ReqAccountEmail reqAccountEmail) {
        return requestApiExecute(RestApiLoader.getInstance().loadChangePrimaryEmailByAccountProfileApi(reqAccountEmail));
    }

    public ResCommon createChannelByChannelApi(ReqCreateTopic channel) {
        return requestApiExecute(RestApiLoader.getInstance().loadCreateChannelByChannelApi(channel));
    }

    public ResCommon modifyPublicTopicNameByChannelApi(ReqModifyTopicName channel, long channelId) {
        return requestApiExecute(RestApiLoader.getInstance().loadModifyPublicTopicNameByChannelApi(channel, channelId));
    }

    public ResCommon modifyPublicTopicDescriptionByChannelApi(ReqModifyTopicDescription description, long channelId) {
        return requestApiExecute(RestApiLoader.getInstance().loadModifyPublicTopicDescriptionByChannelApi(description, channelId));
    }

    public ResCommon modifyPublicTopicAutoJoinByChannelApi(ReqModifyTopicAutoJoin topicAutoJoin, long channelId) {
        return requestApiExecute(RestApiLoader.getInstance().loadModifyPublicTopicAutoJoinByChannelApi(topicAutoJoin, channelId));
    }

    public ResCommon deleteTopicByChannelApi(long channelId, ReqDeleteTopic reqDeleteTopic) {
        return requestApiExecute(RestApiLoader.getInstance().loadDeleteTopicByChannelApi(channelId, reqDeleteTopic));
    }

    public ResCommon joinTopicByChannelApi(long channelId, ReqDeleteTopic reqDeleteTopic) {
        return requestApiExecute(RestApiLoader.getInstance().loadJoinTopicByChannelApi(channelId, reqDeleteTopic));
    }

    public ResCommon leaveTopicByChannelApi(long channelId, ReqDeleteTopic reqDeleteTopic) {
        return requestApiExecute(RestApiLoader.getInstance().loadLeaveTopicByChannelApi(channelId, reqDeleteTopic));
    }

    public ResCommon invitePublicTopicByChannelApi(long channelId, ReqInviteTopicUsers reqInviteTopicUsers) {
        return requestApiExecute(RestApiLoader.getInstance().loadInvitePublicTopicByChannelApi(channelId, reqInviteTopicUsers));
    }

    public ResMessages getPublicTopicMessagesByChannelMessageApi(long teamId, long channelId, long fromId, int count) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetPublicTopicMessagesByChannelMessageApi(teamId, channelId, fromId, count));
    }

    public ResMessages getPublicTopicMessagesByChannelMessageApi(int teamId, int channelId) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetPublicTopicMessagesByChannelMessageApi(teamId, channelId));
    }

    public ResUpdateMessages getPublicTopicUpdatedMessagesByChannelMessageApi(int teamId, int channelId, int currentLinkId) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetPublicTopicUpdatedMessagesByChannelMessageApi(teamId, channelId, currentLinkId));
    }

    public ResMessages getPublicTopicUpdatedMessagesForMarkerByChannelMessageApi(long teamId, long channelId, long currentLinkId) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetPublicTopicUpdatedMessagesForMarkerByChannelMessageApi(teamId, channelId, currentLinkId));
    }

    public ResMessages getPublicTopicUpdatedMessagesForMarkerByChannelMessageApi(long teamId, long channelId, long currentLinkId, int count) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetPublicTopicUpdatedMessagesForMarkerByChannelMessageApi(teamId, channelId, currentLinkId, count));
    }

    public ResMessages getPublicTopicMarkerMessagesByChannelMessageApi(long teamId, long channelId, long currentLinkId) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetPublicTopicMarkerMessagesByChannelMessageApi(teamId, channelId, currentLinkId));
    }

    public ResCommon sendPublicTopicMessageByChannelMessageApi(long channelId, long teamId, ReqSendMessageV3 reqSendMessageV3) {
        return requestApiExecute(RestApiLoader.getInstance().loadSendPublicTopicMessageByChannelMessageApi(channelId, teamId, reqSendMessageV3));
    }

    public ResCommon modifyPublicTopicMessageByChannelMessageApi(ReqModifyMessage message, int channelId, int messageId) {
        return requestApiExecute(RestApiLoader.getInstance().loadModifyPublicTopicMessageByChannelMessageApi(message, channelId, messageId));
    }

    public ResCommon deletePublicTopicMessageByChannelMessageApi(long teamId, long channelId, long messageId) {
        return requestApiExecute(RestApiLoader.getInstance().loadDeletePublicTopicMessageByChannelMessageApi(teamId, channelId, messageId));
    }

    public List<ResChat> getChatListByChatApi(long memberId) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetChatListByChatApi(memberId));
    }

    public ResCommon deleteChatByChatApi(long memberId, long entityId) {
        return requestApiExecute(RestApiLoader.getInstance().loadDeleteChatByChatApi(memberId, entityId));
    }

    public ResCommon sendMessageCommentByCommentsApi(long messageId, long teamId, ReqSendComment reqSendComment) {
        return requestApiExecute(RestApiLoader.getInstance().loadSendMessageCommentByCommentsApi(messageId, teamId, reqSendComment));
    }

    public ResCommon modifyMessageCommentByCommentsApi(ReqModifyComment comment, int messageId, int commentId) {
        return requestApiExecute(RestApiLoader.getInstance().loadModifyMessageCommentByCommentsApi(comment, messageId, commentId));
    }

    public ResCommon deleteMessageCommentByCommentsApi(long teamId, long messageId, long commentId) {
        return requestApiExecute(RestApiLoader.getInstance().loadDeleteMessageCommentByCommentsApi(teamId, messageId, commentId));
    }

    public ResMessages getDirectMessagesByDirectMessageApi(long teamId, long userId, long fromId, int count) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetDirectMessagesByDirectMessageApi(teamId, userId, fromId, count));
    }

    public ResMessages getDirectMessagesByDirectMessageApi(int teamId, int userId) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetDirectMessagesByDirectMessageApi(teamId, userId));
    }

    public ResUpdateMessages getDirectMessagesUpdatedByDirectMessageApi(int teamId, int userId, int timeAfter) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetDirectMessagesUpdatedByDirectMessageApi(teamId, userId, timeAfter));
    }

    public ResMessages getDirectMessagesUpdatedForMarkerByDirectMessageApi(long teamId, long userId, long currentLinkId) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetDirectMessagesUpdatedForMarkerByDirectMessageApi(teamId, userId, currentLinkId));
    }

    public ResMessages getDirectMessagesUpdatedForMarkerByDirectMessageApi(long teamId, long userId, long currentLinkId, int count) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetDirectMessagesUpdatedForMarkerByDirectMessageApi(teamId, userId, currentLinkId, count));
    }

    public ResMessages getDirectMarkerMessagesByDirectMessageApi(long teamId, long userId, long currentLinkId) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetDirectMarkerMessagesByDirectMessageApi(teamId, userId, currentLinkId));
    }

    public ResCommon sendDirectMessageByDirectMessageApi(long userId, long teamId,
                                                         ReqSendMessageV3 reqSendMessageV3) {
        return requestApiExecute(RestApiLoader.getInstance().loadSendDirectMessageByDirectMessageApi(userId, teamId, reqSendMessageV3));
    }

    public ResCommon modifyDirectMessageByDirectMessageApi(ReqModifyMessage message, int userId, int messageId) {
        return requestApiExecute(RestApiLoader.getInstance().loadModifyDirectMessageByDirectMessageApi(message, userId, messageId));
    }

    public ResCommon deleteDirectMessageByDirectMessageApi(long teamId, long userId, long messageId) {
        return requestApiExecute(RestApiLoader.getInstance().loadDeleteDirectMessageByDirectMessageApi(teamId, userId, messageId));
    }

    public ResCommon createPrivateGroupByGroupApi(ReqCreateTopic group) {
        return requestApiExecute(RestApiLoader.getInstance().loadCreatePrivateGroupByGroupApi(group));
    }

    public ResCommon modifyGroupNameByGroupApi(ReqModifyTopicName channel, long groupId) {
        return requestApiExecute(RestApiLoader.getInstance().loadModifyGroupByGroupApi(channel, groupId));
    }

    public ResCommon deleteGroupByGroupApi(long teamId, long groupId) {
        return requestApiExecute(RestApiLoader.getInstance().loadDeleteGroupByGroupApi(teamId, groupId));
    }

    public ResCommon leaveGroupByGroupApi(long groupId, ReqTeam team) {
        return requestApiExecute(RestApiLoader.getInstance().loadLeaveGroupByGroupApi(groupId, team));
    }

    public ResCommon inviteGroupByGroupApi(long groupId, ReqInviteTopicUsers inviteUsers) {
        return requestApiExecute(RestApiLoader.getInstance().loadInviteGroupByGroupApi(groupId, inviteUsers));
    }

    public ResCommon modifyGroupDescriptionByGroupApi(ReqModifyTopicDescription description, long entityId) {
        return requestApiExecute(RestApiLoader.getInstance().loadModifyGroupDescriptionByGroupApi(description, entityId));
    }

    public ResMessages getGroupMessagesByGroupMessageApi(long teamId, long groupId, long fromId, int count) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetGroupMessagesByGroupMessageApi(teamId, groupId, fromId, count));
    }

    public ResMessages getGroupMessagesByGroupMessageApi(int teamId, int groupId) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetGroupMessagesByGroupMessageApi(teamId, groupId));
    }

    public ResUpdateMessages getGroupMessagesUpdatedByGroupMessageApi(int teamId, int groupId, int lastLinkId) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetGroupMessagesUpdatedByGroupMessageApi(teamId, groupId, lastLinkId));
    }

    public ResMessages getGroupMessagesUpdatedForMarkerByGroupMessageApi(long teamId, long groupId,
                                                                         long currentLinkId) {
        return requestApiExecute(RestApiLoader.getInstance()
                .loadGetGroupMessagesUpdatedForMarkerByGroupMessageApi(teamId, groupId, currentLinkId));
    }

    public ResMessages getGroupMessagesUpdatedForMarkerByGroupMessageApi(long teamId, long groupId, long currentLinkId, int count) {
        return requestApiExecute(RestApiLoader.getInstance()
                .loadGetGroupMessagesUpdatedForMarkerByGroupMessageApi(teamId, groupId, currentLinkId, count));
    }

    public ResMessages getGroupMarkerMessagesByGroupMessageApi(long teamId, long groupId, long currentLinkId) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetGroupMarkerMessagesByGroupMessageApi(teamId, groupId, currentLinkId));
    }

    public ResCommon sendGroupMessageByGroupMessageApi(long privateGroupId, long teamId, ReqSendMessageV3 reqSendMessageV3) {
        return requestApiExecute(RestApiLoader.getInstance().loadSendGroupMessageByGroupMessageApi(privateGroupId, teamId, reqSendMessageV3));
    }

    public ResCommon modifyPrivateGroupMessageByGroupMessageApi(ReqModifyMessage message, int groupId, int messageId) {
        return requestApiExecute(RestApiLoader.getInstance().loadModifyPrivateGroupMessageByGroupMessageApi(message, groupId, messageId));
    }

    public ResCommon deletePrivateGroupMessageByGroupMessageApi(long teamId, long groupId, long messageId) {
        return requestApiExecute(RestApiLoader.getInstance().loadDeletePrivateGroupMessageByGroupMessageApi(teamId, groupId, messageId));
    }

    public ResTeamDetailInfo acceptOrDeclineInvitationByInvitationApi(String invitationId, ReqInvitationAcceptOrIgnore reqInvitationAcceptOrIgnore) {
        return requestApiExecute(RestApiLoader.getInstance().loadAcceptOrDeclineInvitationByInvitationApi(invitationId, reqInvitationAcceptOrIgnore));
    }

    public List<ResPendingTeamInfo> getPendingTeamInfoByInvitationApi() {
        return requestApiExecute(RestApiLoader.getInstance().loadGetPendingTeamInfoByInvitationApi());
    }

    public ResMessageSearch searchMessagesByMessageSearchApi(long teamId, String query, int page, int perPage, long writerId, long entityId) {
        return requestApiExecute(RestApiLoader.getInstance().loadSearchMessagesByMessageSearchApi(teamId, query, page, perPage, writerId, entityId));
    }

    public ResMessageSearch searchMessagesByEntityIdByMessageSearchApi(long teamId, String query, int page, int perPage, long entityId) {
        return requestApiExecute(RestApiLoader.getInstance().loadSearchMessagesByEntityIdByMessageSearchApi(teamId, query, page, perPage, entityId));
    }

    public ResMessageSearch searchMessagesByWriterIdByMessageSearchApi(long teamId, String query, int page, int perPage, long writerId) {
        return requestApiExecute(RestApiLoader.getInstance().loadSearchMessagesByWriterIdByMessageSearchApi(teamId, query, page, perPage, writerId));
    }

    public ResMessageSearch searchMessagesByMessageSearchApi(long teamId, String query, int page, int perPage) {
        return requestApiExecute(RestApiLoader.getInstance().loadSearchMessagesByMessageSearchApi(teamId, query, page, perPage));
    }

    public ResFileDetail getFileDetailByMessagesApiAuth(long teamId, long messageId) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetFileDetailByMessagesApiAuth(teamId, messageId));
    }

    public ResCommon shareMessageByMessagesApiAuth(ReqShareMessage share, long messageId) {
        return requestApiExecute(RestApiLoader.getInstance().loadShareMessageByMessagesApiAuth(share, messageId));
    }

    public ResCommon unshareMessageByMessagesApiAuth(ReqUnshareMessage share, long messageId) {
        return requestApiExecute(RestApiLoader.getInstance().loadUnshareMessageByMessagesApiAuth(share, messageId));
    }

    public List<ResMessages.Link> getRoomUpdateMessageByMessagesApiAuth(long teamId, long roomId, long currentLinkId) {
        return requestApiExecute(RestApiLoader.getInstance().getRoomUpdateMessageByMessagesApiAuth(teamId, roomId, currentLinkId));
    }

    public ResLeftSideMenu.User updateMemberProfileByProfileApi(long memberId, ReqUpdateProfile reqUpdateProfile) {
        return requestApiExecute(RestApiLoader.getInstance().loadUpdateMemberProfileByProfileApi(memberId, reqUpdateProfile));
    }

    public ResCommon updateMemberNameByProfileApi(long memberId, ReqProfileName reqProfileName) {
        return requestApiExecute(RestApiLoader.getInstance().loadUpdateMemberNameByProfileApi(memberId, reqProfileName));
    }

    public ResLeftSideMenu.User updateMemberEmailByProfileApi(long memberId, ReqAccountEmail reqAccountEmail) {
        return requestApiExecute(RestApiLoader.getInstance().loadUpdateMemberEmailByProfileApi(memberId, reqAccountEmail));
    }

    public ResRoomInfo getRoomInfoByRoomsApi(long teamId, long roomId) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetRoomInfoByRoomsApi(teamId, roomId));
    }

    public ResCommon enableFavoriteByStarredEntityApi(ReqTeam reqTeam, long entityId) {
        return requestApiExecute(RestApiLoader.getInstance().loadEnableFavoriteByStarredEntityApi(reqTeam, entityId));
    }

    public ResCommon disableFavoriteByStarredEntityApi(long teamId, long entityId) {
        return requestApiExecute(RestApiLoader.getInstance().loadDisableFavoriteByStarredEntityApi(teamId, entityId));
    }

    public ResCommon sendStickerByStickerApi(ReqSendSticker reqSendSticker) {
        return requestApiExecute(RestApiLoader.getInstance().loadSendStickerByStickerApi(reqSendSticker));
    }

    public ResCommon sendStickerCommentByStickerApi(ReqSendSticker reqSendSticker) {
        return requestApiExecute(RestApiLoader.getInstance().loadSendStickerCommentByStickerApi(reqSendSticker));
    }

    public ResTeamDetailInfo createNewTeamByTeamApi(ReqCreateNewTeam req) {
        return requestApiExecute(RestApiLoader.getInstance().loadCreateNewTeamByTeamApi(req));
    }

    public ResLeftSideMenu.User getMemberProfileByTeamApi(long teamId, long memberId) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetMemberProfileByTeamApi(teamId, memberId));
    }

    public List<ResInvitationMembers> inviteToTeamByTeamApi(long teamId, ReqInvitationMembers invitationMembers) {
        return requestApiExecute(RestApiLoader.getInstance().loadInviteToTeamByTeamApi(teamId, invitationMembers));
    }

    public ResTeamDetailInfo.InviteTeam getTeamInfoByTeamApi(long teamId) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetTeamInfoByTeamApi(teamId));
    }

    public ResAnnouncement getAnnouncement(long teamId, long topicId) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetAnnouncement(teamId, topicId));
    }

    public ResCommon createAnnouncement(long teamId, long topicId, ReqCreateAnnouncement reqCreateAnnouncement) {
        return requestApiExecute(RestApiLoader.getInstance().loadCreateAnnouncement(teamId, topicId, reqCreateAnnouncement));
    }

    public ResCommon updateAnnouncementStatus(long teamId, long memberId, ReqUpdateAnnouncementStatus reqUpdateAnnouncementStatus) {
        return requestApiExecute(RestApiLoader.getInstance().loadUpdateAnnouncementStatus(teamId, memberId, reqUpdateAnnouncementStatus));
    }

    public ResCommon deleteAnnouncement(long teamId, long topicId) {
        return requestApiExecute(RestApiLoader.getInstance().loadDeleteAnnouncement(teamId, topicId));
    }

    public ResMessages.OriginalMessage getMessage(long teamId, long messageId) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetMessage(teamId, messageId));
    }

    public ResCommon updateTopicPushSubscribe(long teamId, long topicId, ReqUpdateTopicPushSubscribe reqUpdateTopicPushSubscribe) {
        return requestApiExecute(RestApiLoader.getInstance().loadUpdateTopicPushSubscribe(teamId, topicId, reqUpdateTopicPushSubscribe));
    }

    public ResCommon deleteStickerCommentByStickerApi(long commentId, long teamId) {
        return requestApiExecute(RestApiLoader.getInstance().loadDeleteStickerCommentByStickerApi(commentId, teamId));
    }

    public ResCommon deleteStickerByStickerApi(long messageId, long teamId) {
        return requestApiExecute(RestApiLoader.getInstance().loadDeleteStickerByStickerApi(messageId, teamId));
    }

    public ResCommon deleteFileByFileApi(long teamId, long fileId) {
        return requestApiExecute(RestApiLoader.getInstance().loaderDeleteFileByFileApi(teamId, fileId));
    }

    public List<ResMessages.FileMessage> searchInitImageFileByFileApi(long teamId, long roomId, long messageId, int count) {
        return requestApiExecute(RestApiLoader.getInstance().loaderSearchInitImageFileByFileApi(teamId, roomId, messageId, count));
    }

    public List<ResMessages.FileMessage> searchOldImageFileByFileApi(long teamId, long roomId, long messageId, int count) {
        return requestApiExecute(RestApiLoader.getInstance().loaderSearchOldImageFileByFileApi(teamId, roomId, messageId, count));
    }

    public List<ResMessages.FileMessage> searchNewImageFileByFileApi(long teamId, long roomId, long messageId, int count) {
        return requestApiExecute(RestApiLoader.getInstance().loaderSearchNewImageFileByFileApi(teamId, roomId, messageId, count));
    }

    public ResStarMentioned getMentionedMessagesByTeamApi(long teamId, long messageId, int count) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetMentionedMessagesByTeamApi(teamId, messageId, count));
    }

    public StarMentionedMessageObject registStarredMessageByTeamApi(long teamId, long messageId) {
        return requestApiExecute(RestApiLoader.getInstance().loadRegistStarredMessageByTeamApi(teamId, messageId));
    }

    public ResCommon unregistStarredMessageByTeamApi(long teamId, long messageId) {
        return requestApiExecute(RestApiLoader.getInstance().loadUnregistStarredMessageByTeamApi(teamId, messageId));
    }

    public ResStarMentioned getStarredMessagesByTeamApi(long teamId, long starredId, int count, String type) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetStarredMessagesByTeamApi(teamId, starredId, count, type));
    }

    public ResCommon updatePlatformStatus(ReqUpdatePlatformStatus reqUpdatePlatformStatus) {
        return requestApiExecute(RestApiLoader.getInstance().loadUpdatePlatformStatus(reqUpdatePlatformStatus));
    }

    public ResCreateFolder createFolderByTeamApi(long teamId, ReqCreateFolder reqCreateFolder) {
        return requestApiExecute(RestApiLoader.getInstance().loadCreateFolderByTeamApi(teamId, reqCreateFolder));
    }

    public ResCommon deleteFolderByTeamApi(long teamId, long folderId) {
        return requestApiExecute(RestApiLoader.getInstance().loadDeleteFolderByTeamApi(teamId, folderId));
    }

    public ResUpdateFolder updateFolderByTeamApi(long teamId, long folderId, ReqUpdateFolder reqUpdateFolder) {
        return requestApiExecute(RestApiLoader.getInstance().loadUpdateFolderByTeamApi(teamId, folderId, reqUpdateFolder));
    }

    public List<ResFolder> getFoldersByTeamApi(long teamId) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetFoldersByTeamApi(teamId));
    }

    public List<ResFolderItem> getFolderItemsByTeamApi(long teamId) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetFolderItemsByTeamApi(teamId));
    }

    public ResRegistFolderItem registFolderItemByTeamApi(long teamId, long folderId, ReqRegistFolderItem reqRegistFolderItem) {
        return requestApiExecute(RestApiLoader.getInstance().loadRegistFolderItemByTeamApi(teamId, folderId, reqRegistFolderItem));
    }

    public ResCommon deleteFolderItemByTeamApi(long teamId, long folderId, long itemId) {
        return requestApiExecute(RestApiLoader.getInstance().loadDeleteFolderItemByTeamApi(teamId, folderId, itemId));
    }

    public ResCommon kickUserFromTopic(long teamId, long topicId, ReqMember member) {
        return requestApiExecute(RestApiLoader.getInstance().loadKickUserFromTopic(teamId, topicId, member));
    }

    public ResCommon assignToTopicOwner(long teamId, long topicId, ReqOwner owner) {
        return requestApiExecute(RestApiLoader.getInstance().loadAssignToTopicOwner(teamId, topicId, owner));
    }

    public ResMessages.FileMessage enableFileExternalLink(long teamId, long fileId) {
        return requestApiExecute(RestApiLoader.getInstance().loadEnableFileExternalLink(teamId, fileId));
    }

    public ResMessages.FileMessage disableFileExternalLink(long teamId, long fileId) {
        return requestApiExecute(RestApiLoader.getInstance().loadDisableFileExternalLink(teamId, fileId));
    }

    public ResEventHistory getEventHistory(long ts, long memberId, String eventType, Integer size) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetEventHistory(ts, memberId, eventType, size));
    }

    public ResValidation validDomain(String domain) {
        return requestApiExecute(RestApiLoader.getInstance().loadValidDomain(domain));
    }

    public ResAvatarsInfo getAvartarsInfo() {
        return requestApiExecute(RestApiLoader.getInstance().loadGetAvartarsInfo());
    }
}