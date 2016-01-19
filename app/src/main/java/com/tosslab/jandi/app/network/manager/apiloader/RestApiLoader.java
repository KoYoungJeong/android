package com.tosslab.jandi.app.network.manager.apiloader;

import com.tosslab.jandi.app.network.client.account.devices.IAccountDeviceApiLoader;
import com.tosslab.jandi.app.network.client.account.emails.IAccountEmailsApiLoader;
import com.tosslab.jandi.app.network.client.account.password.IAccountPasswordApiLoader;
import com.tosslab.jandi.app.network.client.chat.IChatApiLoader;
import com.tosslab.jandi.app.network.client.direct.message.IDirectMessageApiLoader;
import com.tosslab.jandi.app.network.client.events.IEventsApiLoader;
import com.tosslab.jandi.app.network.client.file.IFileApiLoader;
import com.tosslab.jandi.app.network.client.invitation.IInvitationApiLoader;
import com.tosslab.jandi.app.network.client.main.IMainRestApiLoader;
import com.tosslab.jandi.app.network.client.messages.IMessagesApiLoader;
import com.tosslab.jandi.app.network.client.messages.comments.ICommentsApiLoader;
import com.tosslab.jandi.app.network.client.messages.search.IMessageSearchApiLoader;
import com.tosslab.jandi.app.network.client.platform.IPlatformApiLoader;
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
import com.tosslab.jandi.app.network.client.validation.ValidationApiLoader;
import com.tosslab.jandi.app.network.manager.apiexecutor.IExecutor;
import com.tosslab.jandi.app.network.manager.restapiclient.JacksonConvertedAuthRestApiClient;
import com.tosslab.jandi.app.network.manager.restapiclient.JacksonConvertedSimpleRestApiClient;
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

/**
 * Created by tee on 15. 6. 20..
 */
public class RestApiLoader implements IAccountDeviceApiLoader, IAccountEmailsApiLoader, IAccountPasswordApiLoader, IChatApiLoader,
        IDirectMessageApiLoader, IInvitationApiLoader, IMainRestApiLoader, ICommentsApiLoader, IMessageSearchApiLoader,
        IMessagesApiLoader, IGroupMessageApiLoader, IGroupApiLoader, IProfileApiLoader, IChannelMessageApiLoader, IChannelApiLoader,
        IRoomsApiLoader, IAccountProfileApiLoader, IStarredEntityApiLoader, IStickerApiLoader, ITeamApiLoader, IFileApiLoader, IPlatformApiLoader,
        IEventsApiLoader, ValidationApiLoader {

    JacksonConvertedAuthRestApiClient authRestApiClient = new JacksonConvertedAuthRestApiClient();

    JacksonConvertedSimpleRestApiClient simpleRestApiClient = new JacksonConvertedSimpleRestApiClient();

    private RestApiLoader() {
    }

    public static RestApiLoader getInstance() {
        return new RestApiLoader();
    }

    @Override
    public IExecutor<ResAccountInfo> loadRegisterNotificationTokenByAccountDeviceApi(ReqNotificationRegister
                                                                                             reqNotificationRegister) {
        return () -> authRestApiClient.registerNotificationTokenByAccountDeviceApi(reqNotificationRegister);
    }

    @Override
    public IExecutor<ResAccountInfo> loadDeleteNotificationTokenByAccountDeviceApi(ReqDeviceToken reqDeviceToken) {
        return () -> authRestApiClient.deleteNotificationTokenByAccountDeviceApi(reqDeviceToken);
    }

    @Override
    public IExecutor<ResAccountInfo> loadSubscribeStateNotificationByAccountDeviceApi(ReqSubscibeToken reqDeviceToken) {
        return () -> authRestApiClient.subscribeStateNotificationByAccountDeviceApi(reqDeviceToken);
    }

    @Override
    public IExecutor<ResCommon> loadGetNotificationBadgeByAccountDeviceApi(ReqNotificationTarget
                                                                                   reqNotificationTarget) {
        return () -> authRestApiClient.getNotificationBadgeByAccountDeviceApi(reqNotificationTarget);
    }

    @Override
    public IExecutor<ResAccountInfo> loadRequestAddEmailByAccountEmailsApi(ReqAccountEmail reqAccountEmail) {
        return () -> authRestApiClient.requestAddEmailByAccountEmailApi(reqAccountEmail);
    }

    @Override
    public IExecutor<ResAccountInfo> loadConfirmEmailByAccountEmailsApi(ReqConfirmEmail reqConfirmEmail) {
        return () -> authRestApiClient.confirmEmailByAccountEmailApi(reqConfirmEmail);
    }

    @Override
    public IExecutor<ResAccountInfo> loadExecutorDeleteEmailByAccountEmailsApi(ReqAccountEmail reqConfirmEmail) {
        return () -> authRestApiClient.deleteEmailByAccountEmailApi(reqConfirmEmail);
    }

    @Override
    public IExecutor<ResCommon> loadChangePasswordByAccountEmailsApi(ReqChangePassword
                                                                             reqConfirmEmail) {
        return () -> authRestApiClient.changePasswordByAccountPasswordApi(reqConfirmEmail);
    }

    @Override
    public IExecutor<ResAccountInfo> loadGetAccountInfoByMainRestApi() {
        return () -> authRestApiClient.getAccountInfoByMainRest();
    }

    @Override
    public IExecutor<ResAccountInfo> loadUpdatePrimaryEmailByMainRestApi(ReqUpdatePrimaryEmailInfo updatePrimaryEmailInfo) {
        return () -> authRestApiClient.updatePrimaryEmailByMainRest(updatePrimaryEmailInfo);
    }

    @Override
    public IExecutor<ResLeftSideMenu> loadGetInfosForSideMenuByMainRestApi(long teamId) {
        return () -> authRestApiClient.getInfosForSideMenuByMainRest(teamId);
    }

    @Override
    public IExecutor<ResCommon> loadSetMarkerByMainRestApi(long entityId, ReqSetMarker reqSetMarker) {
        return () -> authRestApiClient.setMarkerByMainRest(entityId, reqSetMarker);
    }

    @Override
    public IExecutor<ResSearchFile> loadSearchFileByMainRestApi(ReqSearchFile reqSearchFile) {
        return () -> authRestApiClient.searchFileByMainRest(reqSearchFile);
    }

    @Override
    public IExecutor<ResCommon> loadChangePasswordByAccountPasswordApi(ReqChangePassword
                                                                               reqConfirmEmail) {
        return () -> authRestApiClient.changePasswordByAccountPasswordApi(reqConfirmEmail);
    }

    @Override
    public IExecutor<ResAccountInfo> loadChangeNameByAccountProfileApi(ReqProfileName reqProfileName) {
        return () -> authRestApiClient.changeNameByAccountProfileApi(reqProfileName);
    }

    @Override
    public IExecutor<ResAccountInfo> loadChangePrimaryEmailByAccountProfileApi(ReqAccountEmail reqAccountEmail) {
        return () -> authRestApiClient.changePrimaryEmailByAccountProfileApi(reqAccountEmail);
    }

    @Override
    public IExecutor<ResCommon> loadCreateChannelByChannelApi(ReqCreateTopic channel) {
        return () -> authRestApiClient.createChannelByChannelApi(channel);
    }

    @Override
    public IExecutor<ResCommon> loadModifyPublicTopicNameByChannelApi(ReqCreateTopic channel, long
            channelId) {
        return () -> authRestApiClient.modifyPublicTopicNameByChannelApi(channel, channelId);
    }

    @Override
    public IExecutor<ResCommon> loadDeleteTopicByChannelApi(long channelId, ReqDeleteTopic reqDeleteTopic) {
        return () -> authRestApiClient.deleteTopicByChannelApi(channelId, reqDeleteTopic);
    }

    @Override
    public IExecutor<ResCommon> loadJoinTopicByChannelApi(long channelId, ReqDeleteTopic reqDeleteTopic) {
        return () -> authRestApiClient.joinTopicByChannelApi(channelId, reqDeleteTopic);
    }

    @Override
    public IExecutor<ResCommon> loadLeaveTopicByChannelApi(long channelId, ReqDeleteTopic reqDeleteTopic) {
        return () -> authRestApiClient.leaveTopicByChannelApi(channelId, reqDeleteTopic);
    }

    @Override
    public IExecutor<ResCommon> loadInvitePublicTopicByChannelApi(long channelId, ReqInviteTopicUsers reqInviteTopicUsers) {
        return () -> authRestApiClient.invitePublicTopicByChannelApi(channelId, reqInviteTopicUsers);
    }

    @Override
    public IExecutor<ResMessages> loadGetPublicTopicMessagesByChannelMessageApi(long teamId, long
            channelId,
                                                                                long fromId, int count) {
        return () -> authRestApiClient.getPublicTopicMessagesByChannelMessageApi(teamId, channelId, fromId, count);
    }

    @Override
    public IExecutor<ResMessages> loadGetPublicTopicMessagesByChannelMessageApi(int teamId, int
            channelId) {
        return () -> authRestApiClient.getPublicTopicMessagesByChannelMessageApi(teamId, channelId);
    }

    @Override
    public IExecutor<ResUpdateMessages> loadGetPublicTopicUpdatedMessagesByChannelMessageApi(int teamId, int
            channelId, int currentLinkId) {
        return () -> authRestApiClient.getPublicTopicUpdatedMessagesByChannelMessageApi(teamId, channelId, currentLinkId);
    }

    @Override
    public IExecutor<ResMessages> loadGetPublicTopicUpdatedMessagesForMarkerByChannelMessageApi(long
                                                                                                        teamId,
                                                                                                long channelId, long currentLinkId) {
        return () -> authRestApiClient.getPublicTopicUpdatedMessagesForMarkerByChannelMessageApi(teamId, channelId, currentLinkId);
    }

    @Override
    public IExecutor<ResMessages> loadGetPublicTopicUpdatedMessagesForMarkerByChannelMessageApi(long teamId, long channelId, long currentLinkId, int count) {
        return () -> authRestApiClient.getPublicTopicUpdatedMessagesForMarkerByChannelMessageApi(teamId, channelId, currentLinkId, count);
    }

    @Override
    public IExecutor<ResMessages> loadGetPublicTopicMarkerMessagesByChannelMessageApi(long teamId, long
            channelId, long currentLinkId) {
        return () -> authRestApiClient.getPublicTopicMarkerMessagesByChannelMessageApi(teamId, channelId, currentLinkId);
    }

    @Override
    public IExecutor<ResCommon> loadSendPublicTopicMessageByChannelMessageApi(long channelId, long teamId, ReqSendMessageV3 reqSendMessageV3) {
        return () -> authRestApiClient.sendPublicTopicMessageByChannelMessageApi(channelId, teamId, reqSendMessageV3);
    }

    @Override
    public IExecutor<ResCommon> loadModifyPublicTopicMessageByChannelMessageApi(ReqModifyMessage message, int channelId, int messageId) {
        return () -> authRestApiClient.modifyPublicTopicMessageByChannelMessageApi(message, channelId, messageId);
    }

    @Override
    public IExecutor<ResCommon> loadDeletePublicTopicMessageByChannelMessageApi(long teamId, long channelId, long messageId) {
        return () -> authRestApiClient.deletePublicTopicMessageByChannelMessageApi(teamId, channelId, messageId);
    }

    @Override
    public IExecutor<List<ResChat>> loadGetChatListByChatApi(int memberId) {
        return () -> authRestApiClient.getChatListByChatApi(memberId);
    }

    @Override
    public IExecutor<ResCommon> loadDeleteChatByChatApi(int memberId, int entityId) {
        return () -> authRestApiClient.deleteChatByChatApi(memberId, entityId);
    }

    @Override
    public IExecutor<ResCommon> loadSendMessageCommentByCommentsApi(long messageId, long teamId, ReqSendComment reqSendComment) {
        return () -> authRestApiClient.sendMessageCommentByCommentsApi(messageId, teamId, reqSendComment);
    }

    @Override
    public IExecutor<ResCommon> loadModifyMessageCommentByCommentsApi(ReqModifyComment comment, int messageId, int commentId) {
        return () -> authRestApiClient.modifyMessageCommentByCommentsApi(comment, messageId, commentId);
    }

    @Override
    public IExecutor<ResCommon> loadDeleteMessageCommentByCommentsApi(long teamId, long messageId, long commentId) {
        return () -> authRestApiClient.deleteMessageCommentByCommentsApi(teamId, messageId, commentId);
    }

    @Override
    public IExecutor<ResMessages> loadGetDirectMessagesByDirectMessageApi(long teamId, long userId, long
            fromId,
                                                                          int count) {
        return () -> authRestApiClient.getDirectMessagesByDirectMessageApi(teamId, userId, fromId, count);
    }

    @Override
    public IExecutor<ResMessages> loadGetDirectMessagesByDirectMessageApi(int teamId, int userId) {
        return () -> authRestApiClient.getDirectMessagesByDirectMessageApi(teamId, userId);
    }

    @Override
    public IExecutor<ResUpdateMessages> loadGetDirectMessagesUpdatedByDirectMessageApi(int teamId, int
            userId, int
                                                                                               timeAfter) {
        return () -> authRestApiClient.getDirectMessagesUpdatedByDirectMessageApi(teamId, userId, timeAfter);
    }

    @Override
    public IExecutor<ResMessages> loadGetDirectMessagesUpdatedForMarkerByDirectMessageApi(long teamId, long
            userId, long currentLinkId) {
        return () -> authRestApiClient.getDirectMessagesUpdatedForMarkerByDirectMessageApi(teamId, userId, currentLinkId);
    }

    @Override
    public IExecutor<ResMessages> loadGetDirectMessagesUpdatedForMarkerByDirectMessageApi(long teamId, long userId, long currentLinkId, int count) {
        return () -> authRestApiClient.getDirectMessagesUpdatedForMarkerByDirectMessageApi(teamId, userId, currentLinkId, count);
    }

    @Override
    public IExecutor<ResMessages> loadGetDirectMarkerMessagesByDirectMessageApi(long teamId, long userId,
                                                                                long currentLinkId) {
        return () -> authRestApiClient.getDirectMarkerMessagesByDirectMessageApi(teamId, userId, currentLinkId);
    }

    @Override
    public IExecutor<ResCommon> loadSendDirectMessageByDirectMessageApi(long userId, long teamId,
                                                                        ReqSendMessageV3 reqSendMessageV3) {
        return () -> authRestApiClient.sendDirectMessageByDirectMessageApi(userId, teamId, reqSendMessageV3);
    }

    @Override
    public IExecutor<ResCommon> loadModifyDirectMessageByDirectMessageApi(ReqModifyMessage message, int userId, int messageId) {
        return () -> authRestApiClient.modifyDirectMessageByDirectMessageApi(message, userId, messageId);
    }

    @Override
    public IExecutor<ResCommon> loadDeleteDirectMessageByDirectMessageApi(long teamId, long userId, long messageId) {
        return () -> authRestApiClient.deleteDirectMessageByDirectMessageApi(teamId, userId, messageId);
    }

    @Override
    public IExecutor<ResCommon> loadCreatePrivateGroupByGroupApi(ReqCreateTopic group) {
        return () -> authRestApiClient.createPrivateGroupByGroupApi(group);
    }

    @Override
    public IExecutor<ResCommon> loadModifyGroupByGroupApi(ReqCreateTopic channel, long groupId) {
        return () -> authRestApiClient.modifyGroupByGroupApi(channel, groupId);
    }

    @Override
    public IExecutor<ResCommon> loadDeleteGroupByGroupApi(long teamId, long groupId) {
        return () -> authRestApiClient.deleteGroupByGroupApi(teamId, groupId);
    }

    @Override
    public IExecutor<ResCommon> loadLeaveGroupByGroupApi(long groupId, ReqTeam team) {
        return () -> authRestApiClient.leaveGroupByGroupApi(groupId, team);
    }

    @Override
    public IExecutor<ResCommon> loadInviteGroupByGroupApi(long groupId, ReqInviteTopicUsers inviteUsers) {
        return () -> authRestApiClient.inviteGroupByGroupApi(groupId, inviteUsers);
    }

    @Override
    public IExecutor<ResMessages> loadGetGroupMessagesByGroupMessageApi(long teamId, long groupId,
                                                                        long fromId,
                                                                        int count) {
        return () -> authRestApiClient.getGroupMessagesByGroupMessageApi(teamId, groupId, fromId, count);
    }

    @Override
    public IExecutor<ResMessages> loadGetGroupMessagesByGroupMessageApi(int teamId, int groupId) {
        return () -> authRestApiClient.getGroupMessagesByGroupMessageApi(teamId, groupId);
    }

    @Override
    public IExecutor<ResUpdateMessages> loadGetGroupMessagesUpdatedByGroupMessageApi(int teamId, int groupId,
                                                                                     int lastLinkId) {
        return () -> authRestApiClient.getGroupMessagesUpdatedByGroupMessageApi(teamId, groupId, lastLinkId);
    }

    @Override
    public IExecutor<ResMessages> loadGetGroupMessagesUpdatedForMarkerByGroupMessageApi(long teamId,
                                                                                        long groupId,
                                                                                        long currentLinkId) {
        return () -> authRestApiClient.getGroupMessagesUpdatedForMarkerByGroupMessageApi(teamId, groupId, currentLinkId);
    }

    @Override
    public IExecutor<ResMessages> loadGetGroupMessagesUpdatedForMarkerByGroupMessageApi(long teamId, long groupId, long currentLinkId, int count) {
        return () -> authRestApiClient.getGroupMessagesUpdatedForMarkerByGroupMessageApi(teamId, groupId, currentLinkId, count);
    }

    @Override
    public IExecutor<ResMessages> loadGetGroupMarkerMessagesByGroupMessageApi(long teamId, long groupId,
                                                                              long currentLinkId) {
        return () -> authRestApiClient.getGroupMarkerMessagesByGroupMessageApi(teamId, groupId, currentLinkId);
    }

    @Override
    public IExecutor<ResCommon> loadSendGroupMessageByGroupMessageApi(long privateGroupId, long teamId, ReqSendMessageV3 reqSendMessageV3) {
        return () -> authRestApiClient.sendGroupMessageByGroupMessageApi(privateGroupId, teamId, reqSendMessageV3);
    }

    @Override
    public IExecutor<ResCommon> loadModifyPrivateGroupMessageByGroupMessageApi(ReqModifyMessage message, int groupId, int messageId) {
        return () -> authRestApiClient.modifyPrivateGroupMessageByGroupMessageApi(message, groupId, messageId);
    }

    @Override
    public IExecutor<ResCommon> loadDeletePrivateGroupMessageByGroupMessageApi(long teamId, long groupId, long messageId) {
        return () -> authRestApiClient.deletePrivateGroupMessageByGroupMessageApi(teamId, groupId, messageId);
    }


    @Override
    public IExecutor<ResLeftSideMenu.User> loadUpdateMemberProfileByProfileApi(long memberId, ReqUpdateProfile
            reqUpdateProfile) {
        return () -> authRestApiClient.updateMemberProfileByProfileApi(memberId, reqUpdateProfile);
    }

    @Override
    public IExecutor<ResCommon> loadUpdateMemberNameByProfileApi(long memberId, ReqProfileName
            reqProfileName) {
        return () -> authRestApiClient.updateMemberNameByProfileApi(memberId, reqProfileName);
    }

    @Override
    public IExecutor<ResLeftSideMenu.User> loadUpdateMemberEmailByProfileApi(long memberId,
                                                                             ReqAccountEmail reqAccountEmail) {
        return () -> authRestApiClient.updateMemberEmailByProfileApi(memberId, reqAccountEmail);
    }

    @Override
    public IExecutor<ResTeamDetailInfo> loadAcceptOrDeclineInvitationByInvitationApi(String invitationId,
                                                                                     ReqInvitationAcceptOrIgnore reqInvitationAcceptOrIgnore) {
        return () -> authRestApiClient.acceptOrDeclineInvitationByInvitationApi(invitationId, reqInvitationAcceptOrIgnore);
    }

    @Override
    public IExecutor<List<ResPendingTeamInfo>> loadGetPendingTeamInfoByInvitationApi() {
        return () -> authRestApiClient.getPendingTeamInfoByInvitationApi();
    }

    @Override
    public IExecutor<ResMessageSearch> loadSearchMessagesByMessageSearchApi(int teamId, String query, int
            page,
                                                                            int perPage, int writerId, int entityId) {
        return () -> authRestApiClient.searchMessagesByMessageSearchApi(teamId, query, page, perPage, writerId, entityId);
    }

    @Override
    public IExecutor<ResMessageSearch> loadSearchMessagesByEntityIdByMessageSearchApi(int teamId, String query,
                                                                                      int page, int perPage, int entityId) {
        return () -> authRestApiClient.searchMessagesByEntityIdByMessageSearchApi(teamId, query, page, perPage, entityId);
    }

    @Override
    public IExecutor<ResMessageSearch> loadSearchMessagesByWriterIdByMessageSearchApi(int teamId, String query,
                                                                                      int page, int perPage, int writerId) {
        return () -> authRestApiClient.searchMessagesByWriterIdByMessageSearchApi(teamId, query, page, perPage, writerId);
    }

    @Override
    public IExecutor<ResMessageSearch> loadSearchMessagesByMessageSearchApi(int teamId,
                                                                            String query,
                                                                            int page, int perPage) {
        return () -> authRestApiClient.searchMessagesByMessageSearchApi(teamId, query, page, perPage);
    }

    @Override
    public IExecutor<ResFileDetail> loadGetFileDetailByMessagesApiAuth(long teamId, long messageId) {
        return () -> authRestApiClient.getFileDetailByMessagesApiAuth(teamId, messageId);
    }

    @Override
    public IExecutor<ResCommon> loadShareMessageByMessagesApiAuth(ReqShareMessage share, long
            messageId) {
        return () -> authRestApiClient.shareMessageByMessagesApiAuth(share, messageId);
    }

    @Override
    public IExecutor<ResCommon> loadUnshareMessageByMessagesApiAuth(ReqUnshareMessage share, long
            messageId) {
        return () -> authRestApiClient.unshareMessageByMessagesApiAuth(share, messageId);
    }

    @Override
    public IExecutor<List<ResMessages.Link>> getRoomUpdateMessageByMessagesApiAuth(long teamId, long roomId, long currentLinkId) {
        return () -> authRestApiClient.getRoomUpdateMessageByMessagesApiAuth(teamId, roomId, currentLinkId);
    }

    @Override
    public IExecutor<ResRoomInfo> loadGetRoomInfoByRoomsApi(long teamId, long roomId) {
        return () -> authRestApiClient.getRoomInfoByRoomsApi(teamId, roomId);
    }

    @Override
    public IExecutor<ResCommon> loadEnableFavoriteByStarredEntityApi(ReqTeam reqTeam, long entityId) {
        return () -> authRestApiClient.enableFavoriteByStarredEntityApi(reqTeam, entityId);
    }

    @Override
    public IExecutor<ResCommon> loadDisableFavoriteByStarredEntityApi(long teamId, long entityId) {
        return () -> authRestApiClient.disableFavoriteByStarredEntityApi(teamId, entityId);
    }

    @Override
    public IExecutor<ResCommon> loadSendStickerByStickerApi(ReqSendSticker reqSendSticker) {
        return () -> authRestApiClient.sendStickerByStickerApi(reqSendSticker);
    }

    @Override
    public IExecutor<ResCommon> loadSendStickerCommentByStickerApi(ReqSendSticker reqSendSticker) {
        return () -> authRestApiClient.sendStickerCommentByStickerApi(reqSendSticker);
    }

    @Override
    public IExecutor<ResTeamDetailInfo> loadCreateNewTeamByTeamApi(ReqCreateNewTeam req) {
        return () -> authRestApiClient.createNewTeamByTeamApi(req);
    }

    @Override
    public IExecutor<ResLeftSideMenu.User> loadGetMemberProfileByTeamApi(long teamId, long memberId) {
        return () -> authRestApiClient.getMemberProfileByTeamApi(teamId, memberId);
    }

    @Override
    public IExecutor<List<ResInvitationMembers>> loadInviteToTeamByTeamApi(int teamId, ReqInvitationMembers
            invitationMembers) {
        return () -> authRestApiClient.inviteToTeamByTeamApi(teamId, invitationMembers);
    }

    @Override
    public IExecutor<ResTeamDetailInfo.InviteTeam> loadGetTeamInfoByTeamApi(long teamId) {
        return () -> authRestApiClient.getTeamInfoByTeamApi(teamId);
    }

    @Override
    public IExecutor<ResAnnouncement> loadGetAnnouncement(int teamId, int topicId) {
        return () -> authRestApiClient.getAnnouncement(teamId, topicId);
    }

    @Override
    public IExecutor<ResCommon> loadCreateAnnouncement(int teamId, int topicId, ReqCreateAnnouncement reqCreateAnnouncement) {
        return () -> authRestApiClient.createAnnouncement(teamId, topicId, reqCreateAnnouncement);
    }

    @Override
    public IExecutor<ResCommon> loadUpdateAnnouncementStatus(int teamId, int memberId, ReqUpdateAnnouncementStatus reqUpdateAnnouncementStatus) {
        return () -> authRestApiClient.updateAnnouncementStatus(teamId, memberId, reqUpdateAnnouncementStatus);
    }

    @Override
    public IExecutor<ResCommon> loadDeleteAnnouncement(int teamId, int topicId) {
        return () -> authRestApiClient.deleteAnnouncement(teamId, topicId);
    }

    @Override
    public IExecutor<ResMessages.OriginalMessage> loadGetMessage(long teamId, long topicId) {
        return () -> authRestApiClient.getMessage(teamId, topicId);
    }

    @Override
    public IExecutor<ResCommon> loadUpdateTopicPushSubscribe(int teamId, int topicId, ReqUpdateTopicPushSubscribe reqUpdateTopicPushSubscribe) {
        return () -> authRestApiClient.updateTopicPushSubscribe(teamId, topicId, reqUpdateTopicPushSubscribe);
    }

    @Override
    public IExecutor<ResCommon> loadResetPasswordByAccountPasswordApi(ReqAccountEmail
                                                                              reqAccountEmail) {
        return () -> simpleRestApiClient.resetPasswordByAccountPasswordApi(reqAccountEmail);
    }

    @Override
    public IExecutor<ResConfig> loadGetConfigByMainRestApi() {
        return () -> simpleRestApiClient.getConfigByMainRest();
    }

    @Override
    public IExecutor<ResMyTeam> loadGetTeamIdByMainRestApi(String userEmail) {
        return () -> simpleRestApiClient.getTeamIdByMainRest(userEmail);
    }

    @Override
    public IExecutor<ResAccessToken> loadGetAccessTokenByMainRestApi(ReqAccessToken login) {
        return () -> simpleRestApiClient.getAccessTokenByMainRest(login);
    }

    @Override
    public IExecutor<ResCommon> loadSignUpAccountByMainRestApi(ReqSignUpInfo signUpInfo) {
        return () -> simpleRestApiClient.signUpAccountByMainRest(signUpInfo);
    }

    @Override
    public IExecutor<ResAccountActivate> loadActivateAccountByMainRestApi(ReqAccountActivate
                                                                                  reqAccountActivate) {
        return () -> simpleRestApiClient.activateAccountByMainRest(reqAccountActivate);
    }

    @Override
    public IExecutor<ResCommon> loadAccountVerificationByMainRestApi(ReqAccountVerification reqAccountVerification) {
        return () -> simpleRestApiClient.accountVerificationByMainRest(reqAccountVerification);
    }

    @Override
    public IExecutor<ResCommon> loadDeleteStickerCommentByStickerApi(long commentId, long teamId) {
        return () -> authRestApiClient.deleteStickerCommentByStickerApi(commentId, teamId);
    }

    @Override
    public IExecutor<ResCommon> loadDeleteStickerByStickerApi(long messageId, long teamId) {
        return () -> authRestApiClient.deleteStickerByStickerApi(messageId, teamId);
    }

    @Override
    public IExecutor<ResCommon> loaderDeleteFileByFileApi(long teamId, long fileId) {
        return () -> authRestApiClient.deleteFileByFileApi(teamId, fileId);
    }

    @Override
    public IExecutor<List<ResMessages.FileMessage>> loaderSearchInitImageFileByFileApi(int teamId, int roomId, int messageId, int count) {
        return () -> authRestApiClient.searchInitImageFileByFileApi(teamId, roomId, messageId, count);
    }

    @Override
    public IExecutor<List<ResMessages.FileMessage>> loaderSearchOldImageFileByFileApi(int teamId, int roomId, int messageId, int count) {
        return () -> authRestApiClient.searchOldImageFileByFileApi(teamId, roomId, messageId, count);
    }

    @Override
    public IExecutor<List<ResMessages.FileMessage>> loaderSearchNewImageFileByFileApi(int teamId, int roomId, int messageId, int count) {
        return () -> authRestApiClient.searchNewImageFileByFileApi(teamId, roomId, messageId, count);
    }

    @Override
    public IExecutor<ResStarMentioned> loadGetMentionedMessagesByTeamApi(int teamId, Integer messageId, int count) {
        return () -> authRestApiClient.getMentionedMessagesByTeamApi(teamId, messageId, count);
    }

    @Override
    public IExecutor<StarMentionedMessageObject> loadRegistStarredMessageByTeamApi(long teamId, long messageId) {
        return () -> authRestApiClient.registStarredMessageByTeamApi(teamId, messageId);
    }

    @Override
    public IExecutor<ResCommon> loadUnregistStarredMessageByTeamApi(long teamId, long messageId) {
        return () -> authRestApiClient.unregistStarredMessageByTeamApi(teamId, messageId);
    }

    @Override
    public IExecutor<ResStarMentioned> loadGetStarredMessagesByTeamApi(int teamId, Integer messageId, int count, String type) {
        return () -> authRestApiClient.getStarredMessagesByTeamApi(teamId, messageId, count, type);
    }

    @Override
    public IExecutor<ResCommon> loadUpdatePlatformStatus(ReqUpdatePlatformStatus reqUpdatePlatformStatus) {
        return () -> authRestApiClient.updatePlatformStatus(reqUpdatePlatformStatus);
    }

    @Override
    public IExecutor<ResCreateFolder> loadCreateFolderByTeamApi(int teamId, ReqCreateFolder reqCreateFolder) {
        return () -> authRestApiClient.createFolderByTeamApi(teamId, reqCreateFolder);
    }

    @Override
    public IExecutor<ResCommon> loadDeleteFolderByTeamApi(int teamId, int folderId) {
        return () -> authRestApiClient.deleteFolderByTeamApi(teamId, folderId);
    }

    @Override
    public IExecutor<ResUpdateFolder> loadUpdateFolderByTeamApi(int teamId, int folderId, ReqUpdateFolder reqUpdateFolder) {
        return () -> authRestApiClient.updateFolderByTeamApi(teamId, folderId, reqUpdateFolder);
    }

    @Override
    public IExecutor<List<ResFolder>> loadGetFoldersByTeamApi(int teamId) {
        return () -> authRestApiClient.getFoldersByTeamApi(teamId);
    }

    @Override
    public IExecutor<List<ResFolderItem>> loadGetFolderItemsByTeamApi(int teamId) {
        return () -> authRestApiClient.getFolderItemsByTeamApi(teamId);
    }

    @Override
    public IExecutor<ResRegistFolderItem> loadRegistFolderItemByTeamApi(int teamId, int folderId, ReqRegistFolderItem reqRegistFolderItem) {
        return () -> authRestApiClient.registFolderItemByTeamApi(teamId, folderId, reqRegistFolderItem);
    }

    @Override
    public IExecutor<ResCommon> loadDeleteFolderItemByTeamApi(int teamId, int folderId, int itemId) {
        return () -> authRestApiClient.deleteFolderItemByTeamApi(teamId, folderId, itemId);
    }

    @Override
    public IExecutor<ResCommon> loadKickUserFromTopic(int teamId, int topicId, ReqMember member) {
        return () -> authRestApiClient.kickUserFromTopic(teamId, topicId, member);
    }

    @Override
    public IExecutor<ResCommon> loadAssignToTopicOwner(int teamId, int topicId, ReqOwner owner) {
        return () -> authRestApiClient.assignToTopicOwner(teamId, topicId, owner);
    }

    @Override
    public IExecutor<ResMessages.FileMessage> loadEnableFileExternalLink(long teamId, long fileId) {
        return () -> authRestApiClient.enableFileExternalLink(teamId, fileId);
    }

    @Override
    public IExecutor<ResMessages.FileMessage> loadDisableFileExternalLink(long teamId, long fileId) {
        return () -> authRestApiClient.disableFileExternalLink(teamId, fileId);
    }

    @Override
    public IExecutor<ResEventHistory> loadGetEventHistory(long ts, long memberId, String eventType, Integer size) {
        return () -> authRestApiClient.getEventHistory(ts, memberId, eventType, size);
    }

    @Override
    public IExecutor<ResValidation> loadValidDomain(String domain) {
        return () -> authRestApiClient.validDomain(domain);
    }

    @Override
    public IExecutor<ResAvatarsInfo> loadGetAvartarsInfo() {
        return () -> authRestApiClient.getAvartarsInfo();
    }
}