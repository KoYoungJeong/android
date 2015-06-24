package com.tosslab.jandi.app.network.manager.RestApiClient;

import com.tosslab.jandi.app.network.client.account.devices.AccountDeviceApiV2Client;
import com.tosslab.jandi.app.network.client.account.devices.IAccountDeviceApiAuth;
import com.tosslab.jandi.app.network.client.account.emails.AccountEmailsApiV2Client;
import com.tosslab.jandi.app.network.client.account.emails.IAccountEmailsApiAuth;
import com.tosslab.jandi.app.network.client.account.password.AccountPasswordApiV2Client;
import com.tosslab.jandi.app.network.client.account.password.IAccountPasswordApiAuth;
import com.tosslab.jandi.app.network.client.chat.IChatApiAuth;
import com.tosslab.jandi.app.network.client.direct.message.IDirectMessageApiAuth;
import com.tosslab.jandi.app.network.client.invitation.IInvitationApiAuth;
import com.tosslab.jandi.app.network.client.main.IMainRestApiAuth;
import com.tosslab.jandi.app.network.client.main.MainRestApiClient;
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
import com.tosslab.jandi.app.network.manager.RestApiClient.RestAdapterFactory.RestAdapterFactory;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
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
import com.tosslab.jandi.app.network.models.ReqSubscibeToken;
import com.tosslab.jandi.app.network.models.ReqTeam;
import com.tosslab.jandi.app.network.models.ReqUpdatePrimaryEmailInfo;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResChat;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResInvitationMembers;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessageSearch;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;
import com.tosslab.jandi.app.network.models.ResRoomInfo;
import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;
import com.tosslab.jandi.app.network.models.sticker.ReqSendSticker;

import java.util.List;

import retrofit.RestAdapter;
import retrofit.RetrofitError;

/**
 * Created by tee on 15. 6. 20..
 */
public class JacksonConvertedAuthRestApiClient implements IAccountDeviceApiAuth, IAccountEmailsApiAuth, IAccountPasswordApiAuth,
        IChatApiAuth, IDirectMessageApiAuth, IInvitationApiAuth, IMainRestApiAuth, ICommentsApiAuth, IMessageSearchApiAuth,
        IMessagesApiAuth, IGroupMessageApiAuth, IGroupApiAuth, IProfileApiAuth, IChannelMessageApiAuth, IChannelApiAuth,
        IRoomsApiAuth, IAccountProfileApiAuth, IStarredEntityApiAuth, IStickerApiAuth, ITeamApiAuth {

    static RestAdapter restAdapter = RestAdapterFactory.getJacksonConvertedAuthRestAdapter();

    @Override
    public ResAccountInfo registerNotificationTokenByAccountDeviceApi(ReqNotificationRegister reqNotificationRegister) throws RetrofitError {
        return restAdapter.create(AccountDeviceApiV2Client.class).registerNotificationToken(reqNotificationRegister);
    }

    @Override
    public ResAccountInfo deleteNotificationTokenByAccountDeviceApi(ReqDeviceToken reqDeviceToken) throws RetrofitError {
        return restAdapter.create(AccountDeviceApiV2Client.class).deleteNotificationToken(reqDeviceToken);
    }

    @Override
    public ResAccountInfo subscribeStateNotificationByAccountDeviceApi(ReqSubscibeToken reqDeviceToken) throws RetrofitError {
        return restAdapter.create(AccountDeviceApiV2Client.class).subscribeStateNotification(reqDeviceToken);
    }

    @Override
    public ResCommon getNotificationBadgeByAccountDeviceApi(ReqNotificationTarget reqNotificationTarget) throws RetrofitError {
        return restAdapter.create(AccountDeviceApiV2Client.class).getNotificationBadge(reqNotificationTarget);
    }

    @Override
    public ResAccountInfo requestAddEmailByAccountEmailApi(ReqAccountEmail reqAccountEmail) throws RetrofitError {
        return restAdapter.create(AccountEmailsApiV2Client.class).requestAddEmail(reqAccountEmail);
    }

    @Override
    public ResAccountInfo confirmEmailByAccountEmailApi(ReqConfirmEmail reqConfirmEmail) throws RetrofitError {
        return restAdapter.create(AccountEmailsApiV2Client.class).confirmEmail(reqConfirmEmail);
    }

    @Override
    public ResAccountInfo deleteEmailByAccountEmailApi(ReqAccountEmail reqConfirmEmail) throws RetrofitError {
        return restAdapter.create(AccountEmailsApiV2Client.class).deleteEmail(reqConfirmEmail);
    }

    @Override
    public ResCommon changePasswordByAccountPasswordApi(ReqChangePassword reqConfirmEmail) throws RetrofitError {
        return restAdapter.create(AccountPasswordApiV2Client.class).changePassword(reqConfirmEmail);
    }

    @Override
    public ResAccountInfo getAccountInfoByMainRest() throws RetrofitError {
        return restAdapter.create(MainRestApiClient.class).getAccountInfo();
    }

    @Override
    public ResAccountInfo updatePrimaryEmailByMainRest(ReqUpdatePrimaryEmailInfo updatePrimaryEmailInfo) throws RetrofitError {
        return restAdapter.create(MainRestApiClient.class).updatePrimaryEmail(updatePrimaryEmailInfo);
    }

    @Override
    public ResLeftSideMenu getInfosForSideMenuByMainRest(int teamId) throws RetrofitError {
        return restAdapter.create(MainRestApiClient.class).getInfosForSideMenu(teamId);
    }

    @Override
    public ResCommon setMarkerByMainRest(int entityId, ReqSetMarker reqSetMarker) throws RetrofitError {
        return restAdapter.create(MainRestApiClient.class).setMarker(entityId, reqSetMarker);
    }

    @Override
    public ResSearchFile searchFileByMainRest(ReqSearchFile reqSearchFile) throws RetrofitError {
        return restAdapter.create(MainRestApiClient.class).searchFile(reqSearchFile);
    }

    @Override
    public ResAccountInfo changeNameByAccountProfileApi(ReqProfileName reqProfileName) throws RetrofitError {
        return null;
    }

    @Override
    public ResAccountInfo changePrimaryEmailByAccountProfileApi(ReqAccountEmail reqAccountEmail) throws RetrofitError {
        return null;
    }

    @Override
    public ResCommon createChannelByChannelApi(ReqCreateTopic channel) throws RetrofitError {
        return null;
    }

    @Override
    public ResCommon modifyPublicTopicNameByChannelApi(ReqCreateTopic channel, int channelId) throws RetrofitError {
        return null;
    }

    @Override
    public ResCommon deleteTopicByChannelApi(int channelId, ReqDeleteTopic reqDeleteTopic) throws RetrofitError {
        return null;
    }

    @Override
    public ResCommon joinTopicByChannelApi(int channelId, ReqDeleteTopic reqDeleteTopic) throws RetrofitError {
        return null;
    }

    @Override
    public ResCommon leaveTopicByChannelApi(int channelId, ReqDeleteTopic reqDeleteTopic) throws RetrofitError {
        return null;
    }

    @Override
    public ResCommon invitePublicTopicByChannelApi(int channelId, ReqInviteTopicUsers reqInviteTopicUsers) throws RetrofitError {
        return null;
    }

    @Override
    public ResMessages getPublicTopicMessagesByChannelMessageApi(int teamId, int channelId, int fromId, int count) throws RetrofitError {
        return null;
    }

    @Override
    public ResMessages getPublicTopicMessagesByChannelMessageApi(int teamId, int channelId) throws RetrofitError {
        return null;
    }

    @Override
    public ResUpdateMessages getPublicTopicUpdatedMessagesByChannelMessageApi(int teamId, int channelId, int currentLinkId) throws RetrofitError {
        return null;
    }

    @Override
    public ResMessages getPublicTopicUpdatedMessagesForMarkerByChannelMessageApi(int teamId, int channelId, int currentLinkId) throws RetrofitError {
        return null;
    }

    @Override
    public ResMessages getPublicTopicMarkerMessagesByChannelMessageApi(int teamId, int channelId, int currentLinkId) throws RetrofitError {
        return null;
    }

    @Override
    public ResCommon sendPublicTopicMessageByChannelMessageApi(ReqSendMessage message, int channelId) throws RetrofitError {
        return null;
    }

    @Override
    public ResCommon modifyPublicTopicMessageByChannelMessageApi(ReqModifyMessage message, int channelId, int messageId) throws RetrofitError {
        return null;
    }

    @Override
    public ResCommon deletePublicTopicMessageByChannelMessageApi(int teamId, int channelId, int messageId) throws RetrofitError {
        return null;
    }

    @Override
    public List<ResChat> getChatListByChatApi(int memberId) throws RetrofitError {
        return null;
    }

    @Override
    public ResCommon deleteChatByChatApi(int memberId, int entityId) throws RetrofitError {
        return null;
    }

    @Override
    public ResCommon sendMessageCommentByCommentsApi(ReqSendComment comment, int messageId) throws RetrofitError {
        return null;
    }

    @Override
    public ResCommon modifyMessageCommentByCommentsApi(ReqSendComment comment, int messageId, int commentId) throws RetrofitError {
        return null;
    }

    @Override
    public ResCommon deleteMessageCommentByCommentsApi(int teamId, int messageId, int commentId) throws RetrofitError {
        return null;
    }

    @Override
    public ResMessages getDirectMessagesByDirectMessageApi(int teamId, int userId, int fromId, int count) throws RetrofitError {
        return null;
    }

    @Override
    public ResMessages getDirectMessagesByDirectMessageApi(int teamId, int userId) throws RetrofitError {
        return null;
    }

    @Override
    public ResUpdateMessages getDirectMessagesUpdatedByDirectMessageApi(int teamId, int userId, int timeAfter) throws RetrofitError {
        return null;
    }

    @Override
    public ResMessages getDirectMessagesUpdatedForMarkerByDirectMessageApi(int teamId, int userId, int currentLinkId) throws RetrofitError {
        return null;
    }

    @Override
    public ResMessages getDirectMarkerMessagesByDirectMessageApi(int teamId, int userId, int currentLinkId) throws RetrofitError {
        return null;
    }

    @Override
    public ResCommon sendDirectMessageByDirectMessageApi(ReqSendMessage message, int userId) throws RetrofitError {
        return null;
    }

    @Override
    public ResCommon modifyDirectMessageByDirectMessageApi(ReqModifyMessage message, int userId, int messageId) throws RetrofitError {
        return null;
    }

    @Override
    public ResCommon deleteDirectMessageByDirectMessageApi(int teamId, int userId, int messageId) throws RetrofitError {
        return null;
    }

    @Override
    public ResCommon createPrivateGroupByGroupApi(ReqCreateTopic group) throws RetrofitError {
        return null;
    }

    @Override
    public ResCommon modifyGroupByGroupApi(ReqCreateTopic channel, int groupId) throws RetrofitError {
        return null;
    }

    @Override
    public ResCommon deleteGroupByGroupApi(int teamId, int groupId) throws RetrofitError {
        return null;
    }

    @Override
    public ResCommon leaveGroupByGroupApi(int groupId, ReqTeam team) throws RetrofitError {
        return null;
    }

    @Override
    public ResCommon inviteGroupByGroupApi(int groupId, ReqInviteTopicUsers inviteUsers) throws RetrofitError {
        return null;
    }

    @Override
    public ResMessages getGroupMessagesByGroupMessageApi(int teamId, int groupId, int fromId, int count) throws RetrofitError {
        return null;
    }

    @Override
    public ResMessages getGroupMessagesByGroupMessageApi(int teamId, int groupId) throws RetrofitError {
        return null;
    }

    @Override
    public ResUpdateMessages getGroupMessagesUpdatedByGroupMessageApi(int teamId, int groupId, int lastLinkId) throws RetrofitError {
        return null;
    }

    @Override
    public ResMessages getGroupMessagesUpdatedForMarkerByGroupMessageApi(int teamId, int groupId, int currentLinkId) throws RetrofitError {
        return null;
    }

    @Override
    public ResMessages getGroupMarkerMessagesByGroupMessageApi(int teamId, int groupId, int currentLinkId) throws RetrofitError {
        return null;
    }

    @Override
    public ResCommon sendGroupMessageByGroupMessageApi(ReqSendMessage message, int groupId) throws RetrofitError {
        return null;
    }

    @Override
    public ResCommon modifyPrivateGroupMessageByGroupMessageApi(ReqModifyMessage message, int groupId, int messageId) throws RetrofitError {
        return null;
    }

    @Override
    public ResCommon deletePrivateGroupMessageByGroupMessageApi(int teamId, int groupId, int messageId) throws RetrofitError {
        return null;
    }

    @Override
    public ResTeamDetailInfo acceptOrDeclineInvitationByInvitationApi(String invitationId, ReqInvitationAcceptOrIgnore reqInvitationAcceptOrIgnore) throws RetrofitError {
        return null;
    }

    @Override
    public List<ResPendingTeamInfo> getPedingTeamInfoByInvitationApi() throws RetrofitError {
        return null;
    }

    @Override
    public ResMessageSearch searchMessagesByMessageSearchApi(int teamId, String query, int page, int perPage, int writerId, int entityId) throws RetrofitError {
        return null;
    }

    @Override
    public ResMessageSearch searchMessagesByEntityIdByMessageSearchApi(int teamId, String query, int page, int perPage, int entityId) throws RetrofitError {
        return null;
    }

    @Override
    public ResMessageSearch searchMessagesByWriterIdByMessageSearchApi(int teamId, String query, int page, int perPage, int writerId) throws RetrofitError {
        return null;
    }

    @Override
    public ResMessageSearch searchMessagesByMessageSearchApi(int teamId, String query, int page, int perPage) throws RetrofitError {
        return null;
    }

    @Override
    public ResMessageSearch searchMessagesByMessagesApi(int teamId, String query, int page, int perPage, int writerId, int entityId) throws RetrofitError {
        return null;
    }

    @Override
    public ResMessageSearch searchMessagesByEntityIdByMessagesApi(int teamId, String query, int page, int perPage, int entityId) throws RetrofitError {
        return null;
    }

    @Override
    public ResMessageSearch searchMessagesByWriterIdByMessagesApi(int teamId, String query, int page, int perPage, int writerId) throws RetrofitError {
        return null;
    }

    @Override
    public ResMessageSearch searchMessagesByMessagesApi(int teamId, String query, int page, int perPage) throws RetrofitError {
        return null;
    }

    @Override
    public ResLeftSideMenu.User updateMemberProfileByProfileApi(int memberId, ReqUpdateProfile reqUpdateProfile) throws RetrofitError {
        return null;
    }

    @Override
    public ResCommon updateMemberNameByProfileApi(int memberId, ReqProfileName reqProfileName) throws RetrofitError {
        return null;
    }

    @Override
    public ResLeftSideMenu.User updateMemberEmailByProfileApi(int memberId, ReqAccountEmail reqAccountEmail) throws RetrofitError {
        return null;
    }

    @Override
    public ResLeftSideMenu.User updateUserNameByProfileApi(ReqUpdateProfile reqUpdateProfile) throws RetrofitError {
        return null;
    }

    @Override
    public ResRoomInfo getRoomInfoByRoomsApi(int teamId, int roomId) throws RetrofitError {
        return null;
    }

    @Override
    public ResCommon enableFavoriteByStarredEntityApi(ReqTeam reqTeam, int entityId) throws RetrofitError {
        return null;
    }

    @Override
    public ResCommon disableFavoriteByStarredEntityApi(int teamId, int entityId) throws RetrofitError {
        return null;
    }

    @Override
    public ResCommon sendStickerByStickerApi(ReqSendSticker reqSendSticker) throws RetrofitError {
        return null;
    }

    @Override
    public ResCommon sendStickerCommentByStickerApi(ReqSendSticker reqSendSticker) throws RetrofitError {
        return null;
    }

    @Override
    public ResTeamDetailInfo createNewTeamByTeamApi(ReqCreateNewTeam req) throws RetrofitError {
        return null;
    }

    @Override
    public ResLeftSideMenu.User getMemberProfileByTeamApi(int teamId, int memberId) throws RetrofitError {
        return null;
    }

    @Override
    public List<ResInvitationMembers> inviteToTeamByTeamApi(int teamId, ReqInvitationMembers invitationMembers) throws RetrofitError {
        return null;
    }

    @Override
    public ResTeamDetailInfo.InviteTeam getTeamInfoByTeamApi(int teamId) throws RetrofitError {
        return null;
    }
}