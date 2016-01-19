package com.tosslab.jandi.app.network.manager;

import com.tosslab.jandi.app.network.client.account.devices.IAccountDeviceApiAuth;
import com.tosslab.jandi.app.network.client.account.emails.IAccountEmailsApiAuth;
import com.tosslab.jandi.app.network.client.account.password.IAccountPasswordApiAuth;
import com.tosslab.jandi.app.network.client.account.password.IAccountPasswordApiSimple;
import com.tosslab.jandi.app.network.client.chat.IChatApiAuth;
import com.tosslab.jandi.app.network.client.direct.message.IDirectMessageApiAuth;
import com.tosslab.jandi.app.network.client.events.IEventsApiAuth;
import com.tosslab.jandi.app.network.client.file.IFileApiAuth;
import com.tosslab.jandi.app.network.client.invitation.IInvitationApiAuth;
import com.tosslab.jandi.app.network.client.main.IMainRestApiAuth;
import com.tosslab.jandi.app.network.client.main.IMainRestApiSimple;
import com.tosslab.jandi.app.network.client.messages.IMessagesApiAuth;
import com.tosslab.jandi.app.network.client.messages.comments.ICommentsApiAuth;
import com.tosslab.jandi.app.network.client.messages.search.IMessageSearchApiAuth;
import com.tosslab.jandi.app.network.client.platform.IPlatformApiAuth;
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
import com.tosslab.jandi.app.network.client.validation.ValidationApiAuth;
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

import retrofit.RetrofitError;

public class RequestApiManager implements IAccountDeviceApiAuth, IAccountEmailsApiAuth, IAccountPasswordApiAuth,
        IChatApiAuth, IDirectMessageApiAuth, IInvitationApiAuth, IMainRestApiAuth, ICommentsApiAuth, IMessageSearchApiAuth,
        IMessagesApiAuth, IGroupMessageApiAuth, IGroupApiAuth, IProfileApiAuth, IChannelMessageApiAuth, IChannelApiAuth,
        IRoomsApiAuth, IAccountProfileApiAuth, IStarredEntityApiAuth, IStickerApiAuth, ITeamApiAuth, IAccountPasswordApiSimple,
        IMainRestApiSimple, IFileApiAuth, IPlatformApiAuth, IEventsApiAuth, ValidationApiAuth {

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
    public ResLeftSideMenu getInfosForSideMenuByMainRest(long teamId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetInfosForSideMenuByMainRestApi(teamId));
    }

    @Override
    public ResCommon setMarkerByMainRest(long entityId, ReqSetMarker reqSetMarker) throws RetrofitError {
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
    public ResCommon modifyPublicTopicNameByChannelApi(ReqCreateTopic channel, long channelId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadModifyPublicTopicNameByChannelApi(channel, channelId));
    }

    @Override
    public ResCommon deleteTopicByChannelApi(long channelId, ReqDeleteTopic reqDeleteTopic) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadDeleteTopicByChannelApi(channelId, reqDeleteTopic));
    }

    @Override
    public ResCommon joinTopicByChannelApi(long channelId, ReqDeleteTopic reqDeleteTopic) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadJoinTopicByChannelApi(channelId, reqDeleteTopic));
    }

    @Override
    public ResCommon leaveTopicByChannelApi(long channelId, ReqDeleteTopic reqDeleteTopic) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadLeaveTopicByChannelApi(channelId, reqDeleteTopic));
    }

    @Override
    public ResCommon invitePublicTopicByChannelApi(long channelId, ReqInviteTopicUsers reqInviteTopicUsers) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadInvitePublicTopicByChannelApi(channelId, reqInviteTopicUsers));
    }

    @Override
    public ResMessages getPublicTopicMessagesByChannelMessageApi(long teamId, long channelId, long fromId, int count) throws RetrofitError {
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
    public ResMessages getPublicTopicUpdatedMessagesForMarkerByChannelMessageApi(long teamId, long channelId, long currentLinkId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetPublicTopicUpdatedMessagesForMarkerByChannelMessageApi(teamId, channelId, currentLinkId));
    }

    @Override
    public ResMessages getPublicTopicUpdatedMessagesForMarkerByChannelMessageApi(long teamId, long channelId, long currentLinkId, int count) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetPublicTopicUpdatedMessagesForMarkerByChannelMessageApi(teamId, channelId, currentLinkId, count));
    }

    @Override
    public ResMessages getPublicTopicMarkerMessagesByChannelMessageApi(long teamId, long channelId, long currentLinkId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetPublicTopicMarkerMessagesByChannelMessageApi(teamId, channelId, currentLinkId));
    }

    @Override
    public ResCommon sendPublicTopicMessageByChannelMessageApi(long channelId, long teamId, ReqSendMessageV3 reqSendMessageV3) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadSendPublicTopicMessageByChannelMessageApi(channelId, teamId, reqSendMessageV3));
    }

    @Override
    public ResCommon modifyPublicTopicMessageByChannelMessageApi(ReqModifyMessage message, int channelId, int messageId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadModifyPublicTopicMessageByChannelMessageApi(message, channelId, messageId));
    }

    @Override
    public ResCommon deletePublicTopicMessageByChannelMessageApi(long teamId, long channelId, long messageId) throws RetrofitError {
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
    public ResCommon sendMessageCommentByCommentsApi(long messageId, long teamId, ReqSendComment reqSendComment) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadSendMessageCommentByCommentsApi(messageId, teamId, reqSendComment));
    }

    @Override
    public ResCommon modifyMessageCommentByCommentsApi(ReqModifyComment comment, int messageId, int commentId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadModifyMessageCommentByCommentsApi(comment, messageId, commentId));
    }

    @Override
    public ResCommon deleteMessageCommentByCommentsApi(long teamId, long messageId, long commentId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadDeleteMessageCommentByCommentsApi(teamId, messageId, commentId));
    }

    @Override
    public ResMessages getDirectMessagesByDirectMessageApi(long teamId, long userId, long fromId, int count) throws RetrofitError {
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
    public ResMessages getDirectMessagesUpdatedForMarkerByDirectMessageApi(long teamId, long userId, long currentLinkId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetDirectMessagesUpdatedForMarkerByDirectMessageApi(teamId, userId, currentLinkId));
    }

    @Override
    public ResMessages getDirectMessagesUpdatedForMarkerByDirectMessageApi(long teamId, long userId, long currentLinkId, int count) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetDirectMessagesUpdatedForMarkerByDirectMessageApi(teamId, userId, currentLinkId, count));
    }

    @Override
    public ResMessages getDirectMarkerMessagesByDirectMessageApi(long teamId, long userId, long currentLinkId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetDirectMarkerMessagesByDirectMessageApi(teamId, userId, currentLinkId));
    }

    @Override
    public ResCommon sendDirectMessageByDirectMessageApi(long userId, long teamId,
                                                         ReqSendMessageV3 reqSendMessageV3) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadSendDirectMessageByDirectMessageApi(userId, teamId, reqSendMessageV3));
    }

    @Override
    public ResCommon modifyDirectMessageByDirectMessageApi(ReqModifyMessage message, int userId, int messageId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadModifyDirectMessageByDirectMessageApi(message, userId, messageId));
    }

    @Override
    public ResCommon deleteDirectMessageByDirectMessageApi(long teamId, long userId, long messageId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadDeleteDirectMessageByDirectMessageApi(teamId, userId, messageId));
    }

    @Override
    public ResCommon createPrivateGroupByGroupApi(ReqCreateTopic group) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadCreatePrivateGroupByGroupApi(group));
    }

    @Override
    public ResCommon modifyGroupByGroupApi(ReqCreateTopic channel, long groupId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadModifyGroupByGroupApi(channel, groupId));
    }

    @Override
    public ResCommon deleteGroupByGroupApi(long teamId, long groupId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadDeleteGroupByGroupApi(teamId, groupId));
    }

    @Override
    public ResCommon leaveGroupByGroupApi(long groupId, ReqTeam team) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadLeaveGroupByGroupApi(groupId, team));
    }

    @Override
    public ResCommon inviteGroupByGroupApi(long groupId, ReqInviteTopicUsers inviteUsers) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadInviteGroupByGroupApi(groupId, inviteUsers));
    }

    @Override
    public ResMessages getGroupMessagesByGroupMessageApi(long teamId, long groupId, long fromId, int count) throws RetrofitError {
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
    public ResMessages getGroupMessagesUpdatedForMarkerByGroupMessageApi(long teamId, long groupId,
                                                                         long currentLinkId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance()
                .loadGetGroupMessagesUpdatedForMarkerByGroupMessageApi(teamId, groupId, currentLinkId));
    }

    @Override
    public ResMessages getGroupMessagesUpdatedForMarkerByGroupMessageApi(long teamId, long groupId, long currentLinkId, int count) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance()
                .loadGetGroupMessagesUpdatedForMarkerByGroupMessageApi(teamId, groupId, currentLinkId, count));
    }

    @Override
    public ResMessages getGroupMarkerMessagesByGroupMessageApi(long teamId, long groupId, long currentLinkId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetGroupMarkerMessagesByGroupMessageApi(teamId, groupId, currentLinkId));
    }

    @Override
    public ResCommon sendGroupMessageByGroupMessageApi(long privateGroupId, long teamId, ReqSendMessageV3 reqSendMessageV3) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadSendGroupMessageByGroupMessageApi(privateGroupId, teamId, reqSendMessageV3));
    }

    @Override
    public ResCommon modifyPrivateGroupMessageByGroupMessageApi(ReqModifyMessage message, int groupId, int messageId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadModifyPrivateGroupMessageByGroupMessageApi(message, groupId, messageId));
    }

    @Override
    public ResCommon deletePrivateGroupMessageByGroupMessageApi(long teamId, long groupId, long messageId) throws RetrofitError {
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
    public ResFileDetail getFileDetailByMessagesApiAuth(long teamId, long messageId) {
        return requestApiExecute(RestApiLoader.getInstance().loadGetFileDetailByMessagesApiAuth(teamId, messageId));
    }

    @Override
    public ResCommon shareMessageByMessagesApiAuth(ReqShareMessage share, long messageId) {
        return requestApiExecute(RestApiLoader.getInstance().loadShareMessageByMessagesApiAuth(share, messageId));
    }

    @Override
    public ResCommon unshareMessageByMessagesApiAuth(ReqUnshareMessage share, long messageId) {
        return requestApiExecute(RestApiLoader.getInstance().loadUnshareMessageByMessagesApiAuth(share, messageId));
    }

    @Override
    public List<ResMessages.Link> getRoomUpdateMessageByMessagesApiAuth(long teamId, long roomId, long currentLinkId) {
        return requestApiExecute(RestApiLoader.getInstance().getRoomUpdateMessageByMessagesApiAuth(teamId, roomId, currentLinkId));
    }

    @Override
    public ResLeftSideMenu.User updateMemberProfileByProfileApi(long memberId, ReqUpdateProfile reqUpdateProfile) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadUpdateMemberProfileByProfileApi(memberId, reqUpdateProfile));
    }

    @Override
    public ResCommon updateMemberNameByProfileApi(long memberId, ReqProfileName reqProfileName) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadUpdateMemberNameByProfileApi(memberId, reqProfileName));
    }

    @Override
    public ResLeftSideMenu.User updateMemberEmailByProfileApi(long memberId, ReqAccountEmail reqAccountEmail) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadUpdateMemberEmailByProfileApi(memberId, reqAccountEmail));
    }

    @Override
    public ResRoomInfo getRoomInfoByRoomsApi(long teamId, long roomId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetRoomInfoByRoomsApi(teamId, roomId));
    }

    @Override
    public ResCommon enableFavoriteByStarredEntityApi(ReqTeam reqTeam, long entityId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadEnableFavoriteByStarredEntityApi(reqTeam, entityId));
    }

    @Override
    public ResCommon disableFavoriteByStarredEntityApi(long teamId, long entityId) throws RetrofitError {
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
    public ResLeftSideMenu.User getMemberProfileByTeamApi(long teamId, long memberId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetMemberProfileByTeamApi(teamId, memberId));
    }

    @Override
    public List<ResInvitationMembers> inviteToTeamByTeamApi(int teamId, ReqInvitationMembers invitationMembers) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadInviteToTeamByTeamApi(teamId, invitationMembers));
    }

    @Override
    public ResTeamDetailInfo.InviteTeam getTeamInfoByTeamApi(long teamId) throws RetrofitError {
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
    public ResMessages.OriginalMessage getMessage(long teamId, long messageId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetMessage(teamId, messageId));
    }

    @Override
    public ResCommon updateTopicPushSubscribe(int teamId, int topicId, ReqUpdateTopicPushSubscribe reqUpdateTopicPushSubscribe) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadUpdateTopicPushSubscribe(teamId, topicId, reqUpdateTopicPushSubscribe));
    }

    @Override
    public ResCommon deleteStickerCommentByStickerApi(long commentId, long teamId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadDeleteStickerCommentByStickerApi(commentId, teamId));
    }

    @Override
    public ResCommon deleteStickerByStickerApi(long messageId, long teamId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadDeleteStickerByStickerApi(messageId, teamId));
    }

    @Override
    public ResCommon deleteFileByFileApi(long teamId, long fileId) {
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

    @Override
    public ResStarMentioned getMentionedMessagesByTeamApi(int teamId, Integer messageId, int count) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetMentionedMessagesByTeamApi(teamId, messageId, count));
    }

    @Override
    public StarMentionedMessageObject registStarredMessageByTeamApi(long teamId, long messageId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadRegistStarredMessageByTeamApi(teamId, messageId));
    }

    @Override
    public ResCommon unregistStarredMessageByTeamApi(long teamId, long messageId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadUnregistStarredMessageByTeamApi(teamId, messageId));
    }

    @Override
    public ResStarMentioned getStarredMessagesByTeamApi(int teamId, Integer starredId, int count, String type) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetStarredMessagesByTeamApi(teamId, starredId, count, type));
    }

    @Override
    public ResCommon updatePlatformStatus(ReqUpdatePlatformStatus reqUpdatePlatformStatus) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadUpdatePlatformStatus(reqUpdatePlatformStatus));
    }

    @Override
    public ResCreateFolder createFolderByTeamApi(int teamId, ReqCreateFolder reqCreateFolder) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadCreateFolderByTeamApi(teamId, reqCreateFolder));
    }

    @Override
    public ResCommon deleteFolderByTeamApi(int teamId, int folderId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadDeleteFolderByTeamApi(teamId, folderId));
    }

    @Override
    public ResUpdateFolder updateFolderByTeamApi(int teamId, int folderId, ReqUpdateFolder reqUpdateFolder) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadUpdateFolderByTeamApi(teamId, folderId, reqUpdateFolder));
    }

    @Override
    public List<ResFolder> getFoldersByTeamApi(int teamId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetFoldersByTeamApi(teamId));
    }

    @Override
    public List<ResFolderItem> getFolderItemsByTeamApi(int teamId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetFolderItemsByTeamApi(teamId));
    }

    @Override
    public ResRegistFolderItem registFolderItemByTeamApi(int teamId, int folderId, ReqRegistFolderItem reqRegistFolderItem) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadRegistFolderItemByTeamApi(teamId, folderId, reqRegistFolderItem));
    }

    @Override
    public ResCommon deleteFolderItemByTeamApi(int teamId, int folderId, int itemId) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadDeleteFolderItemByTeamApi(teamId, folderId, itemId));
    }

    @Override
    public ResCommon kickUserFromTopic(int teamId, int topicId, ReqMember member) {
        return requestApiExecute(RestApiLoader.getInstance().loadKickUserFromTopic(teamId, topicId, member));
    }

    @Override
    public ResCommon assignToTopicOwner(int teamId, int topicId, ReqOwner owner) {
        return requestApiExecute(RestApiLoader.getInstance().loadAssignToTopicOwner(teamId, topicId, owner));
    }

    @Override
    public ResMessages.FileMessage enableFileExternalLink(long teamId, long fileId) {
        return requestApiExecute(RestApiLoader.getInstance().loadEnableFileExternalLink(teamId, fileId));
    }

    @Override
    public ResMessages.FileMessage disableFileExternalLink(long teamId, long fileId) {
        return requestApiExecute(RestApiLoader.getInstance().loadDisableFileExternalLink(teamId, fileId));
    }

    @Override
    public ResEventHistory getEventHistory(long ts, long memberId, String eventType, Integer size) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetEventHistory(ts, memberId, eventType, size));
    }

    @Override
    public ResValidation validDomain(String domain) throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadValidDomain(domain));
    }

    @Override
    public ResAvatarsInfo getAvartarsInfo() throws RetrofitError {
        return requestApiExecute(RestApiLoader.getInstance().loadGetAvartarsInfo());
    }
}