package com.tosslab.jandi.app.network.manager.restapiclient;

import com.tosslab.jandi.app.network.client.account.devices.AccountDeviceApiV2Client;
import com.tosslab.jandi.app.network.client.account.devices.IAccountDeviceApiAuth;
import com.tosslab.jandi.app.network.client.account.emails.AccountEmailsApiV2Client;
import com.tosslab.jandi.app.network.client.account.emails.IAccountEmailsApiAuth;
import com.tosslab.jandi.app.network.client.account.password.AccountPasswordApiV2Client;
import com.tosslab.jandi.app.network.client.account.password.IAccountPasswordApiAuth;
import com.tosslab.jandi.app.network.client.chat.ChatApiV2Client;
import com.tosslab.jandi.app.network.client.chat.IChatApiAuth;
import com.tosslab.jandi.app.network.client.direct.message.DirectMessageApiV2Client;
import com.tosslab.jandi.app.network.client.direct.message.IDirectMessageApiAuth;
import com.tosslab.jandi.app.network.client.file.FileApiV2Client;
import com.tosslab.jandi.app.network.client.file.IFileApiAuth;
import com.tosslab.jandi.app.network.client.invitation.IInvitationApiAuth;
import com.tosslab.jandi.app.network.client.invitation.InvitationApiV2Client;
import com.tosslab.jandi.app.network.client.main.IMainRestApiAuth;
import com.tosslab.jandi.app.network.client.main.MainRestApiClient;
import com.tosslab.jandi.app.network.client.messages.IMessagesApiAuth;
import com.tosslab.jandi.app.network.client.messages.MessagesApiV2Client;
import com.tosslab.jandi.app.network.client.messages.comments.CommentsApiV2Client;
import com.tosslab.jandi.app.network.client.messages.comments.ICommentsApiAuth;
import com.tosslab.jandi.app.network.client.messages.search.IMessageSearchApiAuth;
import com.tosslab.jandi.app.network.client.messages.search.MessagesSearchApiV2Client;
import com.tosslab.jandi.app.network.client.privatetopic.GroupApiV2Client;
import com.tosslab.jandi.app.network.client.privatetopic.IGroupApiAuth;
import com.tosslab.jandi.app.network.client.privatetopic.messages.GroupMessageApiV2Client;
import com.tosslab.jandi.app.network.client.privatetopic.messages.IGroupMessageApiAuth;
import com.tosslab.jandi.app.network.client.profile.IProfileApiAuth;
import com.tosslab.jandi.app.network.client.profile.ProfileApiV2Client;
import com.tosslab.jandi.app.network.client.publictopic.ChannelApiV2Client;
import com.tosslab.jandi.app.network.client.publictopic.IChannelApiAuth;
import com.tosslab.jandi.app.network.client.publictopic.messages.ChannelMessageApiV2Client;
import com.tosslab.jandi.app.network.client.publictopic.messages.IChannelMessageApiAuth;
import com.tosslab.jandi.app.network.client.rooms.IRoomsApiAuth;
import com.tosslab.jandi.app.network.client.rooms.RoomsApiV2Client;
import com.tosslab.jandi.app.network.client.settings.AccountProfileApiV2Client;
import com.tosslab.jandi.app.network.client.settings.IAccountProfileApiAuth;
import com.tosslab.jandi.app.network.client.settings.IStarredEntityApiAuth;
import com.tosslab.jandi.app.network.client.settings.StarredEntityApiV2Client;
import com.tosslab.jandi.app.network.client.sticker.IStickerApiAuth;
import com.tosslab.jandi.app.network.client.sticker.StickerApiV2Client;
import com.tosslab.jandi.app.network.client.teams.ITeamApiAuth;
import com.tosslab.jandi.app.network.client.teams.TeamApiV2Client;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RestAdapterBuilder;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
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
import com.tosslab.jandi.app.network.models.ReqSubscibeToken;
import com.tosslab.jandi.app.network.models.ReqTeam;
import com.tosslab.jandi.app.network.models.ReqUnshareMessage;
import com.tosslab.jandi.app.network.models.ReqUpdateAnnouncementStatus;
import com.tosslab.jandi.app.network.models.ReqUpdatePrimaryEmailInfo;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ReqUpdateTopicPushSubscribe;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResAnnouncement;
import com.tosslab.jandi.app.network.models.ResChat;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResInvitationMembers;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResStarMentioned;
import com.tosslab.jandi.app.network.models.ResMessageSearch;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;
import com.tosslab.jandi.app.network.models.ResRoomInfo;
import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.network.models.ResStarred;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;
import com.tosslab.jandi.app.network.models.sticker.ReqSendSticker;

import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 6. 20..
 */
public class JacksonConvertedAuthRestApiClient implements IAccountDeviceApiAuth, IAccountEmailsApiAuth, IAccountPasswordApiAuth,
        IChatApiAuth, IDirectMessageApiAuth, IInvitationApiAuth, IMainRestApiAuth, ICommentsApiAuth, IMessageSearchApiAuth,
        IMessagesApiAuth, IGroupMessageApiAuth, IGroupApiAuth, IProfileApiAuth, IChannelMessageApiAuth, IChannelApiAuth,
        IRoomsApiAuth, IAccountProfileApiAuth, IStarredEntityApiAuth, IStickerApiAuth, ITeamApiAuth, IFileApiAuth {

    @Override
    public ResAccountInfo registerNotificationTokenByAccountDeviceApi(ReqNotificationRegister reqNotificationRegister) throws RetrofitError {
        return RestAdapterBuilder.newInstance(AccountDeviceApiV2Client.class).create().registerNotificationToken(reqNotificationRegister);
    }

    @Override
    public ResAccountInfo deleteNotificationTokenByAccountDeviceApi(ReqDeviceToken reqDeviceToken) throws RetrofitError {
        return RestAdapterBuilder.newInstance(AccountDeviceApiV2Client.class).create().deleteNotificationToken(reqDeviceToken);
    }

    @Override
    public ResAccountInfo subscribeStateNotificationByAccountDeviceApi(ReqSubscibeToken reqDeviceToken) throws RetrofitError {
        return RestAdapterBuilder.newInstance(AccountDeviceApiV2Client.class).create().subscribeStateNotification(reqDeviceToken);
    }

    @Override
    public ResCommon getNotificationBadgeByAccountDeviceApi(ReqNotificationTarget reqNotificationTarget) throws RetrofitError {
        return RestAdapterBuilder.newInstance(AccountDeviceApiV2Client.class).create().getNotificationBadge(reqNotificationTarget);
    }

    @Override
    public ResAccountInfo requestAddEmailByAccountEmailApi(ReqAccountEmail reqAccountEmail) throws RetrofitError {
        return RestAdapterBuilder.newInstance(AccountEmailsApiV2Client.class).create().requestAddEmail(reqAccountEmail);
    }

    @Override
    public ResAccountInfo confirmEmailByAccountEmailApi(ReqConfirmEmail reqConfirmEmail) throws RetrofitError {
        return RestAdapterBuilder.newInstance(AccountEmailsApiV2Client.class).create().confirmEmail(reqConfirmEmail);
    }

    @Override
    public ResAccountInfo deleteEmailByAccountEmailApi(ReqAccountEmail reqConfirmEmail) throws RetrofitError {
        return RestAdapterBuilder.newInstance(AccountEmailsApiV2Client.class).create().deleteEmail(reqConfirmEmail);
    }

    @Override
    public ResCommon changePasswordByAccountPasswordApi(ReqChangePassword reqConfirmEmail) throws RetrofitError {
        return RestAdapterBuilder.newInstance(AccountPasswordApiV2Client.class).create().changePassword(reqConfirmEmail);
    }

    @Override
    public ResAccountInfo getAccountInfoByMainRest() throws RetrofitError {
        return RestAdapterBuilder.newInstance(MainRestApiClient.class).create().getAccountInfo();
    }

    @Override
    public ResAccountInfo updatePrimaryEmailByMainRest(ReqUpdatePrimaryEmailInfo updatePrimaryEmailInfo) throws RetrofitError {
        return RestAdapterBuilder.newInstance(MainRestApiClient.class).create().updatePrimaryEmail(updatePrimaryEmailInfo);
    }

    @Override
    public ResLeftSideMenu getInfosForSideMenuByMainRest(int teamId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(MainRestApiClient.class).create().getInfosForSideMenu(teamId);
    }

    @Override
    public ResCommon setMarkerByMainRest(int entityId, ReqSetMarker reqSetMarker) throws RetrofitError {
        return RestAdapterBuilder.newInstance(MainRestApiClient.class).create().setMarker(entityId, reqSetMarker);
    }

    @Override
    public ResSearchFile searchFileByMainRest(ReqSearchFile reqSearchFile) throws RetrofitError {
        return RestAdapterBuilder.newInstance(MainRestApiClient.class).create().searchFile(reqSearchFile);
    }

    @Override
    public ResAccountInfo changeNameByAccountProfileApi(ReqProfileName reqProfileName) throws RetrofitError {
        return RestAdapterBuilder.newInstance(AccountProfileApiV2Client.class).create().changeName(reqProfileName);
    }

    @Override
    public ResAccountInfo changePrimaryEmailByAccountProfileApi(ReqAccountEmail reqAccountEmail) throws RetrofitError {
        return RestAdapterBuilder.newInstance(AccountProfileApiV2Client.class).create().changePrimaryEmail(reqAccountEmail);
    }

    @Override
    public ResCommon createChannelByChannelApi(ReqCreateTopic channel) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ChannelApiV2Client.class).create().createChannel
                (channel.teamId, channel);
    }

    @Override
    public ResCommon modifyPublicTopicNameByChannelApi(ReqCreateTopic channel, int channelId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ChannelApiV2Client.class).create()
                .modifyPublicTopicName(channel.teamId, channel, channelId);
    }

    @Override
    public ResCommon deleteTopicByChannelApi(int channelId, ReqDeleteTopic reqDeleteTopic) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ChannelApiV2Client.class).create().deleteTopic(channelId, reqDeleteTopic);
    }

    @Override
    public ResCommon joinTopicByChannelApi(int channelId, ReqDeleteTopic reqDeleteTopic) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ChannelApiV2Client.class).create().joinTopic(channelId, reqDeleteTopic);
    }

    @Override
    public ResCommon leaveTopicByChannelApi(int channelId, ReqDeleteTopic reqDeleteTopic) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ChannelApiV2Client.class).create().leaveTopic(channelId, reqDeleteTopic);
    }

    @Override
    public ResCommon invitePublicTopicByChannelApi(int channelId, ReqInviteTopicUsers reqInviteTopicUsers) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ChannelApiV2Client.class).create().invitePublicTopic(channelId, reqInviteTopicUsers);
    }

    @Override
    public ResMessages getPublicTopicMessagesByChannelMessageApi(int teamId, int channelId, int fromId, int count) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ChannelMessageApiV2Client.class).create().getPublicTopicMessages(teamId, channelId, fromId, count);
    }

    @Override
    public ResMessages getPublicTopicMessagesByChannelMessageApi(int teamId, int channelId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ChannelMessageApiV2Client.class).create().getPublicTopicMessages(teamId, channelId);
    }

    @Override
    public ResUpdateMessages getPublicTopicUpdatedMessagesByChannelMessageApi(int teamId, int channelId, int currentLinkId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ChannelMessageApiV2Client.class).create().getPublicTopicUpdatedMessages(teamId, channelId, currentLinkId);
    }

    @Override
    public ResMessages getPublicTopicUpdatedMessagesForMarkerByChannelMessageApi(int teamId, int channelId, int currentLinkId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ChannelMessageApiV2Client.class).create().getPublicTopicUpdatedMessagesForMarker(teamId, channelId, currentLinkId);
    }

    @Override
    public ResMessages getPublicTopicMarkerMessagesByChannelMessageApi(int teamId, int channelId, int currentLinkId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ChannelMessageApiV2Client.class).create().getPublicTopicMarkerMessages(teamId, channelId, currentLinkId);
    }

    @Override
    public ResCommon sendPublicTopicMessageByChannelMessageApi(int channelId, int teamId, ReqSendMessageV3 reqSendMessageV3) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ChannelMessageApiV2Client.class).create().sendPublicTopicMessage(channelId, teamId, reqSendMessageV3);
    }

    @Override
    public ResCommon modifyPublicTopicMessageByChannelMessageApi(ReqModifyMessage message, int channelId, int messageId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ChannelMessageApiV2Client.class).create().modifyPublicTopicMessage(message, channelId, messageId);
    }

    @Override
    public ResCommon deletePublicTopicMessageByChannelMessageApi(int teamId, int channelId, int messageId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ChannelMessageApiV2Client.class).create().deletePublicTopicMessage(teamId, channelId, messageId);
    }

    @Override
    public List<ResChat> getChatListByChatApi(int memberId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ChatApiV2Client.class).create().getChatList(memberId);
    }

    @Override
    public ResCommon deleteChatByChatApi(int memberId, int entityId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ChatApiV2Client.class).create().deleteChat(memberId, entityId);
    }

    @Override
    public ResCommon sendMessageCommentByCommentsApi(int messageId, int teamId, ReqSendComment reqSendComment) throws RetrofitError {
        return RestAdapterBuilder.newInstance(CommentsApiV2Client.class).create().sendMessageComment(messageId, teamId, reqSendComment);
    }

    @Override
    public ResCommon modifyMessageCommentByCommentsApi(ReqModifyComment comment, int messageId, int commentId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(CommentsApiV2Client.class).create().modifyMessageComment(comment, messageId, commentId);
    }

    @Override
    public ResCommon deleteMessageCommentByCommentsApi(int teamId, int messageId, int commentId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(CommentsApiV2Client.class).create().deleteMessageComment(teamId, messageId, commentId);
    }

    @Override
    public ResMessages getDirectMessagesByDirectMessageApi(int teamId, int userId, int fromId, int count) throws RetrofitError {
        return RestAdapterBuilder.newInstance(DirectMessageApiV2Client.class).create().getDirectMessages(teamId, userId, fromId, count);
    }

    @Override
    public ResMessages getDirectMessagesByDirectMessageApi(int teamId, int userId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(DirectMessageApiV2Client.class).create().getDirectMessages(teamId, userId);
    }

    @Override
    public ResUpdateMessages getDirectMessagesUpdatedByDirectMessageApi(int teamId, int userId, int timeAfter) throws RetrofitError {
        return RestAdapterBuilder.newInstance(DirectMessageApiV2Client.class).create().getDirectMessagesUpdated(teamId, userId, timeAfter);
    }

    @Override
    public ResMessages getDirectMessagesUpdatedForMarkerByDirectMessageApi(int teamId, int userId, int currentLinkId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(DirectMessageApiV2Client.class).create().getDirectMessagesUpdatedForMarker(teamId, userId, currentLinkId);
    }

    @Override
    public ResMessages getDirectMarkerMessagesByDirectMessageApi(int teamId, int userId, int currentLinkId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(DirectMessageApiV2Client.class).create().getDirectMarkerMessages(teamId, userId, currentLinkId);
    }

    @Override
    public ResCommon sendDirectMessageByDirectMessageApi(int userId, int teamId,
                                                         ReqSendMessageV3 reqSendMessageV3) throws RetrofitError {
        return RestAdapterBuilder.newInstance(DirectMessageApiV2Client.class).create()
                .sendDirectMessage(userId, teamId, reqSendMessageV3);
    }

    @Override
    public ResCommon modifyDirectMessageByDirectMessageApi(ReqModifyMessage message, int userId, int messageId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(DirectMessageApiV2Client.class).create().modifyDirectMessage(message, userId, messageId);
    }

    @Override
    public ResCommon deleteDirectMessageByDirectMessageApi(int teamId, int userId, int messageId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(DirectMessageApiV2Client.class).create().deleteDirectMessage(teamId, userId, messageId);
    }

    @Override
    public ResCommon createPrivateGroupByGroupApi(ReqCreateTopic group) throws RetrofitError {
        return RestAdapterBuilder.newInstance(GroupApiV2Client.class)
                .create()
                .createPrivateGroup(group.teamId, group);
    }

    @Override
    public ResCommon modifyGroupByGroupApi(ReqCreateTopic channel, int groupId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(GroupApiV2Client.class).create().modifyGroup
                (channel.teamId, channel, groupId);
    }

    @Override
    public ResCommon deleteGroupByGroupApi(int teamId, int groupId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(GroupApiV2Client.class).create().deleteGroup(teamId, groupId);
    }

    @Override
    public ResCommon leaveGroupByGroupApi(int groupId, ReqTeam team) throws RetrofitError {
        return RestAdapterBuilder.newInstance(GroupApiV2Client.class).create().leaveGroup(groupId, team);
    }

    @Override
    public ResCommon inviteGroupByGroupApi(int groupId, ReqInviteTopicUsers inviteUsers) throws RetrofitError {
        return RestAdapterBuilder.newInstance(GroupApiV2Client.class).create().inviteGroup(groupId, inviteUsers);
    }

    @Override
    public ResMessages getGroupMessagesByGroupMessageApi(int teamId, int groupId, int fromId, int count) throws RetrofitError {
        return RestAdapterBuilder.newInstance(GroupMessageApiV2Client.class).create().getGroupMessages(teamId, groupId, fromId, count);
    }

    @Override
    public ResMessages getGroupMessagesByGroupMessageApi(int teamId, int groupId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(GroupMessageApiV2Client.class).create().getGroupMessages(teamId, groupId);
    }

    @Override
    public ResUpdateMessages getGroupMessagesUpdatedByGroupMessageApi(int teamId, int groupId, int lastLinkId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(GroupMessageApiV2Client.class).create().getGroupMessagesUpdated(teamId, groupId, lastLinkId);
    }

    @Override
    public ResMessages getGroupMessagesUpdatedForMarkerByGroupMessageApi(int teamId, int groupId, int currentLinkId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(GroupMessageApiV2Client.class).create().getGroupMessagesUpdatedForMarker(teamId, groupId, currentLinkId);
    }

    @Override
    public ResMessages getGroupMarkerMessagesByGroupMessageApi(int teamId, int groupId, int currentLinkId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(GroupMessageApiV2Client.class).create().getGroupMarkerMessages(teamId, groupId, currentLinkId);
    }

    @Override
    public ResCommon sendGroupMessageByGroupMessageApi(int privateGroupId, int teamId, ReqSendMessageV3 reqSendMessageV3) throws RetrofitError {
        return RestAdapterBuilder.newInstance(GroupMessageApiV2Client.class).create().sendGroupMessage(privateGroupId, teamId, reqSendMessageV3);
    }

    @Override
    public ResCommon modifyPrivateGroupMessageByGroupMessageApi(ReqModifyMessage message, int groupId, int messageId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(GroupMessageApiV2Client.class).create().modifyPrivateGroupMessage(message, groupId, messageId);
    }

    @Override
    public ResCommon deletePrivateGroupMessageByGroupMessageApi(int teamId, int groupId, int messageId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(GroupMessageApiV2Client.class).create().deletePrivateGroupMessage(teamId, groupId, messageId);
    }

    @Override
    public ResTeamDetailInfo acceptOrDeclineInvitationByInvitationApi(String invitationId, ReqInvitationAcceptOrIgnore reqInvitationAcceptOrIgnore) throws RetrofitError {
        return RestAdapterBuilder.newInstance(InvitationApiV2Client.class).create().acceptOrDeclineInvitation(invitationId, reqInvitationAcceptOrIgnore);
    }

    @Override
    public List<ResPendingTeamInfo> getPendingTeamInfoByInvitationApi() throws RetrofitError {
        return RestAdapterBuilder.newInstance(InvitationApiV2Client.class).create().getPedingTeamInfo();
    }

    @Override
    public ResMessageSearch searchMessagesByMessageSearchApi(int teamId, String query, int page, int perPage, int writerId, int entityId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(MessagesSearchApiV2Client.class).create().searchMessages(teamId, query, page, perPage, writerId, entityId);
    }

    @Override
    public ResMessageSearch searchMessagesByEntityIdByMessageSearchApi(int teamId, String query, int page, int perPage, int entityId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(MessagesSearchApiV2Client.class).create().searchMessagesByEntityId(teamId, query, page, perPage, entityId);
    }

    @Override
    public ResMessageSearch searchMessagesByWriterIdByMessageSearchApi(int teamId, String query, int page, int perPage, int writerId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(MessagesSearchApiV2Client.class).create().searchMessagesByWriterId(teamId, query, page, perPage, writerId);
    }

    @Override
    public ResMessageSearch searchMessagesByMessageSearchApi(int teamId, String query, int page, int perPage) throws RetrofitError {
        return RestAdapterBuilder.newInstance(MessagesSearchApiV2Client.class).create().searchMessages(teamId, query, page, perPage);
    }

    @Override
    public ResFileDetail getFileDetailByMessagesApiAuth(int teamId, int messageId) {
        return RestAdapterBuilder.newInstance(MessagesApiV2Client.class).create().getFileDetail(teamId, messageId);
    }

    @Override
    public ResCommon shareMessageByMessagesApiAuth(ReqShareMessage share, int messageId) {
        return RestAdapterBuilder.newInstance(MessagesApiV2Client.class).create().shareMessage(share, messageId);
    }

    @Override
    public ResCommon unshareMessageByMessagesApiAuth(ReqUnshareMessage share, int messageId) {
        return RestAdapterBuilder.newInstance(MessagesApiV2Client.class).create().unshareMessage(share, messageId);
    }

    @Override
    public ResUpdateMessages getRoomUpdateMessageByMessagesApiAuth(int teamId, int roomId, int currentLinkId) {
        return RestAdapterBuilder.newInstance(MessagesApiV2Client.class)
                .create().getRoomUpdateMessage(teamId, roomId, currentLinkId);
    }

    @Override
    public ResLeftSideMenu.User updateMemberProfileByProfileApi(int memberId, ReqUpdateProfile reqUpdateProfile) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ProfileApiV2Client.class).create().updateMemberProfile(memberId, reqUpdateProfile);
    }

    @Override
    public ResCommon updateMemberNameByProfileApi(int memberId, ReqProfileName reqProfileName) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ProfileApiV2Client.class).create().updateMemberName(memberId, reqProfileName);
    }

    @Override
    public ResLeftSideMenu.User updateMemberEmailByProfileApi(int memberId, ReqAccountEmail reqAccountEmail) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ProfileApiV2Client.class).create().updateMemberEmail(memberId, reqAccountEmail);
    }

    @Override
    public ResRoomInfo getRoomInfoByRoomsApi(int teamId, int roomId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(RoomsApiV2Client.class).create().getRoomInfo(teamId, roomId);
    }

    @Override
    public ResCommon enableFavoriteByStarredEntityApi(ReqTeam reqTeam, int entityId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(StarredEntityApiV2Client.class).create().enableFavorite(reqTeam, entityId);
    }

    @Override
    public ResCommon disableFavoriteByStarredEntityApi(int teamId, int entityId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(StarredEntityApiV2Client.class).create().disableFavorite(teamId, entityId);
    }

    @Override
    public ResCommon sendStickerByStickerApi(ReqSendSticker reqSendSticker) throws RetrofitError {
        return RestAdapterBuilder.newInstance(StickerApiV2Client.class).create().sendSticker(reqSendSticker);
    }

    @Override
    public ResCommon sendStickerCommentByStickerApi(ReqSendSticker reqSendSticker) throws RetrofitError {
        return RestAdapterBuilder.newInstance(StickerApiV2Client.class).create().sendStickerComment(reqSendSticker);
    }

    @Override
    public ResTeamDetailInfo createNewTeamByTeamApi(ReqCreateNewTeam req) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().createNewTeam(req);
    }

    @Override
    public ResLeftSideMenu.User getMemberProfileByTeamApi(int teamId, int memberId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().getMemberProfile(teamId, memberId);
    }

    @Override
    public List<ResInvitationMembers> inviteToTeamByTeamApi(int teamId, ReqInvitationMembers invitationMembers) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().inviteToTeam(teamId, invitationMembers);
    }

    @Override
    public ResTeamDetailInfo.InviteTeam getTeamInfoByTeamApi(int teamId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().getTeamInfo(teamId);
    }

    @Override
    public ResAnnouncement getAnnouncement(int teamId, int topicId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().getAnnouncement(teamId, topicId);
    }

    @Override
    public ResCommon createAnnouncement(int teamId, int topicId, ReqCreateAnnouncement reqCreateAnnouncement) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().createAnnouncement(teamId, topicId, reqCreateAnnouncement);
    }

    @Override
    public ResCommon updateAnnouncementStatus(int teamId, int memberId, ReqUpdateAnnouncementStatus reqUpdateAnnouncementStatus) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().updateAnnouncementStatus(teamId, memberId, reqUpdateAnnouncementStatus);
    }

    @Override
    public ResCommon deleteAnnouncement(int teamId, int topicId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().deleteAnnouncement(teamId, topicId);
    }

    @Override
    public ResMessages.OriginalMessage getMessage(int teamId, int messageId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().getMessage(teamId, messageId);
    }

    @Override
    public ResCommon updateTopicPushSubscribe(int teamId, int topicId, ReqUpdateTopicPushSubscribe reqUpdateTopicPushSubscribe) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().updateTopicPushSubscribe(teamId, topicId, reqUpdateTopicPushSubscribe);
    }

    @Override
    public ResCommon deleteStickerCommentByStickerApi(int commentId, int teamId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(StickerApiV2Client.class).create().deleteStickerComment(commentId, teamId);
    }

    @Override
    public ResCommon deleteStickerByStickerApi(int messageId, int teamId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(StickerApiV2Client.class).create().deleteSticker(messageId, teamId);
    }

    @Override
    public ResCommon deleteFileByFileApi(int teamId, int fileId) {
        return RestAdapterBuilder.newInstance(FileApiV2Client.class).create().deleteFile(teamId, fileId);
    }

    @Override
    public List<ResMessages.FileMessage> searchInitImageFileByFileApi(int teamId, int roomId, int messageId, int count) {
        return RestAdapterBuilder.newInstance(FileApiV2Client.class).create().searchInitImageFile(teamId, roomId, messageId, count);
    }

    @Override
    public List<ResMessages.FileMessage> searchOldImageFileByFileApi(int teamId, int roomId, int messageId, int count) {
        return RestAdapterBuilder.newInstance(FileApiV2Client.class).create().searchOldImageFile(teamId, roomId, messageId, count);
    }

    @Override
    public List<ResMessages.FileMessage> searchNewImageFileByFileApi(int teamId, int roomId, int messageId, int count) {
        return RestAdapterBuilder.newInstance(FileApiV2Client.class).create().searchNewImageFile(teamId, roomId, messageId, count);
    }

    @Override
    public ResStarMentioned getMentionedMessagesByTeamApi(int teamId, int page, int perPage) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().getMentionedMessages(teamId, page, perPage);
    }

    @Override
    public ResStarred registStarredMessageByTeamApi(int teamId, int messageId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().registStarredMessage(teamId, messageId, new ReqDeleteTopic(0));
    }

    @Override
    public ResCommon unregistStarredMessageByTeamApi(int teamId, int messageId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().unregistStarredMessage(teamId, messageId);
    }

    @Override
    public ResStarMentioned getStarredMessagesByTeamApi(int teamId, String type, int page, int perPage) {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().getStarredMessages(teamId,
                type, page, perPage);
    }
}