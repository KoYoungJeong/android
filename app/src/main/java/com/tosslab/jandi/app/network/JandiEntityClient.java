package com.tosslab.jandi.app.network;

import android.content.Context;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.invitation.InvitationApiClient;
import com.tosslab.jandi.app.network.client.notification.NotificationApiClient;
import com.tosslab.jandi.app.network.client.privatetopic.GroupApiClient;
import com.tosslab.jandi.app.network.client.publictopic.ChannelApiClient;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqInvitationMembers;
import com.tosslab.jandi.app.network.models.ReqInviteUsers;
import com.tosslab.jandi.app.network.models.ReqNotificationRegister;
import com.tosslab.jandi.app.network.models.ReqNotificationSubscribe;
import com.tosslab.jandi.app.network.models.ReqNotificationTarget;
import com.tosslab.jandi.app.network.models.ReqSendComment;
import com.tosslab.jandi.app.network.models.ReqShareMessage;
import com.tosslab.jandi.app.network.models.ReqUnshareMessage;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResInvitationMembers;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.rest.RestService;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Arrays;
import java.util.List;

/**
 * Created by justinygchoi on 2014. 8. 27..
 */
@EBean
public class JandiEntityClient {
    private final String AUTH_HEADER = JandiConstants.AUTH_HEADER;
    private final String ACCEPT_HEADER = "Accept";

    @RestService
    JandiRestClient mJandiRestClient;

    @RestService
    InvitationApiClient invitationApiClient;

    @RestService
    NotificationApiClient notificationApiClient;

    @RestService
    GroupApiClient groupApiClient;

    @RestService
    ChannelApiClient channelApiClient;

    @RootContext
    Context context;

    @AfterInject
    void initAuthentication() {
        String myToken = JandiPreference.getMyToken(context);
        mJandiRestClient.setHeader(AUTH_HEADER, myToken);
        mJandiRestClient.setHeader(ACCEPT_HEADER, JandiV2HttpMessageConverter.APPLICATION_VERSION_FULL_NAME);

        invitationApiClient.setHeader(AUTH_HEADER, myToken);
        invitationApiClient.setHeader(ACCEPT_HEADER, JandiV2HttpMessageConverter.APPLICATION_VERSION_FULL_NAME);

        notificationApiClient.setHeader(AUTH_HEADER, myToken);
        notificationApiClient.setHeader(ACCEPT_HEADER, JandiV2HttpMessageConverter.APPLICATION_VERSION_FULL_NAME);

        groupApiClient.setHeader(AUTH_HEADER, myToken);
        groupApiClient.setHeader(ACCEPT_HEADER, JandiV2HttpMessageConverter.APPLICATION_VERSION_FULL_NAME);

        channelApiClient.setHeader(AUTH_HEADER, myToken);
        channelApiClient.setHeader(ACCEPT_HEADER, JandiV2HttpMessageConverter.APPLICATION_VERSION_FULL_NAME);

    }

    /**
     * *********************************************************
     * 팀 관리
     * **********************************************************
     */
    public List<ResInvitationMembers> inviteTeamMember(String email) throws JandiNetworkException {
        try {
            List<String> strings = Arrays.asList(email);
            return invitationApiClient.inviteMembers(new ReqInvitationMembers(1, strings, "ko"));
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    /**
     * *********************************************************
     * Entity (Channel, Private Group, Direct Message) 관련
     * **********************************************************
     */
    public ResLeftSideMenu getTotalEntitiesInfo(int teamId) throws JandiNetworkException {
        try {

            return mJandiRestClient.getInfosForSideMenu(teamId);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon createPublicTopic(String entityName) throws JandiNetworkException {
        ReqCreateTopic reqCreateTopic = new ReqCreateTopic();
        reqCreateTopic.name = entityName;
        try {
            return channelApiClient.createChannel(reqCreateTopic);

        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon createPrivateGroup(String entityName) throws JandiNetworkException {
        ReqCreateTopic reqCreateTopic = new ReqCreateTopic();
        reqCreateTopic.name = entityName;
        try {
            return groupApiClient.createPrivateGroup(reqCreateTopic);

        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon joinChannel(ResLeftSideMenu.Channel channel) throws JandiNetworkException {
        try {
            return channelApiClient.joinChannel(channel.id);

        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon leaveChannel(int id) throws JandiNetworkException {
        try {
            return channelApiClient.leaveChannel(id);

        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon leavePrivateGroup(int id) throws JandiNetworkException {
        try {
            return channelApiClient.leaveChannel(id);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon modifyChannelName(int id, String name) throws JandiNetworkException {
        ReqCreateTopic entityInfo = new ReqCreateTopic();
        entityInfo.name = name;
        try {
            return channelApiClient.modifyChannelName(entityInfo, id);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon modifyPrivateGroupName(int id, String name) throws JandiNetworkException {
        ReqCreateTopic entityInfo = new ReqCreateTopic();
        entityInfo.name = name;
        try {
            return groupApiClient.modifyGroup(entityInfo, id);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon deleteChannel(int id) throws JandiNetworkException {
        try {
            return channelApiClient.deleteChannel(id);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon deletePrivateGroup(int id) throws JandiNetworkException {
        try {
            return null;
//            return mJandiRestClient.deleteGroup(id);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon inviteChannel(int id, List<Integer> invitedUsers) throws JandiNetworkException {
        ReqInviteUsers reqInviteUsers = new ReqInviteUsers(invitedUsers);
        try {
            return channelApiClient.inviteChannel(id, reqInviteUsers);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon invitePrivateGroup(int id, List<Integer> invitedUsers) throws JandiNetworkException {
        ReqInviteUsers reqInviteUsers = new ReqInviteUsers(invitedUsers);
        try {
            return groupApiClient.inviteGroup(id, reqInviteUsers);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    /**
     * *********************************************************
     * Entity 즐겨찾기 등록 / 해제
     * **********************************************************
     */
    public ResCommon enableFavorite(int entityId) throws JandiNetworkException {
        try {
            return mJandiRestClient.enableFavorite(entityId);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon disableFavorite(int entityId) throws JandiNetworkException {
        try {
            return mJandiRestClient.disableFavorite(entityId);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    /**
     * *********************************************************
     * 사용자 프로필
     * **********************************************************
     */
    public ResLeftSideMenu.User getUserProfile(int entityId) throws JandiNetworkException {
        try {
            return mJandiRestClient.getUserProfile(entityId);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResLeftSideMenu.User updateUserProfile(ReqUpdateProfile reqUpdateProfile) throws JandiNetworkException {
        try {
            return mJandiRestClient.updateUserProfile(reqUpdateProfile);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    /**
     * *********************************************************
     * Push Notification Token
     * **********************************************************
     */
    public ResCommon registerNotificationToken(String oldDevToken, String newDevToken) throws JandiNetworkException {
        ReqNotificationRegister req = new ReqNotificationRegister("android", oldDevToken, newDevToken);
        try {
            return notificationApiClient.registerNotificationToken(req);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon deleteNotificationToken(String regId) throws JandiNetworkException {
        try {
            return notificationApiClient.deleteNotificationToken(regId);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon subscribeNotification(String regId, boolean isSubscribe) throws JandiNetworkException {
        ReqNotificationSubscribe req = new ReqNotificationSubscribe(isSubscribe);

        try {
            return notificationApiClient.subscribeNotification(regId, req);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon setNotificationTarget(String target) throws JandiNetworkException {
        ReqNotificationTarget req = new ReqNotificationTarget(target);
        try {
            return notificationApiClient.setNotificationTarget(req);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }


    /**
     * *********************************************************
     * File 관련
     * **********************************************************
     */
    public ResFileDetail getFileDetail(int messageId) throws JandiNetworkException {
        try {
            return mJandiRestClient.getFileDetail(messageId);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon sendMessageComment(int messageId, String comment) throws JandiNetworkException {
        ReqSendComment reqSendComment = new ReqSendComment();
        reqSendComment.comment = comment;
        try {
            return mJandiRestClient.sendMessageComment(reqSendComment, messageId);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon shareMessage(int messageId, int cdpIdToBeShared) throws JandiNetworkException {
        ReqShareMessage reqShareMessage = new ReqShareMessage();
        reqShareMessage.shareEntity = cdpIdToBeShared;
        try {
            return mJandiRestClient.shareMessage(reqShareMessage, messageId);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon unshareMessage(int messageId, int cdpIdToBeunshared) throws JandiNetworkException {
        ReqUnshareMessage reqUnshareMessage = new ReqUnshareMessage(cdpIdToBeunshared);
        try {
            return mJandiRestClient.unshareMessage(reqUnshareMessage, messageId);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon modifyMessageComment(int messageId, String comment, int feedbackId)
            throws JandiNetworkException {
        ReqSendComment reqModifyComment = new ReqSendComment();
        reqModifyComment.comment = comment;
        try {
            return mJandiRestClient.modifyMessageComment(reqModifyComment, feedbackId, messageId);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon deleteMessageComment(int messageId, int feedbackId) throws JandiNetworkException {
        try {
            return mJandiRestClient.deleteMessageComment(feedbackId, messageId);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon deleteFile(int fileId) throws JandiNetworkException {
        try {
            return mJandiRestClient.deleteFile(fileId);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }
}
