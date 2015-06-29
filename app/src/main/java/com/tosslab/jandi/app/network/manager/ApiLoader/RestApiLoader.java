package com.tosslab.jandi.app.network.manager.ApiLoader;

import com.tosslab.jandi.app.network.client.account.devices.IAccountDeviceApiLoader;
import com.tosslab.jandi.app.network.client.account.emails.IAccountEmailsApiLoader;
import com.tosslab.jandi.app.network.client.account.password.IAccountPasswordApiLoader;
import com.tosslab.jandi.app.network.client.chat.IChatApiLoader;
import com.tosslab.jandi.app.network.client.direct.message.IDirectMessageApiLoader;
import com.tosslab.jandi.app.network.client.invitation.IInvitationApiLoader;
import com.tosslab.jandi.app.network.client.main.IMainRestApiLoader;
import com.tosslab.jandi.app.network.client.messages.IMessagesApiLoader;
import com.tosslab.jandi.app.network.client.messages.comments.ICommentsApiLoader;
import com.tosslab.jandi.app.network.client.messages.search.IMessageSearchApiLoader;
import com.tosslab.jandi.app.network.client.privatetopic.IGroupApiLoader;
import com.tosslab.jandi.app.network.client.privatetopic.messages.IGroupMessageApiLoader;
import com.tosslab.jandi.app.network.client.profile.IProfileApiLoader;
import com.tosslab.jandi.app.network.client.publictopic.IChannelApiLoader;
import com.tosslab.jandi.app.network.client.publictopic.messages.IChannelMessageApiLoader;
import com.tosslab.jandi.app.network.client.rooms.IRoomsApiLoader;
import com.tosslab.jandi.app.network.client.settings.IAccountProfileApiLoader;
import com.tosslab.jandi.app.network.client.settings.IStarredEntityApiLoader;
import com.tosslab.jandi.app.network.client.sticker.IStickerApiLoader;
import com.tosslab.jandi.app.network.client.teams.ITeamApiLoader;
import com.tosslab.jandi.app.network.manager.ApiExecutor.IExecutor;
import com.tosslab.jandi.app.network.manager.RestApiClient.JacksonConvertedAuthRestApiClient;
import com.tosslab.jandi.app.network.manager.RestApiClient.JacksonConvertedSimpleRestApiClient;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqAccountActivate;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqAccountVerification;
import com.tosslab.jandi.app.network.models.ReqChangePassword;
import com.tosslab.jandi.app.network.models.ReqConfirmEmail;
import com.tosslab.jandi.app.network.models.ReqCreateNewTeam;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqDeleteTopic;
import com.tosslab.jandi.app.network.models.ReqDeviceToken;
import com.tosslab.jandi.app.network.models.ReqInvitationAcceptOrIgnore;
import com.tosslab.jandi.app.network.models.ReqInvitationMembers;
import com.tosslab.jandi.app.network.models.ReqInviteTopicUsers;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqNotificationRegister;
import com.tosslab.jandi.app.network.models.ReqNotificationTarget;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ReqSendComment;
import com.tosslab.jandi.app.network.models.ReqSendMessage;
import com.tosslab.jandi.app.network.models.ReqSetMarker;
import com.tosslab.jandi.app.network.models.ReqShareMessage;
import com.tosslab.jandi.app.network.models.ReqSignUpInfo;
import com.tosslab.jandi.app.network.models.ReqSubscibeToken;
import com.tosslab.jandi.app.network.models.ReqTeam;
import com.tosslab.jandi.app.network.models.ReqUnshareMessage;
import com.tosslab.jandi.app.network.models.ReqUpdatePrimaryEmailInfo;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.sticker.ReqSendSticker;

/**
 * Created by tee on 15. 6. 20..
 */
public class RestApiLoader implements IAccountDeviceApiLoader, IAccountEmailsApiLoader, IAccountPasswordApiLoader, IChatApiLoader,
        IDirectMessageApiLoader, IInvitationApiLoader, IMainRestApiLoader, ICommentsApiLoader, IMessageSearchApiLoader,
        IMessagesApiLoader, IGroupMessageApiLoader, IGroupApiLoader, IProfileApiLoader, IChannelMessageApiLoader, IChannelApiLoader,
        IRoomsApiLoader, IAccountProfileApiLoader, IStarredEntityApiLoader, IStickerApiLoader, ITeamApiLoader {

    JacksonConvertedAuthRestApiClient authRestApiClient = new JacksonConvertedAuthRestApiClient();

    JacksonConvertedSimpleRestApiClient SimpleRestApiClient = new JacksonConvertedSimpleRestApiClient();

    private RestApiLoader() {
    }

    public static RestApiLoader getInstance() {
        return new RestApiLoader();
    }

    @Override
    public IExecutor loadRegisterNotificationTokenByAccountDeviceApi(ReqNotificationRegister reqNotificationRegister) {
        return () -> {
            return authRestApiClient.registerNotificationTokenByAccountDeviceApi(reqNotificationRegister);
        };
    }

    @Override
    public IExecutor loadDeleteNotificationTokenByAccountDeviceApi(ReqDeviceToken reqDeviceToken) {
        return () -> authRestApiClient.deleteNotificationTokenByAccountDeviceApi(reqDeviceToken);
    }

    @Override
    public IExecutor loadSubscribeStateNotificationByAccountDeviceApi(ReqSubscibeToken reqDeviceToken) {
        return () -> authRestApiClient.subscribeStateNotificationByAccountDeviceApi(reqDeviceToken);
    }

    @Override
    public IExecutor loadGetNotificationBadgeByAccountDeviceApi(ReqNotificationTarget reqNotificationTarget) {
        return () -> authRestApiClient.getNotificationBadgeByAccountDeviceApi(reqNotificationTarget);
    }

    @Override
    public IExecutor loadRequestAddEmailByAccountEmailsApi(ReqAccountEmail reqAccountEmail) {
        return () -> authRestApiClient.requestAddEmailByAccountEmailApi(reqAccountEmail);
    }

    @Override
    public IExecutor loadConfirmEmailByAccountEmailsApi(ReqConfirmEmail reqConfirmEmail) {
        return () -> authRestApiClient.confirmEmailByAccountEmailApi(reqConfirmEmail);
    }

    @Override
    public IExecutor loadExecutorDeleteEmailByAccountEmailsApi(ReqAccountEmail reqConfirmEmail) {
        return () -> authRestApiClient.deleteEmailByAccountEmailApi(reqConfirmEmail);
    }

    @Override
    public IExecutor loadChangePasswordByAccountEmailsApi(ReqChangePassword reqConfirmEmail) {
        return () -> authRestApiClient.changePasswordByAccountPasswordApi(reqConfirmEmail);
    }

    @Override
    public IExecutor loadGetAccountInfoByMainRestApi() {
        return () -> authRestApiClient.getAccountInfoByMainRest();
    }

    @Override
    public IExecutor loadUpdatePrimaryEmailByMainRestApi(ReqUpdatePrimaryEmailInfo updatePrimaryEmailInfo) {
        return () -> authRestApiClient.updatePrimaryEmailByMainRest(updatePrimaryEmailInfo);
    }

    @Override
    public IExecutor loadGetInfosForSideMenuByMainRestApi(int teamId) {
        return () -> authRestApiClient.getInfosForSideMenuByMainRest(teamId);
    }

    @Override
    public IExecutor loadSetMarkerByMainRestApi(int entityId, ReqSetMarker reqSetMarker) {
        return () -> authRestApiClient.setMarkerByMainRest(entityId, reqSetMarker);
    }

    @Override
    public IExecutor loadSearchFileByMainRestApi(ReqSearchFile reqSearchFile) {
        return () -> authRestApiClient.searchFileByMainRest(reqSearchFile);
    }

    @Override
    public IExecutor loadChangePasswordByAccountPasswordApi(ReqChangePassword reqConfirmEmail) {
        return () -> authRestApiClient.changePasswordByAccountPasswordApi(reqConfirmEmail);
    }

    @Override
    public IExecutor loadChangeNameByAccountProfileApi(ReqProfileName reqProfileName) {
        return () -> authRestApiClient.changeNameByAccountProfileApi(reqProfileName);
    }

    @Override
    public IExecutor loadChangePrimaryEmailByAccountProfileApi(ReqAccountEmail reqAccountEmail) {
        return () -> authRestApiClient.changePrimaryEmailByAccountProfileApi(reqAccountEmail);
    }

    @Override
    public IExecutor loadCreateChannelByChannelApi(ReqCreateTopic channel) {
        return () -> authRestApiClient.createChannelByChannelApi(channel);
    }

    @Override
    public IExecutor loadModifyPublicTopicNameByChannelApi(ReqCreateTopic channel, int channelId) {
        return () -> authRestApiClient.modifyPublicTopicNameByChannelApi(channel, channelId);
    }

    @Override
    public IExecutor loadDeleteTopicByChannelApi(int channelId, ReqDeleteTopic reqDeleteTopic) {
        return () -> authRestApiClient.deleteTopicByChannelApi(channelId, reqDeleteTopic);
    }

    @Override
    public IExecutor loadJoinTopicByChannelApi(int channelId, ReqDeleteTopic reqDeleteTopic) {
        return () -> authRestApiClient.joinTopicByChannelApi(channelId, reqDeleteTopic);
    }

    @Override
    public IExecutor loadLeaveTopicByChannelApi(int channelId, ReqDeleteTopic reqDeleteTopic) {
        return () -> authRestApiClient.leaveTopicByChannelApi(channelId, reqDeleteTopic);
    }

    @Override
    public IExecutor loadInvitePublicTopicByChannelApi(int channelId, ReqInviteTopicUsers reqInviteTopicUsers) {
        return () -> authRestApiClient.invitePublicTopicByChannelApi(channelId, reqInviteTopicUsers);
    }

    @Override
    public IExecutor loadGetPublicTopicMessagesByChannelMessageApi(int teamId, int channelId, int fromId, int count) {
        return () -> authRestApiClient.getPublicTopicMessagesByChannelMessageApi(teamId, channelId, fromId, count);
    }

    @Override
    public IExecutor loadGetPublicTopicMessagesByChannelMessageApi(int teamId, int channelId) {
        return () -> authRestApiClient.getPublicTopicMessagesByChannelMessageApi(teamId, channelId);
    }

    @Override
    public IExecutor loadGetPublicTopicUpdatedMessagesByChannelMessageApi(int teamId, int channelId, int currentLinkId) {
        return () -> authRestApiClient.getPublicTopicUpdatedMessagesByChannelMessageApi(teamId, channelId, currentLinkId);
    }

    @Override
    public IExecutor loadGetPublicTopicUpdatedMessagesForMarkerByChannelMessageApi(int teamId, int channelId, int currentLinkId) {
        return () -> authRestApiClient.getPublicTopicUpdatedMessagesForMarkerByChannelMessageApi(teamId, channelId, currentLinkId);
    }

    @Override
    public IExecutor loadGetPublicTopicMarkerMessagesByChannelMessageApi(int teamId, int channelId, int currentLinkId) {
        return () -> authRestApiClient.getPublicTopicMarkerMessagesByChannelMessageApi(teamId, channelId, currentLinkId);
    }

    @Override
    public IExecutor loadSendPublicTopicMessageByChannelMessageApi(ReqSendMessage message, int channelId) {
        return () -> authRestApiClient.sendPublicTopicMessageByChannelMessageApi(message, channelId);
    }

    @Override
    public IExecutor loadModifyPublicTopicMessageByChannelMessageApi(ReqModifyMessage message, int channelId, int messageId) {
        return () -> authRestApiClient.modifyPublicTopicMessageByChannelMessageApi(message, channelId, messageId);
    }

    @Override
    public IExecutor loadDeletePublicTopicMessageByChannelMessageApi(int teamId, int channelId, int messageId) {
        return () -> authRestApiClient.deletePublicTopicMessageByChannelMessageApi(teamId, channelId, messageId);
    }

    @Override
    public IExecutor loadGetChatListByChatApi(int memberId) {
        return () -> authRestApiClient.getChatListByChatApi(memberId);
    }

    @Override
    public IExecutor loadDeleteChatByChatApi(int memberId, int entityId) {
        return () -> authRestApiClient.deleteChatByChatApi(memberId, entityId);
    }

    @Override
    public IExecutor loadSendMessageCommentByCommentsApi(ReqSendComment comment, int messageId) {
        return () -> authRestApiClient.sendMessageCommentByCommentsApi(comment, messageId);
    }

    @Override
    public IExecutor loadModifyMessageCommentByCommentsApi(ReqSendComment comment, int messageId, int commentId) {
        return () -> authRestApiClient.modifyMessageCommentByCommentsApi(comment, messageId, commentId);
    }

    @Override
    public IExecutor loadDeleteMessageCommentByCommentsApi(int teamId, int messageId, int commentId) {
        return () -> authRestApiClient.deleteMessageCommentByCommentsApi(teamId, messageId, commentId);
    }

    @Override
    public IExecutor loadGetDirectMessagesByDirectMessageApi(int teamId, int userId, int fromId, int count) {
        return () -> authRestApiClient.getDirectMessagesByDirectMessageApi(teamId, userId, fromId, count);
    }

    @Override
    public IExecutor loadGetDirectMessagesByDirectMessageApi(int teamId, int userId) {
        return () -> authRestApiClient.getDirectMessagesByDirectMessageApi(teamId, userId);
    }

    @Override
    public IExecutor loadGetDirectMessagesUpdatedByDirectMessageApi(int teamId, int userId, int timeAfter) {
        return () -> authRestApiClient.getDirectMessagesUpdatedByDirectMessageApi(teamId, userId, timeAfter);
    }

    @Override
    public IExecutor loadGetDirectMessagesUpdatedForMarkerByDirectMessageApi(int teamId, int userId, int currentLinkId) {
        return () -> authRestApiClient.getDirectMessagesUpdatedForMarkerByDirectMessageApi(teamId, userId, currentLinkId);
    }

    @Override
    public IExecutor loadGetDirectMarkerMessagesByDirectMessageApi(int teamId, int userId, int currentLinkId) {
        return () -> authRestApiClient.getDirectMarkerMessagesByDirectMessageApi(teamId, userId, currentLinkId);
    }

    @Override
    public IExecutor loadSendDirectMessageByDirectMessageApi(ReqSendMessage message, int userId) {
        return () -> authRestApiClient.sendDirectMessageByDirectMessageApi(message, userId);
    }

    @Override
    public IExecutor loadModifyDirectMessageByDirectMessageApi(ReqModifyMessage message, int userId, int messageId) {
        return () -> authRestApiClient.modifyDirectMessageByDirectMessageApi(message, userId, messageId);
    }

    @Override
    public IExecutor loadDeleteDirectMessageByDirectMessageApi(int teamId, int userId, int messageId) {
        return () -> authRestApiClient.deleteDirectMessageByDirectMessageApi(teamId, userId, messageId);
    }

    @Override
    public IExecutor loadCreatePrivateGroupByGroupApi(ReqCreateTopic group) {
        return () -> authRestApiClient.createPrivateGroupByGroupApi(group);
    }

    @Override
    public IExecutor loadModifyGroupByGroupApi(ReqCreateTopic channel, int groupId) {
        return () -> authRestApiClient.modifyGroupByGroupApi(channel, groupId);
    }

    @Override
    public IExecutor loadDeleteGroupByGroupApi(int teamId, int groupId) {
        return () -> authRestApiClient.deleteGroupByGroupApi(teamId, groupId);
    }

    @Override
    public IExecutor loadLeaveGroupByGroupApi(int groupId, ReqTeam team) {
        return () -> authRestApiClient.leaveGroupByGroupApi(groupId, team);
    }

    @Override
    public IExecutor loadInviteGroupByGroupApi(int groupId, ReqInviteTopicUsers inviteUsers) {
        return () -> authRestApiClient.inviteGroupByGroupApi(groupId, inviteUsers);
    }

    @Override
    public IExecutor loadGetGroupMessagesByGroupMessageApi(int teamId, int groupId, int fromId, int count) {
        return () -> authRestApiClient.getGroupMessagesByGroupMessageApi(teamId, groupId, fromId, count);
    }

    @Override
    public IExecutor loadGetGroupMessagesByGroupMessageApi(int teamId, int groupId) {
        return () -> authRestApiClient.getGroupMessagesByGroupMessageApi(teamId, groupId);
    }

    @Override
    public IExecutor loadGetGroupMessagesUpdatedByGroupMessageApi(int teamId, int groupId, int lastLinkId) {
        return () -> authRestApiClient.getGroupMessagesUpdatedByGroupMessageApi(teamId, groupId, lastLinkId);
    }

    @Override
    public IExecutor loadGetGroupMessagesUpdatedForMarkerByGroupMessageApi(int teamId, int groupId, int currentLinkId) {
        return () -> authRestApiClient.getGroupMessagesUpdatedByGroupMessageApi(teamId, groupId, currentLinkId);
    }

    @Override
    public IExecutor loadGetGroupMarkerMessagesByGroupMessageApi(int teamId, int groupId, int currentLinkId) {
        return () -> authRestApiClient.getGroupMarkerMessagesByGroupMessageApi(teamId, groupId, currentLinkId);
    }

    @Override
    public IExecutor loadSendGroupMessageByGroupMessageApi(ReqSendMessage message, int groupId) {
        return () -> authRestApiClient.sendGroupMessageByGroupMessageApi(message, groupId);
    }

    @Override
    public IExecutor loadModifyPrivateGroupMessageByGroupMessageApi(ReqModifyMessage message, int groupId, int messageId) {
        return () -> authRestApiClient.modifyPrivateGroupMessageByGroupMessageApi(message, groupId, messageId);
    }

    @Override
    public IExecutor loadDeletePrivateGroupMessageByGroupMessageApi(int teamId, int groupId, int messageId) {
        return () -> authRestApiClient.deletePrivateGroupMessageByGroupMessageApi(teamId, groupId, messageId);
    }


    @Override
    public IExecutor loadUpdateMemberProfileByProfileApi(int memberId, ReqUpdateProfile reqUpdateProfile) {
        return () -> authRestApiClient.updateMemberProfileByProfileApi(memberId, reqUpdateProfile);
    }

    @Override
    public IExecutor loadUpdateMemberNameByProfileApi(int memberId, ReqProfileName reqProfileName) {
        return () -> authRestApiClient.updateMemberNameByProfileApi(memberId, reqProfileName);
    }

    @Override
    public IExecutor loadUpdateMemberEmailByProfileApi(int memberId, ReqAccountEmail reqAccountEmail) {
        return () -> authRestApiClient.updateMemberEmailByProfileApi(memberId, reqAccountEmail);
    }

    @Override
    public IExecutor loadAcceptOrDeclineInvitationByInvitationApi(String invitationId, ReqInvitationAcceptOrIgnore reqInvitationAcceptOrIgnore) {
        return () -> authRestApiClient.acceptOrDeclineInvitationByInvitationApi(invitationId, reqInvitationAcceptOrIgnore);
    }

    @Override
    public IExecutor loadGetPendingTeamInfoByInvitationApi() {
        return () -> authRestApiClient.getPendingTeamInfoByInvitationApi();
    }

    @Override
    public IExecutor loadSearchMessagesByMessageSearchApi(int teamId, String query, int page, int perPage, int writerId, int entityId) {
        return () -> authRestApiClient.searchMessagesByMessageSearchApi(teamId, query, page, perPage, writerId, entityId);
    }

    @Override
    public IExecutor loadSearchMessagesByEntityIdByMessageSearchApi(int teamId, String query, int page, int perPage, int entityId) {
        return () -> authRestApiClient.searchMessagesByEntityIdByMessageSearchApi(teamId, query, page, perPage, entityId);
    }

    @Override
    public IExecutor loadSearchMessagesByWriterIdByMessageSearchApi(int teamId, String query, int page, int perPage, int writerId) {
        return () -> authRestApiClient.searchMessagesByWriterIdByMessageSearchApi(teamId, query, page, perPage, writerId);
    }

    @Override
    public IExecutor loadSearchMessagesByMessageSearchApi(int teamId, String query, int page, int perPage) {
        return () -> authRestApiClient.searchMessagesByMessageSearchApi(teamId, query, page, perPage);
    }

    @Override
    public IExecutor loadGetFileDetailByMessagesApiAuth(int teamId, int messageId) {
        return () -> authRestApiClient.getFileDetailByMessagesApiAuth(teamId, messageId);
    }

    @Override
    public IExecutor loadShareMessageByMessagesApiAuth(ReqShareMessage share, int messageId) {
        return () -> authRestApiClient.shareMessageByMessagesApiAuth(share, messageId);
    }

    @Override
    public IExecutor loadUnshareMessageByMessagesApiAuth(ReqUnshareMessage share, int messageId) {
        return () -> authRestApiClient.unshareMessageByMessagesApiAuth(share, messageId);
    }

    @Override
    public IExecutor loadGetRoomInfoByRoomsApi(int teamId, int roomId) {
        return () -> authRestApiClient.getRoomInfoByRoomsApi(teamId, roomId);
    }

    @Override
    public IExecutor loadEnableFavoriteByStarredEntityApi(ReqTeam reqTeam, int entityId) {
        return () -> authRestApiClient.enableFavoriteByStarredEntityApi(reqTeam, entityId);
    }

    @Override
    public IExecutor loadDisableFavoriteByStarredEntityApi(int teamId, int entityId) {
        return () -> authRestApiClient.disableFavoriteByStarredEntityApi(teamId, entityId);
    }

    @Override
    public IExecutor loadSendStickerByStickerApi(ReqSendSticker reqSendSticker) {
        return () -> authRestApiClient.sendStickerByStickerApi(reqSendSticker);
    }

    @Override
    public IExecutor loadSendStickerCommentByStickerApi(ReqSendSticker reqSendSticker) {
        return () -> authRestApiClient.sendStickerCommentByStickerApi(reqSendSticker);
    }

    @Override
    public IExecutor loadCreateNewTeamByTeamApi(ReqCreateNewTeam req) {
        return () -> authRestApiClient.createNewTeamByTeamApi(req);
    }

    @Override
    public IExecutor loadGetMemberProfileByTeamApi(int teamId, int memberId) {
        return () -> authRestApiClient.getMemberProfileByTeamApi(teamId, memberId);
    }

    @Override
    public IExecutor loadInviteToTeamByTeamApi(int teamId, ReqInvitationMembers invitationMembers) {
        return () -> authRestApiClient.inviteToTeamByTeamApi(teamId, invitationMembers);
    }

    @Override
    public IExecutor loadGetTeamInfoByTeamApi(int teamId) {
        return () -> authRestApiClient.getTeamInfoByTeamApi(teamId);
    }

    @Override
    public IExecutor loadResetPasswordByAccountPasswordApi(ReqAccountEmail reqAccountEmail) {
        return () -> SimpleRestApiClient.resetPasswordByAccountPasswordApi(reqAccountEmail);
    }

    @Override
    public IExecutor loadGetConfigByMainRestApi() {
        return () -> SimpleRestApiClient.getConfigByMainRest();
    }

    @Override
    public IExecutor loadGetTeamIdByMainRestApi(String userEmail) {
        return () -> SimpleRestApiClient.getTeamIdByMainRest(userEmail);
    }

    @Override
    public IExecutor loadGetAccessTokenByMainRestApi(ReqAccessToken login) {
        return () -> SimpleRestApiClient.getAccessTokenByMainRest(login);
    }

    @Override
    public IExecutor loadSignUpAccountByMainRestApi(ReqSignUpInfo signUpInfo) {
        return () -> SimpleRestApiClient.signUpAccountByMainRest(signUpInfo);
    }

    @Override
    public IExecutor loadActivateAccountByMainRestApi(ReqAccountActivate reqAccountActivate) {
        return () -> SimpleRestApiClient.activateAccountByMainRest(reqAccountActivate);
    }

    @Override
    public IExecutor loadAccountVerificationByMainRestApi(ReqAccountVerification reqAccountVerification) {
        return () -> SimpleRestApiClient.accountVerificationByMainRest(reqAccountVerification);
    }

}