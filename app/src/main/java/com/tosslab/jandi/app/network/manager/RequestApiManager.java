package com.tosslab.jandi.app.network.manager;

import com.tosslab.jandi.app.network.client.account.devices.IAccountDeviceApiAuth;
import com.tosslab.jandi.app.network.client.account.emails.IAccountEmailsApiAuth;
import com.tosslab.jandi.app.network.client.account.password.IAccountPasswordApiAuth;
import com.tosslab.jandi.app.network.client.account.password.IAccountPasswordApiSimple;
import com.tosslab.jandi.app.network.client.chat.IChatApiAuth;
import com.tosslab.jandi.app.network.client.direct.message.IDirectMessageApiAuth;
import com.tosslab.jandi.app.network.client.file.IFileApiAuth;
import com.tosslab.jandi.app.network.client.invitation.IInvitationApiAuth;
import com.tosslab.jandi.app.network.client.main.IMainRestApiAuth;
import com.tosslab.jandi.app.network.client.main.IMainRestApiSimple;
import com.tosslab.jandi.app.network.client.messages.IMessagesApiAuth;
import com.tosslab.jandi.app.network.client.messages.comments.ICommentsApiAuth;
import com.tosslab.jandi.app.network.client.messages.search.IMessageSearchApiAuth;
import com.tosslab.jandi.app.network.client.privatetopic.IGroupApiAuth;
import com.tosslab.jandi.app.network.client.privatetopic.messages.IGroupMessageApiAuth;
import com.tosslab.jandi.app.network.client.profile.IProfileApiAuth;
import com.tosslab.jandi.app.network.client.publictopic.IChannelApiAuth;
import com.tosslab.jandi.app.network.client.publictopic.messages.IChannelMessageApiAuth;
import com.tosslab.jandi.app.network.client.rooms.IRoomsApiAuth;
import com.tosslab.jandi.app.network.client.settings.IAccountProfileApiAuth;
import com.tosslab.jandi.app.network.client.settings.IStarredEntityApiAuth;
import com.tosslab.jandi.app.network.client.sticker.IStickerApiAuth;
import com.tosslab.jandi.app.network.client.teams.ITeamApiAuth;
import com.tosslab.jandi.app.network.manager.apiexecutor.IExecutor;
import com.tosslab.jandi.app.network.manager.apiexecutor.PoolableRequestApiExecutor;
import com.tosslab.jandi.app.network.manager.apiloader.RestApiLoader;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqAccountActivate;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqAccountVerification;
import com.tosslab.jandi.app.network.models.ReqChangePassword;
import com.tosslab.jandi.app.network.models.ReqConfirmEmail;
import com.tosslab.jandi.app.network.models.ReqCreateAnnouncement;
import com.tosslab.jandi.app.network.models.ReqCreateNewTeam;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqDeleteTopic;
import com.tosslab.jandi.app.network.models.ReqDeviceToken;
import com.tosslab.jandi.app.network.models.ReqInvitationAcceptOrIgnore;
import com.tosslab.jandi.app.network.models.ReqInvitationMembers;
import com.tosslab.jandi.app.network.models.ReqInviteTopicUsers;
import com.tosslab.jandi.app.network.models.ReqModifyComment;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqNotificationRegister;
import com.tosslab.jandi.app.network.models.ReqNotificationTarget;
import com.tosslab.jandi.app.network.models.ReqProfileName;
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
import com.tosslab.jandi.app.network.models.ReqUpdatePrimaryEmailInfo;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountActivate;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResAnnouncement;
import com.tosslab.jandi.app.network.models.ResChat;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResInvitationMembers;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessageSearch;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResMyTeam;
import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;
import com.tosslab.jandi.app.network.models.ResRoomInfo;
import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;
import com.tosslab.jandi.app.network.models.sticker.ReqSendSticker;

import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 6. 20..
 */

public class RequestApiManager implements IAccountDeviceApiAuth, IAccountEmailsApiAuth, IAccountPasswordApiAuth,
        IChatApiAuth, IDirectMessageApiAuth, IInvitationApiAuth, IMainRestApiAuth, ICommentsApiAuth, IMessageSearchApiAuth,
        IMessagesApiAuth, IGroupMessageApiAuth, IGroupApiAuth, IProfileApiAuth, IChannelMessageApiAuth, IChannelApiAuth,
        IRoomsApiAuth, IAccountProfileApiAuth, IStarredEntityApiAuth, IStickerApiAuth, ITeamApiAuth, IAccountPasswordApiSimple,
        IMainRestApiSimple, IFileApiAuth {

    private static final RequestApiManager requestApiManager = new RequestApiManager();

    private RequestApiManager() {
    }

    public static final RequestApiManager getInstance() {
        return requestApiManager;
    }

    public <RESULT> RESULT requestApiExecute(IExecutor<RESULT> executor) {
        PoolableRequestApiExecutor requestApiexecutor = PoolableRequestApiExecutor.obtain();

        try {
            return requestApiexecutor.execute(executor);
        } catch (RetrofitError e) {
            throw e;
        } finally {
            requestApiexecutor.recycle();
        }

    }

    @Override
    public ResAccountInfo registerNotificationTokenByAccountDeviceApi(ReqNotificationRegister reqNotificationRegister) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadRegisterNotificationTokenByAccountDeviceApi(reqNotificationRegister));
    }

    @Override
    public ResAccountInfo deleteNotificationTokenByAccountDeviceApi(ReqDeviceToken reqDeviceToken) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadDeleteNotificationTokenByAccountDeviceApi(reqDeviceToken));
    }

    @Override
    public ResAccountInfo subscribeStateNotificationByAccountDeviceApi(ReqSubscibeToken reqDeviceToken) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadSubscribeStateNotificationByAccountDeviceApi(reqDeviceToken));
    }

    @Override
    public ResCommon getNotificationBadgeByAccountDeviceApi(ReqNotificationTarget reqNotificationTarget) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetNotificationBadgeByAccountDeviceApi(reqNotificationTarget));
    }

    @Override
    public ResAccountInfo requestAddEmailByAccountEmailApi(ReqAccountEmail reqAccountEmail) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadRequestAddEmailByAccountEmailsApi(reqAccountEmail));
    }

    @Override
    public ResAccountInfo confirmEmailByAccountEmailApi(ReqConfirmEmail reqConfirmEmail) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadConfirmEmailByAccountEmailsApi(reqConfirmEmail));
    }

    @Override
    public ResAccountInfo deleteEmailByAccountEmailApi(ReqAccountEmail reqConfirmEmail) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadExecutorDeleteEmailByAccountEmailsApi(reqConfirmEmail));
    }

    @Override
    public ResCommon resetPasswordByAccountPasswordApi(ReqAccountEmail reqAccountEmail) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadResetPasswordByAccountPasswordApi(reqAccountEmail));
    }

    @Override
    public ResCommon changePasswordByAccountPasswordApi(ReqChangePassword reqConfirmEmail) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadChangePasswordByAccountEmailsApi(reqConfirmEmail));
    }

    @Override
    public ResConfig getConfigByMainRest() throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetConfigByMainRestApi());
    }

    @Override
    public ResMyTeam getTeamIdByMainRest(String userEmail) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetTeamIdByMainRestApi(userEmail));
    }

    @Override
    public ResAccessToken getAccessTokenByMainRest(ReqAccessToken login) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetAccessTokenByMainRestApi(login));
    }

    @Override
    public ResCommon signUpAccountByMainRest(ReqSignUpInfo signUpInfo) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadSignUpAccountByMainRestApi(signUpInfo));
    }

    @Override
    public ResAccountActivate activateAccountByMainRest(ReqAccountActivate reqAccountActivate) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadActivateAccountByMainRestApi(reqAccountActivate));
    }

    @Override
    public ResCommon accountVerificationByMainRest(ReqAccountVerification reqAccountVerification) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadAccountVerificationByMainRestApi(reqAccountVerification));
    }

    @Override
    public ResAccountInfo getAccountInfoByMainRest() throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetAccountInfoByMainRestApi());
    }

    @Override
    public ResAccountInfo updatePrimaryEmailByMainRest(ReqUpdatePrimaryEmailInfo updatePrimaryEmailInfo) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadUpdatePrimaryEmailByMainRestApi(updatePrimaryEmailInfo));
    }

    @Override
    public ResLeftSideMenu getInfosForSideMenuByMainRest(int teamId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetInfosForSideMenuByMainRestApi(teamId));
    }

    @Override
    public ResCommon setMarkerByMainRest(int entityId, ReqSetMarker reqSetMarker) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadSetMarkerByMainRestApi(entityId, reqSetMarker));
    }

    @Override
    public ResSearchFile searchFileByMainRest(ReqSearchFile reqSearchFile) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadSearchFileByMainRestApi(reqSearchFile));
    }

    @Override
    public ResAccountInfo changeNameByAccountProfileApi(ReqProfileName reqProfileName) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadChangeNameByAccountProfileApi(reqProfileName));
    }

    @Override
    public ResAccountInfo changePrimaryEmailByAccountProfileApi(ReqAccountEmail reqAccountEmail) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadChangePrimaryEmailByAccountProfileApi(reqAccountEmail));
    }

    @Override
    public ResCommon createChannelByChannelApi(ReqCreateTopic channel) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadCreateChannelByChannelApi(channel));
    }

    @Override
    public ResCommon modifyPublicTopicNameByChannelApi(ReqCreateTopic channel, int channelId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadModifyPublicTopicNameByChannelApi(channel, channelId));
    }

    @Override
    public ResCommon deleteTopicByChannelApi(int channelId, ReqDeleteTopic reqDeleteTopic) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadDeleteTopicByChannelApi(channelId, reqDeleteTopic));
    }

    @Override
    public ResCommon joinTopicByChannelApi(int channelId, ReqDeleteTopic reqDeleteTopic) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadJoinTopicByChannelApi(channelId, reqDeleteTopic));
    }

    @Override
    public ResCommon leaveTopicByChannelApi(int channelId, ReqDeleteTopic reqDeleteTopic) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadLeaveTopicByChannelApi(channelId, reqDeleteTopic));
    }

    @Override
    public ResCommon invitePublicTopicByChannelApi(int channelId, ReqInviteTopicUsers reqInviteTopicUsers) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadInvitePublicTopicByChannelApi(channelId, reqInviteTopicUsers));
    }

    @Override
    public ResMessages getPublicTopicMessagesByChannelMessageApi(int teamId, int channelId, int fromId, int count) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetPublicTopicMessagesByChannelMessageApi(teamId, channelId, fromId, count));
    }

    @Override
    public ResMessages getPublicTopicMessagesByChannelMessageApi(int teamId, int channelId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetPublicTopicMessagesByChannelMessageApi(teamId, channelId));
    }

    @Override
    public ResUpdateMessages getPublicTopicUpdatedMessagesByChannelMessageApi(int teamId, int channelId, int currentLinkId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetPublicTopicUpdatedMessagesByChannelMessageApi(teamId, channelId, currentLinkId));
    }

    @Override
    public ResMessages getPublicTopicUpdatedMessagesForMarkerByChannelMessageApi(int teamId, int channelId, int currentLinkId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetPublicTopicUpdatedMessagesForMarkerByChannelMessageApi(teamId, channelId, currentLinkId));
    }

    @Override
    public ResMessages getPublicTopicMarkerMessagesByChannelMessageApi(int teamId, int channelId, int currentLinkId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetPublicTopicMarkerMessagesByChannelMessageApi(teamId, channelId, currentLinkId));
    }

    @Override
    public ResCommon sendPublicTopicMessageByChannelMessageApi(int channelId, int teamId, ReqSendMessageV3 reqSendMessageV3) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadSendPublicTopicMessageByChannelMessageApi(channelId, teamId, reqSendMessageV3));
    }

    @Override
    public ResCommon modifyPublicTopicMessageByChannelMessageApi(ReqModifyMessage message, int channelId, int messageId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadModifyPublicTopicMessageByChannelMessageApi(message, channelId, messageId));
    }

    @Override
    public ResCommon deletePublicTopicMessageByChannelMessageApi(int teamId, int channelId, int messageId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadDeletePublicTopicMessageByChannelMessageApi(teamId, channelId, messageId));
    }

    @Override
    public List<ResChat> getChatListByChatApi(int memberId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetChatListByChatApi(memberId));
    }

    @Override
    public ResCommon deleteChatByChatApi(int memberId, int entityId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadDeleteChatByChatApi(memberId, entityId));
    }

    @Override
    public ResCommon sendMessageCommentByCommentsApi(int messageId, int teamId, ReqSendComment reqSendComment) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadSendMessageCommentByCommentsApi(messageId, teamId, reqSendComment));
    }

    @Override
    public ResCommon modifyMessageCommentByCommentsApi(ReqModifyComment comment, int messageId, int commentId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadModifyMessageCommentByCommentsApi(comment, messageId, commentId));
    }

    @Override
    public ResCommon deleteMessageCommentByCommentsApi(int teamId, int messageId, int commentId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadDeleteMessageCommentByCommentsApi(teamId, messageId, commentId));
    }

    @Override
    public ResMessages getDirectMessagesByDirectMessageApi(int teamId, int userId, int fromId, int count) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetDirectMessagesByDirectMessageApi(teamId, userId, fromId, count));
    }

    @Override
    public ResMessages getDirectMessagesByDirectMessageApi(int teamId, int userId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetDirectMessagesByDirectMessageApi(teamId, userId));
    }

    @Override
    public ResUpdateMessages getDirectMessagesUpdatedByDirectMessageApi(int teamId, int userId, int timeAfter) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetDirectMessagesUpdatedByDirectMessageApi(teamId, userId, timeAfter));
    }

    @Override
    public ResMessages getDirectMessagesUpdatedForMarkerByDirectMessageApi(int teamId, int userId, int currentLinkId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetDirectMessagesUpdatedForMarkerByDirectMessageApi(teamId, userId, currentLinkId));
    }

    @Override
    public ResMessages getDirectMarkerMessagesByDirectMessageApi(int teamId, int userId, int currentLinkId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetDirectMarkerMessagesByDirectMessageApi(teamId, userId, currentLinkId));
    }

    @Override
    public ResCommon sendDirectMessageByDirectMessageApi(int userId, int teamId,
                                                         ReqSendMessageV3 reqSendMessageV3) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadSendDirectMessageByDirectMessageApi(userId, teamId, reqSendMessageV3));
    }

    @Override
    public ResCommon modifyDirectMessageByDirectMessageApi(ReqModifyMessage message, int userId, int messageId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadModifyDirectMessageByDirectMessageApi(message, userId, messageId));
    }

    @Override
    public ResCommon deleteDirectMessageByDirectMessageApi(int teamId, int userId, int messageId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadDeleteDirectMessageByDirectMessageApi(teamId, userId, messageId));
    }

    @Override
    public ResCommon createPrivateGroupByGroupApi(ReqCreateTopic group) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadCreatePrivateGroupByGroupApi(group));
    }

    @Override
    public ResCommon modifyGroupByGroupApi(ReqCreateTopic channel, int groupId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadModifyGroupByGroupApi(channel, groupId));
    }

    @Override
    public ResCommon deleteGroupByGroupApi(int teamId, int groupId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadDeleteGroupByGroupApi(teamId, groupId));
    }

    @Override
    public ResCommon leaveGroupByGroupApi(int groupId, ReqTeam team) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadLeaveGroupByGroupApi(groupId, team));
    }

    @Override
    public ResCommon inviteGroupByGroupApi(int groupId, ReqInviteTopicUsers inviteUsers) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadInviteGroupByGroupApi(groupId, inviteUsers));
    }

    @Override
    public ResMessages getGroupMessagesByGroupMessageApi(int teamId, int groupId, int fromId, int count) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetGroupMessagesByGroupMessageApi(teamId, groupId, fromId, count));
    }

    @Override
    public ResMessages getGroupMessagesByGroupMessageApi(int teamId, int groupId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetGroupMessagesByGroupMessageApi(teamId, groupId));
    }

    @Override
    public ResUpdateMessages getGroupMessagesUpdatedByGroupMessageApi(int teamId, int groupId, int lastLinkId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetGroupMessagesUpdatedByGroupMessageApi(teamId, groupId, lastLinkId));
    }

    @Override
    public ResMessages getGroupMessagesUpdatedForMarkerByGroupMessageApi(int teamId, int groupId,
                                                                         int currentLinkId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance()
                .loadGetGroupMessagesUpdatedForMarkerByGroupMessageApi(teamId, groupId, currentLinkId));
    }

    @Override
    public ResMessages getGroupMarkerMessagesByGroupMessageApi(int teamId, int groupId, int currentLinkId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetGroupMarkerMessagesByGroupMessageApi(teamId, groupId, currentLinkId));
    }

    @Override
    public ResCommon sendGroupMessageByGroupMessageApi(int privateGroupId, int teamId, ReqSendMessageV3 reqSendMessageV3) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadSendGroupMessageByGroupMessageApi(privateGroupId, teamId, reqSendMessageV3));
    }

    @Override
    public ResCommon modifyPrivateGroupMessageByGroupMessageApi(ReqModifyMessage message, int groupId, int messageId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadModifyPrivateGroupMessageByGroupMessageApi(message, groupId, messageId));
    }

    @Override
    public ResCommon deletePrivateGroupMessageByGroupMessageApi(int teamId, int groupId, int messageId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadDeletePrivateGroupMessageByGroupMessageApi(teamId, groupId, messageId));
    }

    @Override
    public ResTeamDetailInfo acceptOrDeclineInvitationByInvitationApi(String invitationId, ReqInvitationAcceptOrIgnore reqInvitationAcceptOrIgnore) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadAcceptOrDeclineInvitationByInvitationApi(invitationId, reqInvitationAcceptOrIgnore));
    }

    @Override
    public List<ResPendingTeamInfo> getPendingTeamInfoByInvitationApi() throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetPendingTeamInfoByInvitationApi());
    }

    @Override
    public ResMessageSearch searchMessagesByMessageSearchApi(int teamId, String query, int page, int perPage, int writerId, int entityId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadSearchMessagesByMessageSearchApi(teamId, query, page, perPage, writerId, entityId));
    }

    @Override
    public ResMessageSearch searchMessagesByEntityIdByMessageSearchApi(int teamId, String query, int page, int perPage, int entityId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadSearchMessagesByEntityIdByMessageSearchApi(teamId, query, page, perPage, entityId));
    }

    @Override
    public ResMessageSearch searchMessagesByWriterIdByMessageSearchApi(int teamId, String query, int page, int perPage, int writerId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadSearchMessagesByWriterIdByMessageSearchApi(teamId, query, page, perPage, writerId));
    }

    @Override
    public ResMessageSearch searchMessagesByMessageSearchApi(int teamId, String query, int page, int perPage) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadSearchMessagesByMessageSearchApi(teamId, query, page, perPage));
    }

    @Override
    public ResFileDetail getFileDetailByMessagesApiAuth(int teamId, int messageId) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetFileDetailByMessagesApiAuth(teamId, messageId));
    }

    @Override
    public ResCommon shareMessageByMessagesApiAuth(ReqShareMessage share, int messageId) {
        return requestApiExecute(RestApiLoader.getInstance().loadShareMessageByMessagesApiAuth(share, messageId));
    }

    @Override
    public ResCommon unshareMessageByMessagesApiAuth(ReqUnshareMessage share, int messageId) {
        return requestApiExecute(RestApiLoader.getInstance().loadUnshareMessageByMessagesApiAuth(share, messageId));
    }

    @Override
    public ResLeftSideMenu.User updateMemberProfileByProfileApi(int memberId, ReqUpdateProfile reqUpdateProfile) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadUpdateMemberProfileByProfileApi(memberId, reqUpdateProfile));
    }

    @Override
    public ResCommon updateMemberNameByProfileApi(int memberId, ReqProfileName reqProfileName) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadUpdateMemberNameByProfileApi(memberId, reqProfileName));
    }

    @Override
    public ResLeftSideMenu.User updateMemberEmailByProfileApi(int memberId, ReqAccountEmail reqAccountEmail) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadUpdateMemberEmailByProfileApi(memberId, reqAccountEmail));
    }

    @Override
    public ResRoomInfo getRoomInfoByRoomsApi(int teamId, int roomId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetRoomInfoByRoomsApi(teamId, roomId));
    }

    @Override
    public ResCommon enableFavoriteByStarredEntityApi(ReqTeam reqTeam, int entityId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadEnableFavoriteByStarredEntityApi(reqTeam, entityId));
    }

    @Override
    public ResCommon disableFavoriteByStarredEntityApi(int teamId, int entityId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadDisableFavoriteByStarredEntityApi(teamId, entityId));
    }

    @Override
    public ResCommon sendStickerByStickerApi(ReqSendSticker reqSendSticker) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadSendStickerByStickerApi(reqSendSticker));
    }

    @Override
    public ResCommon sendStickerCommentByStickerApi(ReqSendSticker reqSendSticker) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadSendStickerCommentByStickerApi(reqSendSticker));
    }

    @Override
    public ResTeamDetailInfo createNewTeamByTeamApi(ReqCreateNewTeam req) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadCreateNewTeamByTeamApi(req));
    }

    @Override
    public ResLeftSideMenu.User getMemberProfileByTeamApi(int teamId, int memberId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetMemberProfileByTeamApi(teamId, memberId));
    }

    @Override
    public List<ResInvitationMembers> inviteToTeamByTeamApi(int teamId, ReqInvitationMembers invitationMembers) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadInviteToTeamByTeamApi(teamId, invitationMembers));
    }

    @Override
    public ResTeamDetailInfo.InviteTeam getTeamInfoByTeamApi(int teamId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetTeamInfoByTeamApi(teamId));
    }

    @Override
    public ResAnnouncement getAnnouncement(int teamId, int topicId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetAnnouncement(teamId, topicId));
    }

    @Override
    public ResCommon createAnnouncement(int teamId, int topicId, ReqCreateAnnouncement reqCreateAnnouncement) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadCreateAnnouncement(teamId, topicId, reqCreateAnnouncement));
    }

    @Override
    public ResCommon updateAnnouncementStatus(int teamId, int memberId, ReqUpdateAnnouncementStatus reqUpdateAnnouncementStatus) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadUpdateAnnouncementStatus(teamId, memberId, reqUpdateAnnouncementStatus));
    }

    @Override
    public ResCommon deleteAnnouncement(int teamId, int topicId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadDeleteAnnouncement(teamId, topicId));
    }

    @Override
    public ResMessages.OriginalMessage getMessage(int teamId, int messageId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetMessage(teamId, messageId));
    }

    @Override
    public ResCommon deleteStickerCommentByStickerApi(int commentId, int teamId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadDeleteStickerCommentByStickerApi(commentId, teamId));
    }

    @Override
    public ResCommon deleteStickerByStickerApi(int messageId, int teamId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadDeleteStickerByStickerApi(messageId, teamId));
    }

    @Override
    public ResCommon deleteFileByFileApi(int teamId, int fileId) {
        return requestApiExecute(RestApiLoader.getInstance().loaderDeleteFileByFileApi(teamId, fileId));
    }

    @Override
    public List<ResMessages.FileMessage> searchInitImageFileByFileApi(int teamId, int roomId, int messageId, int count) {
        return requestApiExecute(RestApiLoader.getInstance().loaderSearchInitImageFileByFileApi(teamId, roomId, messageId, count));
    }

    @Override
    public List<ResMessages.FileMessage> searchOldImageFileByFileApi(int teamId, int roomId, int messageId, int count) {
        return requestApiExecute(RestApiLoader.getInstance().loaderSearchOldImageFileByFileApi(teamId, roomId, messageId, count));
    }

    @Override
    public List<ResMessages.FileMessage> searchNewImageFileByFileApi(int teamId, int roomId, int messageId, int count) {
        return requestApiExecute(RestApiLoader.getInstance().loaderSearchNewImageFileByFileApi(teamId, roomId, messageId, count));
    }
}