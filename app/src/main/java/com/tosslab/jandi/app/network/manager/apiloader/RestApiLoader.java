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
import com.tosslab.jandi.app.network.manager.apiexecutor.Executor;
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
    public Executor<ResAccountInfo> loadRegisterNotificationTokenByAccountDeviceApi(ReqNotificationRegister
                                                                                             reqNotificationRegister) {
        return () -> authRestApiClient.registerNotificationTokenByAccountDeviceApi(reqNotificationRegister);
    }

    @Override
    public Executor<ResAccountInfo> loadDeleteNotificationTokenByAccountDeviceApi(ReqDeviceToken reqDeviceToken) {
        return () -> authRestApiClient.deleteNotificationTokenByAccountDeviceApi(reqDeviceToken);
    }

    @Override
    public Executor<ResAccountInfo> loadSubscribeStateNotificationByAccountDeviceApi(ReqSubscibeToken reqDeviceToken) {
        return () -> authRestApiClient.subscribeStateNotificationByAccountDeviceApi(reqDeviceToken);
    }

    @Override
    public Executor<ResCommon> loadGetNotificationBadgeByAccountDeviceApi(ReqNotificationTarget
                                                                                   reqNotificationTarget) {
        return () -> authRestApiClient.getNotificationBadgeByAccountDeviceApi(reqNotificationTarget);
    }

    @Override
    public Executor<ResAccountInfo> loadRequestAddEmailByAccountEmailsApi(ReqAccountEmail reqAccountEmail) {
        return () -> authRestApiClient.requestAddEmailByAccountEmailApi(reqAccountEmail);
    }

    @Override
    public Executor<ResAccountInfo> loadConfirmEmailByAccountEmailsApi(ReqConfirmEmail reqConfirmEmail) {
        return () -> authRestApiClient.confirmEmailByAccountEmailApi(reqConfirmEmail);
    }

    @Override
    public Executor<ResAccountInfo> loadExecutorDeleteEmailByAccountEmailsApi(ReqAccountEmail reqConfirmEmail) {
        return () -> authRestApiClient.deleteEmailByAccountEmailApi(reqConfirmEmail);
    }

    @Override
    public Executor<ResCommon> loadChangePasswordByAccountEmailsApi(ReqChangePassword
                                                                             reqConfirmEmail) {
        return () -> authRestApiClient.changePasswordByAccountPasswordApi(reqConfirmEmail);
    }

    @Override
    public Executor<ResAccountInfo> loadGetAccountInfoByMainRestApi() {
        return () -> authRestApiClient.getAccountInfoByMainRest();
    }

    @Override
    public Executor<ResAccountInfo> loadUpdatePrimaryEmailByMainRestApi(ReqUpdatePrimaryEmailInfo updatePrimaryEmailInfo) {
        return () -> authRestApiClient.updatePrimaryEmailByMainRest(updatePrimaryEmailInfo);
    }

    @Override
    public Executor<ResLeftSideMenu> loadGetInfosForSideMenuByMainRestApi(long teamId) {
        return () -> authRestApiClient.getInfosForSideMenuByMainRest(teamId);
    }

    @Override
    public Executor<ResCommon> loadSetMarkerByMainRestApi(long entityId, ReqSetMarker reqSetMarker) {
        return () -> authRestApiClient.setMarkerByMainRest(entityId, reqSetMarker);
    }

    @Override
    public Executor<ResSearchFile> loadSearchFileByMainRestApi(ReqSearchFile reqSearchFile) {
        return () -> authRestApiClient.searchFileByMainRest(reqSearchFile);
    }

    @Override
    public Executor<ResCommon> loadChangePasswordByAccountPasswordApi(ReqChangePassword
                                                                               reqConfirmEmail) {
        return () -> authRestApiClient.changePasswordByAccountPasswordApi(reqConfirmEmail);
    }

    @Override
    public Executor<ResAccountInfo> loadChangeNameByAccountProfileApi(ReqProfileName reqProfileName) {
        return () -> authRestApiClient.changeNameByAccountProfileApi(reqProfileName);
    }

    @Override
    public Executor<ResAccountInfo> loadChangePrimaryEmailByAccountProfileApi(ReqAccountEmail reqAccountEmail) {
        return () -> authRestApiClient.changePrimaryEmailByAccountProfileApi(reqAccountEmail);
    }

    @Override
    public Executor<ResCommon> loadCreateChannelByChannelApi(ReqCreateTopic channel) {
        return () -> authRestApiClient.createChannelByChannelApi(channel);
    }

    @Override
    public Executor<ResCommon> loadModifyPublicTopicNameByChannelApi(ReqModifyTopicName channel, long
            channelId) {
        return () -> authRestApiClient.modifyPublicTopicNameByChannelApi(channel, channelId);
    }

    @Override
    public Executor<ResCommon> loadModifyPublicTopicDescriptionByChannelApi(ReqModifyTopicDescription description, long channelId) throws IOException {
        return () -> authRestApiClient.modifyPublicTopicDescriptionByChannelApi(description, channelId);
    }

    @Override
    public Executor<ResCommon> loadModifyPublicTopicAutoJoinByChannelApi(ReqModifyTopicAutoJoin topicAutoJoin, long channelId) throws IOException {
        return () -> authRestApiClient.modifyPublicTopicAutoJoinByChannelApi(topicAutoJoin, channelId);
    }

    @Override
    public Executor<ResCommon> loadDeleteTopicByChannelApi(long channelId, ReqDeleteTopic reqDeleteTopic) {
        return () -> authRestApiClient.deleteTopicByChannelApi(channelId, reqDeleteTopic);
    }

    @Override
    public Executor<ResCommon> loadJoinTopicByChannelApi(long channelId, ReqDeleteTopic reqDeleteTopic) {
        return () -> authRestApiClient.joinTopicByChannelApi(channelId, reqDeleteTopic);
    }

    @Override
    public Executor<ResCommon> loadLeaveTopicByChannelApi(long channelId, ReqDeleteTopic reqDeleteTopic) {
        return () -> authRestApiClient.leaveTopicByChannelApi(channelId, reqDeleteTopic);
    }

    @Override
    public Executor<ResCommon> loadInvitePublicTopicByChannelApi(long channelId, ReqInviteTopicUsers reqInviteTopicUsers) {
        return () -> authRestApiClient.invitePublicTopicByChannelApi(channelId, reqInviteTopicUsers);
    }

    @Override
    public Executor<ResMessages> loadGetPublicTopicMessagesByChannelMessageApi(long teamId, long
            channelId,
                                                                                long fromId, int count) {
        return () -> authRestApiClient.getPublicTopicMessagesByChannelMessageApi(teamId, channelId, fromId, count);
    }

    @Override
    public Executor<ResMessages> loadGetPublicTopicMessagesByChannelMessageApi(int teamId, int
            channelId) {
        return () -> authRestApiClient.getPublicTopicMessagesByChannelMessageApi(teamId, channelId);
    }

    @Override
    public Executor<ResUpdateMessages> loadGetPublicTopicUpdatedMessagesByChannelMessageApi(int teamId, int
            channelId, int currentLinkId) {
        return () -> authRestApiClient.getPublicTopicUpdatedMessagesByChannelMessageApi(teamId, channelId, currentLinkId);
    }

    @Override
    public Executor<ResMessages> loadGetPublicTopicUpdatedMessagesForMarkerByChannelMessageApi(long
                                                                                                        teamId,
                                                                                                long channelId, long currentLinkId) {
        return () -> authRestApiClient.getPublicTopicUpdatedMessagesForMarkerByChannelMessageApi(teamId, channelId, currentLinkId);
    }

    @Override
    public Executor<ResMessages> loadGetPublicTopicUpdatedMessagesForMarkerByChannelMessageApi(long teamId, long channelId, long currentLinkId, int count) {
        return () -> authRestApiClient.getPublicTopicUpdatedMessagesForMarkerByChannelMessageApi(teamId, channelId, currentLinkId, count);
    }

    @Override
    public Executor<ResMessages> loadGetPublicTopicMarkerMessagesByChannelMessageApi(long teamId, long
            channelId, long currentLinkId) {
        return () -> authRestApiClient.getPublicTopicMarkerMessagesByChannelMessageApi(teamId, channelId, currentLinkId);
    }

    @Override
    public Executor<ResCommon> loadSendPublicTopicMessageByChannelMessageApi(long channelId, long teamId, ReqSendMessageV3 reqSendMessageV3) {
        return () -> authRestApiClient.sendPublicTopicMessageByChannelMessageApi(channelId, teamId, reqSendMessageV3);
    }

    @Override
    public Executor<ResCommon> loadModifyPublicTopicMessageByChannelMessageApi(ReqModifyMessage message, int channelId, int messageId) {
        return () -> authRestApiClient.modifyPublicTopicMessageByChannelMessageApi(message, channelId, messageId);
    }

    @Override
    public Executor<ResCommon> loadDeletePublicTopicMessageByChannelMessageApi(long teamId, long channelId, long messageId) {
        return () -> authRestApiClient.deletePublicTopicMessageByChannelMessageApi(teamId, channelId, messageId);
    }

    @Override
    public Executor<List<ResChat>> loadGetChatListByChatApi(long memberId) {
        return () -> authRestApiClient.getChatListByChatApi(memberId);
    }

    @Override
    public Executor<ResCommon> loadDeleteChatByChatApi(long memberId, long entityId) {
        return () -> authRestApiClient.deleteChatByChatApi(memberId, entityId);
    }

    @Override
    public Executor<ResCommon> loadSendMessageCommentByCommentsApi(long messageId, long teamId, ReqSendComment reqSendComment) {
        return () -> authRestApiClient.sendMessageCommentByCommentsApi(messageId, teamId, reqSendComment);
    }

    @Override
    public Executor<ResCommon> loadModifyMessageCommentByCommentsApi(ReqModifyComment comment, int messageId, int commentId) {
        return () -> authRestApiClient.modifyMessageCommentByCommentsApi(comment, messageId, commentId);
    }

    @Override
    public Executor<ResCommon> loadDeleteMessageCommentByCommentsApi(long teamId, long messageId, long commentId) {
        return () -> authRestApiClient.deleteMessageCommentByCommentsApi(teamId, messageId, commentId);
    }

    @Override
    public Executor<ResMessages> loadGetDirectMessagesByDirectMessageApi(long teamId, long userId, long
            fromId,
                                                                          int count) {
        return () -> authRestApiClient.getDirectMessagesByDirectMessageApi(teamId, userId, fromId, count);
    }

    @Override
    public Executor<ResMessages> loadGetDirectMessagesByDirectMessageApi(int teamId, int userId) {
        return () -> authRestApiClient.getDirectMessagesByDirectMessageApi(teamId, userId);
    }

    @Override
    public Executor<ResUpdateMessages> loadGetDirectMessagesUpdatedByDirectMessageApi(int teamId, int
            userId, int
                                                                                               timeAfter) {
        return () -> authRestApiClient.getDirectMessagesUpdatedByDirectMessageApi(teamId, userId, timeAfter);
    }

    @Override
    public Executor<ResMessages> loadGetDirectMessagesUpdatedForMarkerByDirectMessageApi(long teamId, long
            userId, long currentLinkId) {
        return () -> authRestApiClient.getDirectMessagesUpdatedForMarkerByDirectMessageApi(teamId, userId, currentLinkId);
    }

    @Override
    public Executor<ResMessages> loadGetDirectMessagesUpdatedForMarkerByDirectMessageApi(long teamId, long userId, long currentLinkId, int count) {
        return () -> authRestApiClient.getDirectMessagesUpdatedForMarkerByDirectMessageApi(teamId, userId, currentLinkId, count);
    }

    @Override
    public Executor<ResMessages> loadGetDirectMarkerMessagesByDirectMessageApi(long teamId, long userId,
                                                                                long currentLinkId) {
        return () -> authRestApiClient.getDirectMarkerMessagesByDirectMessageApi(teamId, userId, currentLinkId);
    }

    @Override
    public Executor<ResCommon> loadSendDirectMessageByDirectMessageApi(long userId, long teamId,
                                                                        ReqSendMessageV3 reqSendMessageV3) {
        return () -> authRestApiClient.sendDirectMessageByDirectMessageApi(userId, teamId, reqSendMessageV3);
    }

    @Override
    public Executor<ResCommon> loadModifyDirectMessageByDirectMessageApi(ReqModifyMessage message, int userId, int messageId) {
        return () -> authRestApiClient.modifyDirectMessageByDirectMessageApi(message, userId, messageId);
    }

    @Override
    public Executor<ResCommon> loadDeleteDirectMessageByDirectMessageApi(long teamId, long userId, long messageId) {
        return () -> authRestApiClient.deleteDirectMessageByDirectMessageApi(teamId, userId, messageId);
    }

    @Override
    public Executor<ResCommon> loadCreatePrivateGroupByGroupApi(ReqCreateTopic group) {
        return () -> authRestApiClient.createPrivateGroupByGroupApi(group);
    }

    @Override
    public Executor<ResCommon> loadModifyGroupByGroupApi(ReqModifyTopicName channel, long groupId) {
        return () -> authRestApiClient.modifyGroupNameByGroupApi(channel, groupId);
    }

    @Override
    public Executor<ResCommon> loadModifyGroupDescriptionByGroupApi(ReqModifyTopicDescription entityInfo, long entityId) {
        return () -> authRestApiClient.modifyGroupDescriptionByGroupApi(entityInfo, entityId);
    }

    @Override
    public Executor<ResCommon> loadDeleteGroupByGroupApi(long teamId, long groupId) {
        return () -> authRestApiClient.deleteGroupByGroupApi(teamId, groupId);
    }

    @Override
    public Executor<ResCommon> loadLeaveGroupByGroupApi(long groupId, ReqTeam team) {
        return () -> authRestApiClient.leaveGroupByGroupApi(groupId, team);
    }

    @Override
    public Executor<ResCommon> loadInviteGroupByGroupApi(long groupId, ReqInviteTopicUsers inviteUsers) {
        return () -> authRestApiClient.inviteGroupByGroupApi(groupId, inviteUsers);
    }

    @Override
    public Executor<ResMessages> loadGetGroupMessagesByGroupMessageApi(long teamId, long groupId,
                                                                        long fromId,
                                                                        int count) {
        return () -> authRestApiClient.getGroupMessagesByGroupMessageApi(teamId, groupId, fromId, count);
    }

    @Override
    public Executor<ResMessages> loadGetGroupMessagesByGroupMessageApi(int teamId, int groupId) {
        return () -> authRestApiClient.getGroupMessagesByGroupMessageApi(teamId, groupId);
    }

    @Override
    public Executor<ResUpdateMessages> loadGetGroupMessagesUpdatedByGroupMessageApi(int teamId, int groupId,
                                                                                     int lastLinkId) {
        return () -> authRestApiClient.getGroupMessagesUpdatedByGroupMessageApi(teamId, groupId, lastLinkId);
    }

    @Override
    public Executor<ResMessages> loadGetGroupMessagesUpdatedForMarkerByGroupMessageApi(long teamId,
                                                                                        long groupId,
                                                                                        long currentLinkId) {
        return () -> authRestApiClient.getGroupMessagesUpdatedForMarkerByGroupMessageApi(teamId, groupId, currentLinkId);
    }

    @Override
    public Executor<ResMessages> loadGetGroupMessagesUpdatedForMarkerByGroupMessageApi(long teamId, long groupId, long currentLinkId, int count) {
        return () -> authRestApiClient.getGroupMessagesUpdatedForMarkerByGroupMessageApi(teamId, groupId, currentLinkId, count);
    }

    @Override
    public Executor<ResMessages> loadGetGroupMarkerMessagesByGroupMessageApi(long teamId, long groupId,
                                                                              long currentLinkId) {
        return () -> authRestApiClient.getGroupMarkerMessagesByGroupMessageApi(teamId, groupId, currentLinkId);
    }

    @Override
    public Executor<ResCommon> loadSendGroupMessageByGroupMessageApi(long privateGroupId, long teamId, ReqSendMessageV3 reqSendMessageV3) {
        return () -> authRestApiClient.sendGroupMessageByGroupMessageApi(privateGroupId, teamId, reqSendMessageV3);
    }

    @Override
    public Executor<ResCommon> loadModifyPrivateGroupMessageByGroupMessageApi(ReqModifyMessage message, int groupId, int messageId) {
        return () -> authRestApiClient.modifyPrivateGroupMessageByGroupMessageApi(message, groupId, messageId);
    }

    @Override
    public Executor<ResCommon> loadDeletePrivateGroupMessageByGroupMessageApi(long teamId, long groupId, long messageId) {
        return () -> authRestApiClient.deletePrivateGroupMessageByGroupMessageApi(teamId, groupId, messageId);
    }


    @Override
    public Executor<ResLeftSideMenu.User> loadUpdateMemberProfileByProfileApi(long memberId, ReqUpdateProfile
            reqUpdateProfile) {
        return () -> authRestApiClient.updateMemberProfileByProfileApi(memberId, reqUpdateProfile);
    }

    @Override
    public Executor<ResCommon> loadUpdateMemberNameByProfileApi(long memberId, ReqProfileName
            reqProfileName) {
        return () -> authRestApiClient.updateMemberNameByProfileApi(memberId, reqProfileName);
    }

    @Override
    public Executor<ResLeftSideMenu.User> loadUpdateMemberEmailByProfileApi(long memberId,
                                                                             ReqAccountEmail reqAccountEmail) {
        return () -> authRestApiClient.updateMemberEmailByProfileApi(memberId, reqAccountEmail);
    }

    @Override
    public Executor<ResTeamDetailInfo> loadAcceptOrDeclineInvitationByInvitationApi(String invitationId,
                                                                                     ReqInvitationAcceptOrIgnore reqInvitationAcceptOrIgnore) {
        return () -> authRestApiClient.acceptOrDeclineInvitationByInvitationApi(invitationId, reqInvitationAcceptOrIgnore);
    }

    @Override
    public Executor<List<ResPendingTeamInfo>> loadGetPendingTeamInfoByInvitationApi() {
        return () -> authRestApiClient.getPendingTeamInfoByInvitationApi();
    }

    @Override
    public Executor<ResMessageSearch> loadSearchMessagesByMessageSearchApi(long teamId, String query, int
            page,
                                                                            int perPage, long writerId, long entityId) {
        return () -> authRestApiClient.searchMessagesByMessageSearchApi(teamId, query, page, perPage, writerId, entityId);
    }

    @Override
    public Executor<ResMessageSearch> loadSearchMessagesByEntityIdByMessageSearchApi(long teamId, String query,
                                                                                      int page, int perPage, long entityId) {
        return () -> authRestApiClient.searchMessagesByEntityIdByMessageSearchApi(teamId, query, page, perPage, entityId);
    }

    @Override
    public Executor<ResMessageSearch> loadSearchMessagesByWriterIdByMessageSearchApi(long teamId, String query,
                                                                                      int page, int perPage, long writerId) {
        return () -> authRestApiClient.searchMessagesByWriterIdByMessageSearchApi(teamId, query, page, perPage, writerId);
    }

    @Override
    public Executor<ResMessageSearch> loadSearchMessagesByMessageSearchApi(long teamId,
                                                                            String query,
                                                                            int page, int perPage) {
        return () -> authRestApiClient.searchMessagesByMessageSearchApi(teamId, query, page, perPage);
    }

    @Override
    public Executor<ResFileDetail> loadGetFileDetailByMessagesApiAuth(long teamId, long messageId) {
        return () -> authRestApiClient.getFileDetailByMessagesApiAuth(teamId, messageId);
    }

    @Override
    public Executor<ResCommon> loadShareMessageByMessagesApiAuth(ReqShareMessage share, long
            messageId) {
        return () -> authRestApiClient.shareMessageByMessagesApiAuth(share, messageId);
    }

    @Override
    public Executor<ResCommon> loadUnshareMessageByMessagesApiAuth(ReqUnshareMessage share, long
            messageId) {
        return () -> authRestApiClient.unshareMessageByMessagesApiAuth(share, messageId);
    }

    @Override
    public Executor<List<ResMessages.Link>> getRoomUpdateMessageByMessagesApiAuth(long teamId, long roomId, long currentLinkId) {
        return () -> authRestApiClient.getRoomUpdateMessageByMessagesApiAuth(teamId, roomId, currentLinkId);
    }

    @Override
    public Executor<ResRoomInfo> loadGetRoomInfoByRoomsApi(long teamId, long roomId) {
        return () -> authRestApiClient.getRoomInfoByRoomsApi(teamId, roomId);
    }

    @Override
    public Executor<ResCommon> loadEnableFavoriteByStarredEntityApi(ReqTeam reqTeam, long entityId) {
        return () -> authRestApiClient.enableFavoriteByStarredEntityApi(reqTeam, entityId);
    }

    @Override
    public Executor<ResCommon> loadDisableFavoriteByStarredEntityApi(long teamId, long entityId) {
        return () -> authRestApiClient.disableFavoriteByStarredEntityApi(teamId, entityId);
    }

    @Override
    public Executor<ResCommon> loadSendStickerByStickerApi(ReqSendSticker reqSendSticker) {
        return () -> authRestApiClient.sendStickerByStickerApi(reqSendSticker);
    }

    @Override
    public Executor<ResCommon> loadSendStickerCommentByStickerApi(ReqSendSticker reqSendSticker) {
        return () -> authRestApiClient.sendStickerCommentByStickerApi(reqSendSticker);
    }

    @Override
    public Executor<ResTeamDetailInfo> loadCreateNewTeamByTeamApi(ReqCreateNewTeam req) {
        return () -> authRestApiClient.createNewTeamByTeamApi(req);
    }

    @Override
    public Executor<ResLeftSideMenu.User> loadGetMemberProfileByTeamApi(long teamId, long memberId) {
        return () -> authRestApiClient.getMemberProfileByTeamApi(teamId, memberId);
    }

    @Override
    public Executor<List<ResInvitationMembers>> loadInviteToTeamByTeamApi(long teamId, ReqInvitationMembers
            invitationMembers) {
        return () -> authRestApiClient.inviteToTeamByTeamApi(teamId, invitationMembers);
    }

    @Override
    public Executor<ResTeamDetailInfo.InviteTeam> loadGetTeamInfoByTeamApi(long teamId) {
        return () -> authRestApiClient.getTeamInfoByTeamApi(teamId);
    }

    @Override
    public Executor<ResAnnouncement> loadGetAnnouncement(long teamId, long topicId) {
        return () -> authRestApiClient.getAnnouncement(teamId, topicId);
    }

    @Override
    public Executor<ResCommon> loadCreateAnnouncement(long teamId, long topicId, ReqCreateAnnouncement reqCreateAnnouncement) {
        return () -> authRestApiClient.createAnnouncement(teamId, topicId, reqCreateAnnouncement);
    }

    @Override
    public Executor<ResCommon> loadUpdateAnnouncementStatus(long teamId, long memberId, ReqUpdateAnnouncementStatus reqUpdateAnnouncementStatus) {
        return () -> authRestApiClient.updateAnnouncementStatus(teamId, memberId, reqUpdateAnnouncementStatus);
    }

    @Override
    public Executor<ResCommon> loadDeleteAnnouncement(long teamId, long topicId) {
        return () -> authRestApiClient.deleteAnnouncement(teamId, topicId);
    }

    @Override
    public Executor<ResMessages.OriginalMessage> loadGetMessage(long teamId, long topicId) {
        return () -> authRestApiClient.getMessage(teamId, topicId);
    }

    @Override
    public Executor<ResCommon> loadUpdateTopicPushSubscribe(long teamId, long topicId, ReqUpdateTopicPushSubscribe reqUpdateTopicPushSubscribe) {
        return () -> authRestApiClient.updateTopicPushSubscribe(teamId, topicId, reqUpdateTopicPushSubscribe);
    }

    @Override
    public Executor<ResCommon> loadResetPasswordByAccountPasswordApi(ReqAccountEmail
                                                                              reqAccountEmail) {
        return () -> simpleRestApiClient.resetPasswordByAccountPasswordApi(reqAccountEmail);
    }

    @Override
    public Executor<ResConfig> loadGetConfigByMainRestApi() {
        return () -> simpleRestApiClient.getConfigByMainRest();
    }

    @Override
    public Executor<ResMyTeam> loadGetTeamIdByMainRestApi(String userEmail) {
        return () -> simpleRestApiClient.getTeamIdByMainRest(userEmail);
    }

    @Override
    public Executor<ResAccessToken> loadGetAccessTokenByMainRestApi(ReqAccessToken login) {
        return () -> simpleRestApiClient.getAccessTokenByMainRest(login);
    }

    @Override
    public Executor<ResCommon> loadSignUpAccountByMainRestApi(ReqSignUpInfo signUpInfo) {
        return () -> simpleRestApiClient.signUpAccountByMainRest(signUpInfo);
    }

    @Override
    public Executor<ResAccountActivate> loadActivateAccountByMainRestApi(ReqAccountActivate
                                                                                  reqAccountActivate) {
        return () -> simpleRestApiClient.activateAccountByMainRest(reqAccountActivate);
    }

    @Override
    public Executor<ResCommon> loadAccountVerificationByMainRestApi(ReqAccountVerification reqAccountVerification) {
        return () -> simpleRestApiClient.accountVerificationByMainRest(reqAccountVerification);
    }

    @Override
    public Executor<ResCommon> loadDeleteStickerCommentByStickerApi(long commentId, long teamId) {
        return () -> authRestApiClient.deleteStickerCommentByStickerApi(commentId, teamId);
    }

    @Override
    public Executor<ResCommon> loadDeleteStickerByStickerApi(long messageId, long teamId) {
        return () -> authRestApiClient.deleteStickerByStickerApi(messageId, teamId);
    }

    @Override
    public Executor<ResCommon> loaderDeleteFileByFileApi(long teamId, long fileId) {
        return () -> authRestApiClient.deleteFileByFileApi(teamId, fileId);
    }

    @Override
    public Executor<List<ResMessages.FileMessage>> loaderSearchInitImageFileByFileApi(long teamId, long roomId, long messageId, int count) {
        return () -> authRestApiClient.searchInitImageFileByFileApi(teamId, roomId, messageId, count);
    }

    @Override
    public Executor<List<ResMessages.FileMessage>> loaderSearchOldImageFileByFileApi(long teamId, long roomId, long messageId, int count) {
        return () -> authRestApiClient.searchOldImageFileByFileApi(teamId, roomId, messageId, count);
    }

    @Override
    public Executor<List<ResMessages.FileMessage>> loaderSearchNewImageFileByFileApi(long teamId, long roomId, long messageId, int count) {
        return () -> authRestApiClient.searchNewImageFileByFileApi(teamId, roomId, messageId, count);
    }

    @Override
    public Executor<ResStarMentioned> loadGetMentionedMessagesByTeamApi(long teamId, long messageId, int count) {
        return () -> authRestApiClient.getMentionedMessagesByTeamApi(teamId, messageId, count);
    }

    @Override
    public Executor<StarMentionedMessageObject> loadRegistStarredMessageByTeamApi(long teamId, long messageId) {
        return () -> authRestApiClient.registStarredMessageByTeamApi(teamId, messageId);
    }

    @Override
    public Executor<ResCommon> loadUnregistStarredMessageByTeamApi(long teamId, long messageId) {
        return () -> authRestApiClient.unregistStarredMessageByTeamApi(teamId, messageId);
    }

    @Override
    public Executor<ResStarMentioned> loadGetStarredMessagesByTeamApi(long teamId, long messageId, int count, String type) {
        return () -> authRestApiClient.getStarredMessagesByTeamApi(teamId, messageId, count, type);
    }

    @Override
    public Executor<ResCommon> loadUpdatePlatformStatus(ReqUpdatePlatformStatus reqUpdatePlatformStatus) {
        return () -> authRestApiClient.updatePlatformStatus(reqUpdatePlatformStatus);
    }

    @Override
    public Executor<ResCreateFolder> loadCreateFolderByTeamApi(long teamId, ReqCreateFolder reqCreateFolder) {
        return () -> authRestApiClient.createFolderByTeamApi(teamId, reqCreateFolder);
    }

    @Override
    public Executor<ResCommon> loadDeleteFolderByTeamApi(long teamId, long folderId) {
        return () -> authRestApiClient.deleteFolderByTeamApi(teamId, folderId);
    }

    @Override
    public Executor<ResUpdateFolder> loadUpdateFolderByTeamApi(long teamId, long folderId, ReqUpdateFolder reqUpdateFolder) {
        return () -> authRestApiClient.updateFolderByTeamApi(teamId, folderId, reqUpdateFolder);
    }

    @Override
    public Executor<List<ResFolder>> loadGetFoldersByTeamApi(long teamId) {
        return () -> authRestApiClient.getFoldersByTeamApi(teamId);
    }

    @Override
    public Executor<List<ResFolderItem>> loadGetFolderItemsByTeamApi(long teamId) {
        return () -> authRestApiClient.getFolderItemsByTeamApi(teamId);
    }

    @Override
    public Executor<ResRegistFolderItem> loadRegistFolderItemByTeamApi(long teamId, long folderId, ReqRegistFolderItem reqRegistFolderItem) {
        return () -> authRestApiClient.registFolderItemByTeamApi(teamId, folderId, reqRegistFolderItem);
    }

    @Override
    public Executor<ResCommon> loadDeleteFolderItemByTeamApi(long teamId, long folderId, long itemId) {
        return () -> authRestApiClient.deleteFolderItemByTeamApi(teamId, folderId, itemId);
    }

    @Override
    public Executor<ResCommon> loadKickUserFromTopic(long teamId, long topicId, ReqMember member) {
        return () -> authRestApiClient.kickUserFromTopic(teamId, topicId, member);
    }

    @Override
    public Executor<ResCommon> loadAssignToTopicOwner(long teamId, long topicId, ReqOwner owner) {
        return () -> authRestApiClient.assignToTopicOwner(teamId, topicId, owner);
    }

    @Override
    public Executor<ResMessages.FileMessage> loadEnableFileExternalLink(long teamId, long fileId) {
        return () -> authRestApiClient.enableFileExternalLink(teamId, fileId);
    }

    @Override
    public Executor<ResMessages.FileMessage> loadDisableFileExternalLink(long teamId, long fileId) {
        return () -> authRestApiClient.disableFileExternalLink(teamId, fileId);
    }

    @Override
    public Executor<ResEventHistory> loadGetEventHistory(long ts, long memberId, String eventType, Integer size) {
        return () -> authRestApiClient.getEventHistory(ts, memberId, eventType, size);
    }

    @Override
    public Executor<ResValidation> loadValidDomain(String domain) {
        return () -> authRestApiClient.validDomain(domain);
    }

    @Override
    public Executor<ResAvatarsInfo> loadGetAvartarsInfo() {
        return () -> authRestApiClient.getAvartarsInfo();
    }
}