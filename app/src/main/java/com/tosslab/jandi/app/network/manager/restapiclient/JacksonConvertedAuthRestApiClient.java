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
import com.tosslab.jandi.app.network.client.events.EventsApiV2Client;
import com.tosslab.jandi.app.network.client.events.IEventsApiAuth;
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
import com.tosslab.jandi.app.network.client.platform.IPlatformApiAuth;
import com.tosslab.jandi.app.network.client.platform.PlatformApiV2Client;
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
import com.tosslab.jandi.app.network.client.validation.ValidationApi;
import com.tosslab.jandi.app.network.client.validation.ValidationApiAuth;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RestAdapterBuilder;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
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
import com.tosslab.jandi.app.network.models.ReqNull;
import com.tosslab.jandi.app.network.models.ReqOwner;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ReqRegistFolderItem;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ReqSendComment;
import com.tosslab.jandi.app.network.models.ReqSendMessageV3;
import com.tosslab.jandi.app.network.models.ReqSetMarker;
import com.tosslab.jandi.app.network.models.ReqShareMessage;
import com.tosslab.jandi.app.network.models.ReqSubscibeToken;
import com.tosslab.jandi.app.network.models.ReqTeam;
import com.tosslab.jandi.app.network.models.ReqUnshareMessage;
import com.tosslab.jandi.app.network.models.ReqUpdateAnnouncementStatus;
import com.tosslab.jandi.app.network.models.ReqUpdateFolder;
import com.tosslab.jandi.app.network.models.ReqUpdatePlatformStatus;
import com.tosslab.jandi.app.network.models.ReqUpdatePrimaryEmailInfo;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ReqUpdateTopicPushSubscribe;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResAnnouncement;
import com.tosslab.jandi.app.network.models.ResAvatarsInfo;
import com.tosslab.jandi.app.network.models.ResChat;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResCreateFolder;
import com.tosslab.jandi.app.network.models.ResEventHistory;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResFolder;
import com.tosslab.jandi.app.network.models.ResFolderItem;
import com.tosslab.jandi.app.network.models.ResInvitationMembers;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessageSearch;
import com.tosslab.jandi.app.network.models.ResMessages;
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

/**
 * Created by tee on 15. 6. 20..
 */
public class JacksonConvertedAuthRestApiClient implements IAccountDeviceApiAuth, IAccountEmailsApiAuth, IAccountPasswordApiAuth,
        IChatApiAuth, IDirectMessageApiAuth, IInvitationApiAuth, IMainRestApiAuth, ICommentsApiAuth, IMessageSearchApiAuth,
        IMessagesApiAuth, IGroupMessageApiAuth, IGroupApiAuth, IProfileApiAuth, IChannelMessageApiAuth, IChannelApiAuth,
        IRoomsApiAuth, IAccountProfileApiAuth, IStarredEntityApiAuth, IStickerApiAuth, ITeamApiAuth, IFileApiAuth, IPlatformApiAuth,
        IEventsApiAuth, ValidationApiAuth {

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
    public ResLeftSideMenu getInfosForSideMenuByMainRest(long teamId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(MainRestApiClient.class).create().getInfosForSideMenu(teamId);
    }

    @Override
    public ResCommon setMarkerByMainRest(long entityId, ReqSetMarker reqSetMarker) throws RetrofitError {
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
    public ResCommon modifyPublicTopicNameByChannelApi(ReqModifyTopicName channel, long channelId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ChannelApiV2Client.class).create()
                .modifyPublicTopicName(channel.teamId, channel, channelId);
    }

    @Override
    public ResCommon modifyPublicTopicDescriptionByChannelApi(ReqModifyTopicDescription description, long channelId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ChannelApiV2Client.class).create()
                .modifyPublicTopicDescription(description.teamId, description, channelId);
    }

    @Override
    public ResCommon modifyPublicTopicAutoJoinByChannelApi(ReqModifyTopicAutoJoin topicAutoJoin, long channelId) {
        return RestAdapterBuilder.newInstance(ChannelApiV2Client.class).create()
                .modifyPublicTopicAutoJoin(topicAutoJoin.teamId, topicAutoJoin, channelId);
    }

    @Override
    public ResCommon deleteTopicByChannelApi(long channelId, ReqDeleteTopic reqDeleteTopic) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ChannelApiV2Client.class).create().deleteTopic(channelId, reqDeleteTopic);
    }

    @Override
    public ResCommon joinTopicByChannelApi(long channelId, ReqDeleteTopic reqDeleteTopic) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ChannelApiV2Client.class).create().joinTopic(channelId, reqDeleteTopic);
    }

    @Override
    public ResCommon leaveTopicByChannelApi(long channelId, ReqDeleteTopic reqDeleteTopic) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ChannelApiV2Client.class).create().leaveTopic(channelId, reqDeleteTopic);
    }

    @Override
    public ResCommon invitePublicTopicByChannelApi(long channelId, ReqInviteTopicUsers reqInviteTopicUsers) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ChannelApiV2Client.class).create().invitePublicTopic(channelId, reqInviteTopicUsers);
    }

    @Override
    public ResMessages getPublicTopicMessagesByChannelMessageApi(long teamId, long channelId, long fromId, int count) throws RetrofitError {
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
    public ResMessages getPublicTopicUpdatedMessagesForMarkerByChannelMessageApi(long teamId, long channelId, long currentLinkId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ChannelMessageApiV2Client.class).create().getPublicTopicUpdatedMessagesForMarker(teamId, channelId, currentLinkId);
    }

    @Override
    public ResMessages getPublicTopicUpdatedMessagesForMarkerByChannelMessageApi(long teamId, long channelId, long currentLinkId, int count) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ChannelMessageApiV2Client.class).create().getPublicTopicUpdatedMessagesForMarker(teamId, channelId, currentLinkId, count);
    }

    @Override
    public ResMessages getPublicTopicMarkerMessagesByChannelMessageApi(long teamId, long channelId, long currentLinkId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ChannelMessageApiV2Client.class).create().getPublicTopicMarkerMessages(teamId, channelId, currentLinkId);
    }

    @Override
    public ResCommon sendPublicTopicMessageByChannelMessageApi(long channelId, long teamId, ReqSendMessageV3 reqSendMessageV3) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ChannelMessageApiV2Client.class).create().sendPublicTopicMessage(channelId, teamId, reqSendMessageV3);
    }

    @Override
    public ResCommon modifyPublicTopicMessageByChannelMessageApi(ReqModifyMessage message, int channelId, int messageId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ChannelMessageApiV2Client.class).create().modifyPublicTopicMessage(message, channelId, messageId);
    }

    @Override
    public ResCommon deletePublicTopicMessageByChannelMessageApi(long teamId, long channelId, long messageId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ChannelMessageApiV2Client.class).create().deletePublicTopicMessage(teamId, channelId, messageId);
    }

    @Override
    public List<ResChat> getChatListByChatApi(long memberId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ChatApiV2Client.class).create().getChatList(memberId);
    }

    @Override
    public ResCommon deleteChatByChatApi(long memberId, long entityId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ChatApiV2Client.class).create().deleteChat(memberId, entityId);
    }

    @Override
    public ResCommon sendMessageCommentByCommentsApi(long messageId, long teamId, ReqSendComment reqSendComment) throws RetrofitError {
        return RestAdapterBuilder.newInstance(CommentsApiV2Client.class).create().sendMessageComment(messageId, teamId, reqSendComment);
    }

    @Override
    public ResCommon modifyMessageCommentByCommentsApi(ReqModifyComment comment, int messageId, int commentId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(CommentsApiV2Client.class).create().modifyMessageComment(comment, messageId, commentId);
    }

    @Override
    public ResCommon deleteMessageCommentByCommentsApi(long teamId, long messageId, long commentId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(CommentsApiV2Client.class).create().deleteMessageComment(teamId, messageId, commentId);
    }

    @Override
    public ResMessages getDirectMessagesByDirectMessageApi(long teamId, long userId, long fromId, int count) throws RetrofitError {
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
    public ResMessages getDirectMessagesUpdatedForMarkerByDirectMessageApi(long teamId, long userId, long currentLinkId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(DirectMessageApiV2Client.class).create().getDirectMessagesUpdatedForMarker(teamId, userId, currentLinkId);
    }

    @Override
    public ResMessages getDirectMessagesUpdatedForMarkerByDirectMessageApi(long teamId, long userId, long currentLinkId, int count) throws RetrofitError {
        return RestAdapterBuilder.newInstance(DirectMessageApiV2Client.class).create().getDirectMessagesUpdatedForMarker(teamId, userId, currentLinkId, count);
    }

    @Override
    public ResMessages getDirectMarkerMessagesByDirectMessageApi(long teamId, long userId, long currentLinkId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(DirectMessageApiV2Client.class).create().getDirectMarkerMessages(teamId, userId, currentLinkId);
    }

    @Override
    public ResCommon sendDirectMessageByDirectMessageApi(long userId, long teamId,
                                                         ReqSendMessageV3 reqSendMessageV3) throws RetrofitError {
        return RestAdapterBuilder.newInstance(DirectMessageApiV2Client.class).create()
                .sendDirectMessage(userId, teamId, reqSendMessageV3);
    }

    @Override
    public ResCommon modifyDirectMessageByDirectMessageApi(ReqModifyMessage message, int userId, int messageId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(DirectMessageApiV2Client.class).create().modifyDirectMessage(message, userId, messageId);
    }

    @Override
    public ResCommon deleteDirectMessageByDirectMessageApi(long teamId, long userId, long messageId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(DirectMessageApiV2Client.class).create().deleteDirectMessage(teamId, userId, messageId);
    }

    @Override
    public ResCommon createPrivateGroupByGroupApi(ReqCreateTopic group) throws RetrofitError {
        return RestAdapterBuilder.newInstance(GroupApiV2Client.class)
                .create()
                .createPrivateGroup(group.teamId, group);
    }

    @Override
    public ResCommon modifyGroupNameByGroupApi(ReqModifyTopicName channel, long groupId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(GroupApiV2Client.class).create().modifyGroupName
                (channel.teamId, channel, groupId);
    }

    @Override
    public ResCommon deleteGroupByGroupApi(long teamId, long groupId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(GroupApiV2Client.class).create().deleteGroup(teamId, groupId);
    }

    @Override
    public ResCommon leaveGroupByGroupApi(long groupId, ReqTeam team) throws RetrofitError {
        return RestAdapterBuilder.newInstance(GroupApiV2Client.class).create().leaveGroup(groupId, team);
    }

    @Override
    public ResCommon inviteGroupByGroupApi(long groupId, ReqInviteTopicUsers inviteUsers) throws RetrofitError {
        return RestAdapterBuilder.newInstance(GroupApiV2Client.class).create().inviteGroup(groupId, inviteUsers);
    }

    @Override
    public ResCommon modifyGroupDescriptionByGroupApi(ReqModifyTopicDescription description, long entityId) {
        return RestAdapterBuilder.newInstance(GroupApiV2Client.class).create().modifyGroupDescription(description.teamId, description, entityId);
    }

    @Override
    public ResMessages getGroupMessagesByGroupMessageApi(long teamId, long groupId, long fromId, int count) throws RetrofitError {
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
    public ResMessages getGroupMessagesUpdatedForMarkerByGroupMessageApi(long teamId, long groupId, long currentLinkId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(GroupMessageApiV2Client.class).create().getGroupMessagesUpdatedForMarker(teamId, groupId, currentLinkId);
    }

    @Override
    public ResMessages getGroupMessagesUpdatedForMarkerByGroupMessageApi(long teamId, long groupId, long currentLinkId, int count) throws RetrofitError {
        return RestAdapterBuilder.newInstance(GroupMessageApiV2Client.class).create().getGroupMessagesUpdatedForMarker(teamId, groupId, currentLinkId, count);
    }

    @Override
    public ResMessages getGroupMarkerMessagesByGroupMessageApi(long teamId, long groupId, long currentLinkId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(GroupMessageApiV2Client.class).create().getGroupMarkerMessages(teamId, groupId, currentLinkId);
    }

    @Override
    public ResCommon sendGroupMessageByGroupMessageApi(long privateGroupId, long teamId, ReqSendMessageV3 reqSendMessageV3) throws RetrofitError {
        return RestAdapterBuilder.newInstance(GroupMessageApiV2Client.class).create().sendGroupMessage(privateGroupId, teamId, reqSendMessageV3);
    }

    @Override
    public ResCommon modifyPrivateGroupMessageByGroupMessageApi(ReqModifyMessage message, int groupId, int messageId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(GroupMessageApiV2Client.class).create().modifyPrivateGroupMessage(message, groupId, messageId);
    }

    @Override
    public ResCommon deletePrivateGroupMessageByGroupMessageApi(long teamId, long groupId, long messageId) throws RetrofitError {
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
    public ResMessageSearch searchMessagesByMessageSearchApi(long teamId, String query, int page, int perPage, long writerId, long entityId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(MessagesSearchApiV2Client.class).create().searchMessages(teamId, query, page, perPage, writerId, entityId);
    }

    @Override
    public ResMessageSearch searchMessagesByEntityIdByMessageSearchApi(long teamId, String query, int page, int perPage, long entityId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(MessagesSearchApiV2Client.class).create().searchMessagesByEntityId(teamId, query, page, perPage, entityId);
    }

    @Override
    public ResMessageSearch searchMessagesByWriterIdByMessageSearchApi(long teamId, String query, int page, int perPage, long writerId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(MessagesSearchApiV2Client.class).create().searchMessagesByWriterId(teamId, query, page, perPage, writerId);
    }

    @Override
    public ResMessageSearch searchMessagesByMessageSearchApi(long teamId, String query, int page, int perPage) throws RetrofitError {
        return RestAdapterBuilder.newInstance(MessagesSearchApiV2Client.class).create().searchMessages(teamId, query, page, perPage);
    }

    @Override
    public ResFileDetail getFileDetailByMessagesApiAuth(long teamId, long messageId) {
        return RestAdapterBuilder.newInstance(MessagesApiV2Client.class).create().getFileDetail(teamId, messageId);
    }

    @Override
    public ResCommon shareMessageByMessagesApiAuth(ReqShareMessage share, long messageId) {
        return RestAdapterBuilder.newInstance(MessagesApiV2Client.class).create().shareMessage(share, messageId);
    }

    @Override
    public ResCommon unshareMessageByMessagesApiAuth(ReqUnshareMessage share, long messageId) {
        return RestAdapterBuilder.newInstance(MessagesApiV2Client.class).create().unshareMessage(share, messageId);
    }

    @Override
    public List<ResMessages.Link> getRoomUpdateMessageByMessagesApiAuth(long teamId, long roomId, long currentLinkId) {
        return RestAdapterBuilder.newInstance(MessagesApiV2Client.class)
                .create().getRoomUpdateMessage(teamId, roomId, currentLinkId);
    }

    @Override
    public ResLeftSideMenu.User updateMemberProfileByProfileApi(long memberId, ReqUpdateProfile reqUpdateProfile) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ProfileApiV2Client.class).create().updateMemberProfile(memberId, reqUpdateProfile);
    }

    @Override
    public ResCommon updateMemberNameByProfileApi(long memberId, ReqProfileName reqProfileName) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ProfileApiV2Client.class).create().updateMemberName(memberId, reqProfileName);
    }

    @Override
    public ResLeftSideMenu.User updateMemberEmailByProfileApi(long memberId, ReqAccountEmail reqAccountEmail) throws RetrofitError {
        return RestAdapterBuilder.newInstance(ProfileApiV2Client.class).create().updateMemberEmail(memberId, reqAccountEmail);
    }

    @Override
    public ResRoomInfo getRoomInfoByRoomsApi(long teamId, long roomId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(RoomsApiV2Client.class).create().getRoomInfo(teamId, roomId);
    }

    @Override
    public ResCommon enableFavoriteByStarredEntityApi(ReqTeam reqTeam, long entityId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(StarredEntityApiV2Client.class).create().enableFavorite(reqTeam, entityId);
    }

    @Override
    public ResCommon disableFavoriteByStarredEntityApi(long teamId, long entityId) throws RetrofitError {
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
    public ResLeftSideMenu.User getMemberProfileByTeamApi(long teamId, long memberId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().getMemberProfile(teamId, memberId);
    }

    @Override
    public List<ResInvitationMembers> inviteToTeamByTeamApi(long teamId, ReqInvitationMembers invitationMembers) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().inviteToTeam(teamId, invitationMembers);
    }

    @Override
    public ResCommon cancelInvitationUserByTeamApi(long teamId, long memberId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().cancelInvitationUser(teamId, memberId);
    }

    @Override
    public ResTeamDetailInfo.InviteTeam getTeamInfoByTeamApi(long teamId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().getTeamInfo(teamId);
    }

    @Override
    public ResAnnouncement getAnnouncement(long teamId, long topicId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().getAnnouncement(teamId, topicId);
    }

    @Override
    public ResCommon createAnnouncement(long teamId, long topicId, ReqCreateAnnouncement reqCreateAnnouncement) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().createAnnouncement(teamId, topicId, reqCreateAnnouncement);
    }

    @Override
    public ResCommon updateAnnouncementStatus(long teamId, long memberId, ReqUpdateAnnouncementStatus reqUpdateAnnouncementStatus) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().updateAnnouncementStatus(teamId, memberId, reqUpdateAnnouncementStatus);
    }

    @Override
    public ResCommon deleteAnnouncement(long teamId, long topicId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().deleteAnnouncement(teamId, topicId);
    }

    @Override
    public ResMessages.OriginalMessage getMessage(long teamId, long messageId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().getMessage(teamId, messageId);
    }

    @Override
    public ResCommon updateTopicPushSubscribe(long teamId, long topicId, ReqUpdateTopicPushSubscribe reqUpdateTopicPushSubscribe) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().updateTopicPushSubscribe(teamId, topicId, reqUpdateTopicPushSubscribe);
    }

    @Override
    public ResCommon deleteStickerCommentByStickerApi(long commentId, long teamId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(StickerApiV2Client.class).create().deleteStickerComment(commentId, teamId);
    }

    @Override
    public ResCommon deleteStickerByStickerApi(long messageId, long teamId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(StickerApiV2Client.class).create().deleteSticker(messageId, teamId);
    }

    @Override
    public ResCommon deleteFileByFileApi(long teamId, long fileId) {
        return RestAdapterBuilder.newInstance(FileApiV2Client.class).create().deleteFile(teamId, fileId);
    }

    @Override
    public List<ResMessages.FileMessage> searchInitImageFileByFileApi(long teamId, long roomId, long messageId, int count) {
        return RestAdapterBuilder.newInstance(FileApiV2Client.class).create().searchInitImageFile(teamId, roomId, messageId, count);
    }

    @Override
    public List<ResMessages.FileMessage> searchOldImageFileByFileApi(long teamId, long roomId, long messageId, int count) {
        return RestAdapterBuilder.newInstance(FileApiV2Client.class).create().searchOldImageFile(teamId, roomId, messageId, count);
    }

    @Override
    public List<ResMessages.FileMessage> searchNewImageFileByFileApi(long teamId, long roomId, long messageId, int count) {
        return RestAdapterBuilder.newInstance(FileApiV2Client.class).create().searchNewImageFile(teamId, roomId, messageId, count);
    }

    @Override
    public ResStarMentioned getMentionedMessagesByTeamApi(long teamId, long messageId, int count) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().getMentionedMessages(teamId, messageId, count);
    }

    @Override
    public StarMentionedMessageObject registStarredMessageByTeamApi(long teamId, long messageId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().registStarredMessage(teamId, messageId, new ReqNull());
    }

    @Override
    public ResCommon unregistStarredMessageByTeamApi(long teamId, long messageId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().unregistStarredMessage(teamId, messageId);
    }

    @Override
    public ResStarMentioned getStarredMessagesByTeamApi(long teamId, long starredId, int count, String type) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().getStarredMessages(teamId,
                starredId, count, type);
    }

    @Override
    public ResCommon updatePlatformStatus(ReqUpdatePlatformStatus reqUpdatePlatformStatus) throws RetrofitError {
        return RestAdapterBuilder.newInstance(PlatformApiV2Client.class).create().updatePlatformStatus(reqUpdatePlatformStatus);
    }

    @Override
    public ResCreateFolder createFolderByTeamApi(long teamId, ReqCreateFolder reqCreateFolder) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().createFolder(teamId, reqCreateFolder);
    }

    @Override
    public ResCommon deleteFolderByTeamApi(long teamId, long folderId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().deleteFolder(teamId, folderId);
    }

    @Override
    public ResUpdateFolder updateFolderByTeamApi(long teamId, long folderId, ReqUpdateFolder reqUpdateFolder) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().updateFolder(teamId, folderId, reqUpdateFolder);
    }

    @Override
    public List<ResFolder> getFoldersByTeamApi(long teamId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().getFolders(teamId);

    }

    @Override
    public List<ResFolderItem> getFolderItemsByTeamApi(long teamId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().getFolderItems(teamId);
    }

    @Override
    public ResRegistFolderItem registFolderItemByTeamApi(long teamId, long folderId, ReqRegistFolderItem reqRegistFolderItem) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().registFolderItem(teamId, folderId, reqRegistFolderItem);
    }

    @Override
    public ResCommon deleteFolderItemByTeamApi(long teamId, long folderId, long itemId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().deleteFolderItem(teamId, folderId, itemId);
    }

    @Override
    public ResCommon kickUserFromTopic(long teamId, long topicId, ReqMember member) {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().kickUserFromTopic(teamId, topicId, member);
    }

    @Override
    public ResCommon assignToTopicOwner(long teamId, long topicId, ReqOwner owner) {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().assignToTopicOwner(teamId, topicId, owner);
    }

    @Override
    public ResMessages.FileMessage enableFileExternalLink(long teamId, long fileId) {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().enableFileExternalLink(teamId, fileId, new ReqNull());
    }

    @Override
    public ResMessages.FileMessage disableFileExternalLink(long teamId, long fileId) {
        return RestAdapterBuilder.newInstance(TeamApiV2Client.class).create().disableFileExternalLink(teamId, fileId);
    }

    @Override
    public ResEventHistory getEventHistory(long ts, long memberId, String eventType, Integer size) throws RetrofitError {
        return RestAdapterBuilder.newInstance(EventsApiV2Client.class).create().getEventHistory(ts, memberId, eventType, size);
    }

    @Override
    public ResValidation validDomain(String domain) {
        return RestAdapterBuilder.newInstance(ValidationApi.class).create().validDomain(domain);
    }

    @Override
    public ResAvatarsInfo getAvartarsInfo() throws RetrofitError {
        return RestAdapterBuilder.newInstance(ProfileApiV2Client.class).create().getAvartarsInfo();
    }
}