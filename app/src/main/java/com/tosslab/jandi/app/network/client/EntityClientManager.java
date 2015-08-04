package com.tosslab.jandi.app.network.client;

import android.content.Context;
import android.util.Log;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqDeleteTopic;
import com.tosslab.jandi.app.network.models.ReqInviteTopicUsers;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ReqSendComment;
import com.tosslab.jandi.app.network.models.ReqShareMessage;
import com.tosslab.jandi.app.network.models.ReqTeam;
import com.tosslab.jandi.app.network.models.ReqUnshareMessage;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by justinygchoi on 2014. 8. 27..
 */
@EBean
public class EntityClientManager {

    @RootContext
    Context context;
    private int selectedTeamId;

    @AfterInject
    void initAuthentication() {
        ResAccountInfo.UserTeam selectedTeamInfo = AccountRepository.getRepository().getSelectedTeamInfo();
        selectedTeamId = selectedTeamInfo.getTeamId();
    }

    /**
     * *********************************************************
     * Entity (Channel, Private Group, Direct Message) 관련
     * **********************************************************
     */

    public ResLeftSideMenu getTotalEntitiesInfo() throws RetrofitError {
        return RequestApiManager.getInstance().getInfosForSideMenuByMainRest(selectedTeamId);
    }

    public ResCommon createPublicTopic(String entityName, String topicDescription) throws RetrofitError {
        final ReqCreateTopic reqCreateTopic = new ReqCreateTopic();
        reqCreateTopic.teamId = selectedTeamId;
        reqCreateTopic.name = entityName;
        reqCreateTopic.description = topicDescription;
        return RequestApiManager.getInstance().createChannelByChannelApi(reqCreateTopic);
    }

    public ResCommon createPrivateGroup(String entityName, String topicDescription) throws RetrofitError {
        final ReqCreateTopic reqCreateTopic = new ReqCreateTopic();
        reqCreateTopic.teamId = selectedTeamId;
        reqCreateTopic.name = entityName;
        reqCreateTopic.description = topicDescription;
        return RequestApiManager.getInstance().createPrivateGroupByGroupApi(reqCreateTopic);
    }

    public ResCommon joinChannel(int id) throws RetrofitError {
        return RequestApiManager.getInstance().joinTopicByChannelApi(id, new ReqDeleteTopic(selectedTeamId));
    }

    public ResCommon leaveChannel(final int id) throws RetrofitError {
        return RequestApiManager.getInstance().leaveTopicByChannelApi(id, new ReqDeleteTopic(selectedTeamId));
    }

    public ResCommon leavePrivateGroup(final int id) throws RetrofitError {
        return RequestApiManager.getInstance().leaveGroupByGroupApi(id, new ReqTeam(selectedTeamId));
    }

    public ResCommon modifyChannelName(final int id, String name) throws RetrofitError {
        final ReqCreateTopic entityInfo = new ReqCreateTopic();
        entityInfo.teamId = selectedTeamId;
        entityInfo.name = name;
        return RequestApiManager.getInstance().modifyPublicTopicNameByChannelApi(entityInfo, id);
    }

    public ResCommon modifyPrivateGroupName(final int id, String name) throws RetrofitError {
        final ReqCreateTopic entityInfo = new ReqCreateTopic();
        entityInfo.teamId = selectedTeamId;
        entityInfo.name = name;
        return RequestApiManager.getInstance().modifyGroupByGroupApi(entityInfo, id);
    }

    public ResCommon deleteChannel(final int id) throws RetrofitError {
        return RequestApiManager.getInstance().deleteTopicByChannelApi(id, new ReqDeleteTopic(selectedTeamId));
    }

    public ResCommon deletePrivateGroup(final int id) throws RetrofitError {
        return RequestApiManager.getInstance().deleteGroupByGroupApi(selectedTeamId, id);
    }

    public ResCommon inviteChannel(final int id, final List<Integer> invitedUsers) throws RetrofitError {
        return RequestApiManager.getInstance().invitePublicTopicByChannelApi(id, new ReqInviteTopicUsers(invitedUsers, selectedTeamId));
    }

    public ResCommon invitePrivateGroup(final int id, final List<Integer> invitedUsers) throws RetrofitError {
        return RequestApiManager.getInstance().inviteGroupByGroupApi(id, new ReqInviteTopicUsers(invitedUsers, selectedTeamId));
    }

    /**
     * *********************************************************
     * Entity 즐겨찾기 등록 / 해제
     * **********************************************************
     */
    public ResCommon enableFavorite(final int entityId) throws RetrofitError {
        return RequestApiManager.getInstance().enableFavoriteByStarredEntityApi(new ReqTeam(selectedTeamId), entityId);
    }

    public ResCommon disableFavorite(final int entityId) throws RetrofitError {
        return RequestApiManager.getInstance().disableFavoriteByStarredEntityApi(selectedTeamId, entityId);
    }

    /**
     * *********************************************************
     * 사용자 프로필
     * **********************************************************
     */
    public ResLeftSideMenu.User getUserProfile(final int entityId) throws RetrofitError {
        return RequestApiManager.getInstance().getMemberProfileByTeamApi(selectedTeamId, entityId);
    }

    public ResLeftSideMenu.User updateUserProfile(final int entityId, final ReqUpdateProfile reqUpdateProfile) throws RetrofitError {
        return RequestApiManager.getInstance().updateMemberProfileByProfileApi(entityId, reqUpdateProfile);
    }

    public ResCommon updateMemberName(final int entityId, final ReqProfileName profileName) throws RetrofitError {
        return RequestApiManager.getInstance().updateMemberNameByProfileApi(entityId, profileName);
    }

    public ResLeftSideMenu.User updateMemberEmail(int entityId, String email) throws RetrofitError {
        return RequestApiManager.getInstance().updateMemberEmailByProfileApi(entityId, new ReqAccountEmail(email));
    }

//    /**
//     * *********************************************************
//     * Push Notification Token
//     * **********************************************************
//     */
//    @Deprecated
//    public ResAccountInfo registerNotificationToken(String oldDevToken, String newDevToken) throws RetrofitError {
//        ReqNotificationRegister req = new ReqNotificationRegister("android", newDevToken);
//        return RequestApiManager.getInstance().registerNotificationTokenByAccountDeviceApi(req);
//    }
//
//    @Deprecated
//    public ResAccountInfo deleteNotificationToken(String regId) throws RetrofitError {
//        return RequestApiManager.getInstance().deleteNotificationTokenByAccountDeviceApi(new ReqDeviceToken(regId));
//    }
//
//    @Deprecated
//    public ResAccountInfo subscribeNotification(final String regId, final boolean isSubscribe) throws RetrofitError {
//        ReqNotificationSubscribe req = new ReqNotificationSubscribe(isSubscribe);
//        return RequestApiManager.getInstance().subscribeStateNotificationByAccountDeviceApi(new ReqSubscibeToken(regId, isSubscribe));
//    }

    /**
     * *********************************************************
     * File 관련
     * **********************************************************
     */
    public ResFileDetail getFileDetail(final int messageId) throws RetrofitError {
        return RequestApiManager.getInstance().getFileDetailByMessagesApiAuth(selectedTeamId, messageId);
    }

    public ResCommon sendMessageComment(final int messageId, String comment, List<MentionObject> mentions) throws RetrofitError {

        final ReqSendComment reqSendComment = new ReqSendComment(comment, mentions);

        Log.e("reqSendComment", reqSendComment.toString());

        return RequestApiManager.getInstance().sendMessageCommentByCommentsApi(messageId, selectedTeamId,
                reqSendComment);
    }

    public ResCommon shareMessage(final int messageId, int cdpIdToBeShared) throws RetrofitError {
        final ReqShareMessage reqShareMessage = new ReqShareMessage();
        reqShareMessage.shareEntity = cdpIdToBeShared;
        reqShareMessage.teamId = selectedTeamId;
        return RequestApiManager.getInstance().shareMessageByMessagesApiAuth(reqShareMessage, messageId);
    }

    public ResCommon unshareMessage(final int messageId, int cdpIdToBeunshared) throws RetrofitError {
        final ReqUnshareMessage reqUnshareMessage = new ReqUnshareMessage(selectedTeamId, cdpIdToBeunshared);
        return RequestApiManager.getInstance().unshareMessageByMessagesApiAuth(reqUnshareMessage, messageId);
    }

//    public ResCommon modifyMessageComment(final int messageId, String comment, final int feedbackId)
//            throws RetrofitError {
//        final ReqSendComment reqModifyComment = new ReqSendComment();
//        reqModifyComment.teamId = selectedTeamId;
//        reqModifyComment.comment = comment;
//        return RequestApiManager.getInstance().modifyMessageCommentByCommentsApi(reqModifyComment, feedbackId, messageId);
//    }

    public ResCommon deleteMessageComment(final int messageId, final int feedbackId) throws RetrofitError {
        return RequestApiManager.getInstance().deleteMessageCommentByCommentsApi(selectedTeamId, feedbackId, messageId);
    }

    public ResCommon deleteFile(final int fileId) throws RetrofitError {
        return RequestApiManager.getInstance().deleteFileByFileApi(selectedTeamId, fileId);
    }

    public ResCommon modifyChannelDescription(int entityId, String description) throws RetrofitError {
        final ReqCreateTopic entityInfo = new ReqCreateTopic();
        entityInfo.teamId = selectedTeamId;
        entityInfo.description = description;
        return RequestApiManager.getInstance().modifyPublicTopicNameByChannelApi(entityInfo, entityId);
    }

    public ResCommon modifyPrivateGroupDescription(int entityId, String description) throws RetrofitError {
        final ReqCreateTopic entityInfo = new ReqCreateTopic();
        entityInfo.teamId = selectedTeamId;
        entityInfo.description = description;
        return RequestApiManager.getInstance().modifyGroupByGroupApi(entityInfo, entityId);
    }

    public int getSelectedTeamId() {
        return selectedTeamId;
    }
}
