package com.tosslab.jandi.app.network.client;

import android.content.Context;

import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.client.account.devices.AccountDevicesApiClient;
import com.tosslab.jandi.app.network.client.account.devices.AccountDevicesApiClient_;
import com.tosslab.jandi.app.network.client.file.FileApiClient;
import com.tosslab.jandi.app.network.client.file.FileApiClient_;
import com.tosslab.jandi.app.network.client.invitation.InvitationApiClient;
import com.tosslab.jandi.app.network.client.invitation.InvitationApiClient_;
import com.tosslab.jandi.app.network.client.messages.MessagesApiClient;
import com.tosslab.jandi.app.network.client.messages.MessagesApiClient_;
import com.tosslab.jandi.app.network.client.messages.comments.CommentsApiClient;
import com.tosslab.jandi.app.network.client.messages.comments.CommentsApiClient_;
import com.tosslab.jandi.app.network.client.privatetopic.GroupApiClient;
import com.tosslab.jandi.app.network.client.privatetopic.GroupApiClient_;
import com.tosslab.jandi.app.network.client.profile.ProfileApiClient;
import com.tosslab.jandi.app.network.client.profile.ProfileApiClient_;
import com.tosslab.jandi.app.network.client.publictopic.ChannelApiClient;
import com.tosslab.jandi.app.network.client.publictopic.ChannelApiClient_;
import com.tosslab.jandi.app.network.client.settings.StarredEntityApiClient;
import com.tosslab.jandi.app.network.client.settings.StarredEntityApiClient_;
import com.tosslab.jandi.app.network.client.teams.TeamsApiClient;
import com.tosslab.jandi.app.network.client.teams.TeamsApiClient_;
import com.tosslab.jandi.app.network.manager.Request;
import com.tosslab.jandi.app.network.manager.RequestManager;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqDeleteTopic;
import com.tosslab.jandi.app.network.models.ReqDeviceToken;
import com.tosslab.jandi.app.network.models.ReqInvitationMembers;
import com.tosslab.jandi.app.network.models.ReqInviteTopicUsers;
import com.tosslab.jandi.app.network.models.ReqNotificationRegister;
import com.tosslab.jandi.app.network.models.ReqNotificationSubscribe;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ReqSendComment;
import com.tosslab.jandi.app.network.models.ReqShareMessage;
import com.tosslab.jandi.app.network.models.ReqSubscibeToken;
import com.tosslab.jandi.app.network.models.ReqTeam;
import com.tosslab.jandi.app.network.models.ReqUnshareMessage;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResInvitationMembers;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.LanguageUtil;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Arrays;
import java.util.List;

/**
 * Created by justinygchoi on 2014. 8. 27..
 */
@EBean
public class JandiEntityClient {

    @RootContext
    Context context;

    private int selectedTeamId;

    @AfterInject
    void initAuthentication() {

        ResAccountInfo.UserTeam selectedTeamInfo = JandiAccountDatabaseManager.getInstance(context).getSelectedTeamInfo();
        selectedTeamId = selectedTeamInfo.getTeamId();

    }

    /**
     * *********************************************************
     * Entity (Channel, Private Group, Direct Message) 관련
     * **********************************************************
     */
    public ResLeftSideMenu getTotalEntitiesInfo() throws JandiNetworkException {
        try {

            return RequestManager.newInstance(context, new Request<ResLeftSideMenu>() {
                @Override
                public ResLeftSideMenu request() throws JandiNetworkException {

                    JandiRestClient mJandiRestClient = new JandiRestClient_(context);
                    mJandiRestClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
                    return mJandiRestClient.getInfosForSideMenu(selectedTeamId);
                }
            }).request();

        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    public ResCommon createPublicTopic(String entityName) throws JandiNetworkException {
        final ReqCreateTopic reqCreateTopic = new ReqCreateTopic();
        reqCreateTopic.teamId = selectedTeamId;
        reqCreateTopic.name = entityName;
        try {
            return RequestManager.newInstance(context, new Request<ResCommon>() {

                @Override
                public ResCommon request() throws JandiNetworkException {
                    ChannelApiClient channelApiClient = new ChannelApiClient_(context);
                    channelApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
                    return channelApiClient.createChannel(reqCreateTopic);
                }
            }).request();

        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    public ResCommon createPrivateGroup(String entityName) throws JandiNetworkException {
        final ReqCreateTopic reqCreateTopic = new ReqCreateTopic();
        reqCreateTopic.teamId = selectedTeamId;
        reqCreateTopic.name = entityName;
        try {

            return RequestManager.newInstance(context, new Request<ResCommon>() {

                @Override
                public ResCommon request() throws JandiNetworkException {
                    GroupApiClient groupApiClient = new GroupApiClient_(context);
                    groupApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
                    return groupApiClient.createPrivateGroup(reqCreateTopic);
                }
            }).request();


        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    public ResCommon joinChannel(final ResLeftSideMenu.Channel channel) throws JandiNetworkException {
        try {

            return RequestManager.newInstance(context, new Request<ResCommon>() {

                @Override
                public ResCommon request() throws JandiNetworkException {
                    ChannelApiClient channelApiClient = new ChannelApiClient_(context);
                    channelApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
                    return channelApiClient.joinTopic(channel.id, new ReqDeleteTopic(selectedTeamId));
                }
            }).request();


        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    public ResCommon leaveChannel(final int id) throws JandiNetworkException {
        try {

            return RequestManager.newInstance(context, new Request<ResCommon>() {

                @Override
                public ResCommon request() throws JandiNetworkException {
                    ChannelApiClient channelApiClient = new ChannelApiClient_(context);
                    channelApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
                    return channelApiClient.leaveTopic(id, new ReqDeleteTopic(selectedTeamId));
                }
            }).request();

        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    public ResCommon leavePrivateGroup(final int id) throws JandiNetworkException {
        try {
            return RequestManager.newInstance(context, new Request<ResCommon>() {

                @Override
                public ResCommon request() throws JandiNetworkException {
                    GroupApiClient groupApiClient = new GroupApiClient_(context);
                    groupApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
                    return groupApiClient.leaveGroup(id, new ReqTeam(selectedTeamId));
                }
            }).request();
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    public ResCommon modifyChannelName(final int id, String name) throws JandiNetworkException {
        final ReqCreateTopic entityInfo = new ReqCreateTopic();
        entityInfo.teamId = selectedTeamId;
        entityInfo.name = name;
        try {

            return RequestManager.newInstance(context, new Request<ResCommon>() {

                @Override
                public ResCommon request() throws JandiNetworkException {
                    ChannelApiClient channelApiClient = new ChannelApiClient_(context);
                    channelApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
                    return channelApiClient.modifyPublicTopicName(entityInfo, id);
                }
            }).request();
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    public ResCommon modifyPrivateGroupName(final int id, String name) throws JandiNetworkException {
        final ReqCreateTopic entityInfo = new ReqCreateTopic();
        entityInfo.teamId = selectedTeamId;
        entityInfo.name = name;
        try {

            return RequestManager.newInstance(context, new Request<ResCommon>() {

                @Override
                public ResCommon request() throws JandiNetworkException {
                    GroupApiClient groupApiClient = new GroupApiClient_(context);
                    groupApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
                    return groupApiClient.modifyGroup(entityInfo, id);
                }
            }).request();

        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    public ResCommon deleteChannel(final int id) throws JandiNetworkException {
        try {

            return RequestManager.newInstance(context, new Request<ResCommon>() {

                @Override
                public ResCommon request() throws JandiNetworkException {
                    ChannelApiClient channelApiClient = new ChannelApiClient_(context);
                    channelApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
                    return channelApiClient.deleteTopic(id, new ReqDeleteTopic(selectedTeamId));
                }
            }).request();

        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    public ResCommon deletePrivateGroup(final int id) throws JandiNetworkException {
        try {
            return RequestManager.newInstance(context, new Request<ResCommon>() {

                @Override
                public ResCommon request() throws JandiNetworkException {
                    GroupApiClient groupApiClient = new GroupApiClient_(context);
                    groupApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
                    return groupApiClient.deleteGroup(selectedTeamId, id);
                }
            }).request();
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    public ResCommon inviteChannel(final int id, final List<Integer> invitedUsers) throws JandiNetworkException {
        try {
            return RequestManager.newInstance(context, new Request<ResCommon>() {

                @Override
                public ResCommon request() throws JandiNetworkException {
                    ChannelApiClient channelApiClient = new ChannelApiClient_(context);
                    channelApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
                    return channelApiClient.invitePublicTopic(id, new ReqInviteTopicUsers(invitedUsers, selectedTeamId));
                }
            }).request();
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    public ResCommon invitePrivateGroup(final int id, final List<Integer> invitedUsers) throws JandiNetworkException {
        try {

            return RequestManager.newInstance(context, new Request<ResCommon>() {

                @Override
                public ResCommon request() throws JandiNetworkException {
                    GroupApiClient groupApiClient = new GroupApiClient_(context);
                    groupApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
                    return groupApiClient.inviteGroup(id, new ReqInviteTopicUsers(invitedUsers, selectedTeamId));
                }
            }).request();

        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    /**
     * *********************************************************
     * Entity 즐겨찾기 등록 / 해제
     * **********************************************************
     */
    public ResCommon enableFavorite(final int entityId) throws JandiNetworkException {
        try {

            return RequestManager.newInstance(context, new Request<ResCommon>() {

                @Override
                public ResCommon request() throws JandiNetworkException {
                    StarredEntityApiClient starredEntityApiClient = new StarredEntityApiClient_(context);
                    starredEntityApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
                    return starredEntityApiClient.enableFavorite(new ReqTeam(selectedTeamId), entityId);
                }
            }).request();

        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    public ResCommon disableFavorite(final int entityId) throws JandiNetworkException {
        try {

            return RequestManager.newInstance(context, new Request<ResCommon>() {

                @Override
                public ResCommon request() throws JandiNetworkException {
                    StarredEntityApiClient starredEntityApiClient = new StarredEntityApiClient_(context);
                    starredEntityApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
                    return starredEntityApiClient.disableFavorite(selectedTeamId, entityId);
                }
            }).request();

        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    /**
     * *********************************************************
     * 사용자 프로필
     * **********************************************************
     */
    public ResLeftSideMenu.User getUserProfile(final int entityId) throws JandiNetworkException {
        try {
            return RequestManager.newInstance(context, new Request<ResLeftSideMenu.User>() {
                @Override
                public ResLeftSideMenu.User request() throws JandiNetworkException {

                    TeamsApiClient teamsApiClient = new TeamsApiClient_(context);
                    teamsApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
                    return teamsApiClient.getMemberProfile(selectedTeamId, entityId);
                }
            }).request();
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    public ResLeftSideMenu.User updateUserProfile(final int entityId, final ReqUpdateProfile reqUpdateProfile) throws JandiNetworkException {
        try {

            return RequestManager.newInstance(context, new Request<ResLeftSideMenu.User>() {
                @Override
                public ResLeftSideMenu.User request() throws JandiNetworkException {
                    ProfileApiClient profileApiClient = new ProfileApiClient_(context);
                    profileApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
                    return profileApiClient.updateMemberProfile(entityId, reqUpdateProfile);
                }
            }).request();

        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    public ResCommon updateMemberName(final int entityId, final ReqProfileName profileName) throws JandiNetworkException {
        try {

            return RequestManager.newInstance(context, new Request<ResCommon>() {
                @Override
                public ResCommon request() throws JandiNetworkException {
                    ProfileApiClient profileApiClient = new ProfileApiClient_(context);
                    profileApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
                    return profileApiClient.updateMemberName(entityId, profileName);
                }
            }).request();

        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    public ResLeftSideMenu.User updateMemberEmail(int entityId, String email) throws JandiNetworkException {
        return RequestManager.newInstance(context, new Request<ResLeftSideMenu.User>() {
            @Override
            public ResLeftSideMenu.User request() throws JandiNetworkException {

                ProfileApiClient profileApiClient = new ProfileApiClient_(context);
                profileApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
                return profileApiClient.updateMemberEmail(entityId, new ReqAccountEmail(email));
            }
        }).request();
    }

    /**
     * *********************************************************
     * Push Notification Token
     * **********************************************************
     */
    @Deprecated
    public ResAccountInfo registerNotificationToken(String oldDevToken, String newDevToken) throws JandiNetworkException {
        ReqNotificationRegister req = new ReqNotificationRegister("android", newDevToken);
        try {
            AccountDevicesApiClient accountDevicesApiClient = new AccountDevicesApiClient_(context);
            accountDevicesApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
            return accountDevicesApiClient.registerNotificationToken(req);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    @Deprecated
    public ResAccountInfo deleteNotificationToken(String regId) throws JandiNetworkException {
        try {
            AccountDevicesApiClient accountDevicesApiClient = new AccountDevicesApiClient_(context);
            accountDevicesApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
            return accountDevicesApiClient.deleteNotificationToken(new ReqDeviceToken(regId));
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    public ResAccountInfo subscribeNotification(final String regId, final boolean isSubscribe) throws JandiNetworkException {
        ReqNotificationSubscribe req = new ReqNotificationSubscribe(isSubscribe);

        try {
            return RequestManager.newInstance(context, new Request<ResAccountInfo>() {
                @Override
                public ResAccountInfo request() throws JandiNetworkException {
                    AccountDevicesApiClient accountDevicesApiClient = new AccountDevicesApiClient_(context);
                    accountDevicesApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
                    return accountDevicesApiClient.subscribeStateNotification(new ReqSubscibeToken(regId, isSubscribe));
                }
            }).request();
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    /**
     * *********************************************************
     * File 관련
     * **********************************************************
     */
    public ResFileDetail getFileDetail(final int messageId) throws JandiNetworkException {
        try {
            return RequestManager.newInstance(context, new Request<ResFileDetail>() {
                @Override
                public ResFileDetail request() throws JandiNetworkException {
                    MessagesApiClient messagesApiClient = new MessagesApiClient_(context);
                    messagesApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
                    return messagesApiClient.getFileDetail(selectedTeamId, messageId);
                }
            }).request();
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    public ResCommon sendMessageComment(final int messageId, String comment) throws JandiNetworkException {
        final ReqSendComment reqSendComment = new ReqSendComment();
        reqSendComment.teamId = selectedTeamId;
        reqSendComment.comment = comment;
        try {
            return RequestManager.newInstance(context, new Request<ResCommon>() {
                @Override
                public ResCommon request() throws JandiNetworkException {
                    CommentsApiClient commentsApiClient = new CommentsApiClient_(context);
                    commentsApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
                    return commentsApiClient.sendMessageComment(reqSendComment, messageId);
                }
            }).request();
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    public ResCommon shareMessage(final int messageId, int cdpIdToBeShared) throws JandiNetworkException {
        final ReqShareMessage reqShareMessage = new ReqShareMessage();
        reqShareMessage.shareEntity = cdpIdToBeShared;
        reqShareMessage.teamId = selectedTeamId;
        try {

            return RequestManager.newInstance(context, new Request<ResCommon>() {
                @Override
                public ResCommon request() throws JandiNetworkException {
                    MessagesApiClient messagesApiClient = new MessagesApiClient_(context);
                    messagesApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
                    return messagesApiClient.shareMessage(reqShareMessage, messageId);
                }
            }).request();

        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    public ResCommon unshareMessage(final int messageId, int cdpIdToBeunshared) throws JandiNetworkException {
        final ReqUnshareMessage reqUnshareMessage = new ReqUnshareMessage(selectedTeamId, cdpIdToBeunshared);
        try {
            return RequestManager.newInstance(context, new Request<ResCommon>() {
                @Override
                public ResCommon request() throws JandiNetworkException {
                    MessagesApiClient messagesApiClient = new MessagesApiClient_(context);
                    messagesApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
                    return messagesApiClient.unshareMessage(reqUnshareMessage, messageId);
                }
            }).request();

        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    public ResCommon modifyMessageComment(final int messageId, String comment, final int feedbackId)
            throws JandiNetworkException {
        final ReqSendComment reqModifyComment = new ReqSendComment();
        reqModifyComment.teamId = selectedTeamId;
        reqModifyComment.comment = comment;

        try {

            return RequestManager.newInstance(context, new Request<ResCommon>() {
                @Override
                public ResCommon request() throws JandiNetworkException {
                    CommentsApiClient commentsApiClient = new CommentsApiClient_(context);
                    commentsApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
                    return commentsApiClient.modifyMessageComment(reqModifyComment, feedbackId, messageId);
                }
            }).request();


        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    public ResCommon deleteMessageComment(final int messageId, final int feedbackId) throws JandiNetworkException {
        try {

            return RequestManager.newInstance(context, new Request<ResCommon>() {
                @Override
                public ResCommon request() throws JandiNetworkException {
                    CommentsApiClient commentsApiClient = new CommentsApiClient_(context);
                    commentsApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
                    return commentsApiClient.deleteMessageComment(selectedTeamId, feedbackId, messageId);
                }
            }).request();

        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    public ResCommon deleteFile(final int fileId) throws JandiNetworkException {
        try {

            return RequestManager.newInstance(context, new Request<ResCommon>() {
                @Override
                public ResCommon request() throws JandiNetworkException {
                    FileApiClient fileApiClient = new FileApiClient_(context);
                    fileApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
                    return fileApiClient.deleteFile(selectedTeamId, fileId);
                }
            }).request();


        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }
}
