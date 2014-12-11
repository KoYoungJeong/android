package com.tosslab.jandi.app.network;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.models.ReqCreateEntity;
import com.tosslab.jandi.app.network.models.ReqInvitation;
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
import com.tosslab.jandi.app.network.models.ResInvitation;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.springframework.web.client.HttpStatusCodeException;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 8. 27..
 * TODO MessageManipulator 와 합쳐지겠지...
 */
public class JandiEntityClient {
    private final String AUTH_HEADER = JandiConstants.AUTH_HEADER;
    private final String ACCEPT_HEADER = "Accept";

    private JandiRestClient mJandiRestClient;

    public JandiEntityClient(JandiRestClient jandiRestClient, String token) {
        mJandiRestClient = jandiRestClient;
        mJandiRestClient.setHeader(AUTH_HEADER, token);
        mJandiRestClient.setHeader(ACCEPT_HEADER,
                JandiV2HttpMessageConverter.APPLICATION_VERSION_FULL_NAME);
    }

    /************************************************************
     * 팀 관리
     ************************************************************/
    public ResInvitation inviteTeamMember(String email) throws JandiNetworkException {
        try {
            return mJandiRestClient.inviteTeamMember(new ReqInvitation(email));
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    /************************************************************
     * Entity (Channel, Private Group, Direct Message) 관련
     ************************************************************/
    public ResLeftSideMenu getTotalEntitiesInfo() throws JandiNetworkException {
        try {
            return mJandiRestClient.getInfosForSideMenu();
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon createPublicTopic(String entityName) throws JandiNetworkException {
        ReqCreateEntity reqCreateEntity = new ReqCreateEntity();
        reqCreateEntity.name = entityName;
        try {
            return mJandiRestClient.createChannel(reqCreateEntity);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon createPrivateGroup(String entityName) throws JandiNetworkException {
        ReqCreateEntity reqCreateEntity = new ReqCreateEntity();
        reqCreateEntity.name = entityName;
        try {
            return mJandiRestClient.createPrivateGroup(reqCreateEntity);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon joinChannel(ResLeftSideMenu.Channel channel) throws JandiNetworkException {
        try {
            return mJandiRestClient.joinChannel(channel.id);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon leaveChannel(int id) throws JandiNetworkException {
        try {
            return mJandiRestClient.leaveChannel(id);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon leavePrivateGroup(int id) throws JandiNetworkException {
        try {
            return mJandiRestClient.leaveGroup(id);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon modifyChannelName(int id, String name) throws JandiNetworkException {
        ReqCreateEntity entityInfo = new ReqCreateEntity();
        entityInfo.name = name;
        try {
            return mJandiRestClient.modifyChannel(entityInfo, id);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon modifyPrivateGroupName(int id, String name) throws JandiNetworkException {
        ReqCreateEntity entityInfo = new ReqCreateEntity();
        entityInfo.name = name;
        try {
            return mJandiRestClient.modifyGroup(entityInfo, id);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon deleteChannel(int id) throws JandiNetworkException {
        try {
            return mJandiRestClient.deleteChannel(id);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon deletePrivateGroup(int id) throws JandiNetworkException {
        try {
            return mJandiRestClient.deleteGroup(id);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon inviteChannel(int id, List<Integer> invitedUsers) throws JandiNetworkException {
        ReqInviteUsers reqInviteUsers = new ReqInviteUsers(invitedUsers);
        try {
            return mJandiRestClient.inviteChannel(id, reqInviteUsers);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon invitePrivateGroup(int id, List<Integer> invitedUsers) throws JandiNetworkException {
        ReqInviteUsers reqInviteUsers = new ReqInviteUsers(invitedUsers);
        try {
            return mJandiRestClient.inviteGroup(id, reqInviteUsers);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    /************************************************************
     * Entity 즐겨찾기 등록 / 해제
     ************************************************************/
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

    /************************************************************
     * 사용자 프로필
     ************************************************************/
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

    /************************************************************
     * Push Notification Token
     ************************************************************/
    public ResCommon registerNotificationToken(String oldDevToken, String newDevToken) throws JandiNetworkException {
        ReqNotificationRegister req = new ReqNotificationRegister("android", oldDevToken, newDevToken);
        try {
            return mJandiRestClient.registerNotificationToken(req);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon deleteNotificationToken(String regId) throws JandiNetworkException {
        try {
            return mJandiRestClient.deleteNotificationToken(regId);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon subscribeNotification(String regId, boolean isSubscribe) throws JandiNetworkException {
        ReqNotificationSubscribe req = new ReqNotificationSubscribe(isSubscribe);

        try {
            return mJandiRestClient.subscribeNotification(regId, req);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon setNotificationTarget(String target) throws JandiNetworkException {
        ReqNotificationTarget req = new ReqNotificationTarget(target);
        try {
            return mJandiRestClient.setNotificationTarget(req);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }


    /************************************************************
     * File 관련
     ************************************************************/
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
