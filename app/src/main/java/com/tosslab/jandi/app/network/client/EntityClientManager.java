package com.tosslab.jandi.app.network.client;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.file.FileApi;
import com.tosslab.jandi.app.network.client.main.LeftSideApi;
import com.tosslab.jandi.app.network.client.messages.MessageApi;
import com.tosslab.jandi.app.network.client.messages.comments.CommentApi;
import com.tosslab.jandi.app.network.client.privatetopic.GroupApi;
import com.tosslab.jandi.app.network.client.profile.ProfileApi;
import com.tosslab.jandi.app.network.client.publictopic.ChannelApi;
import com.tosslab.jandi.app.network.client.settings.StarredEntityApi;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqDeleteTopic;
import com.tosslab.jandi.app.network.models.ReqInviteTopicUsers;
import com.tosslab.jandi.app.network.models.ReqModifyTopicAutoJoin;
import com.tosslab.jandi.app.network.models.ReqModifyTopicDescription;
import com.tosslab.jandi.app.network.models.ReqModifyTopicName;
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

import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;


/**
 * Created by justinygchoi on 2014. 8. 27..
 */
@EBean
public class EntityClientManager {

    @Inject
    Lazy<LeftSideApi> leftSideApi;
    @Inject
    Lazy<ChannelApi> channelApi;
    @Inject
    Lazy<GroupApi> groupApi;
    @Inject
    Lazy<StarredEntityApi> starredEntityApi;
    @Inject
    Lazy<ProfileApi> profileApi;
    @Inject
    Lazy<MessageApi> messageApi;
    @Inject
    Lazy<CommentApi> commentApi;
    @Inject
    Lazy<FileApi> fileApi;
    private long selectedTeamId;

    @AfterInject
    void initAuthentication() {
        ResAccountInfo.UserTeam selectedTeamInfo = AccountRepository.getRepository().getSelectedTeamInfo();
        if (selectedTeamInfo == null) {
            return;
        }
        selectedTeamId = selectedTeamInfo.getTeamId();

        DaggerApiClientComponent
                .builder()
                .build()
                .inject(this);
    }

    /**
     * *********************************************************
     * Entity (Channel, Private Group, Direct Message) 관련
     * **********************************************************
     */

    public ResLeftSideMenu getTotalEntitiesInfo() throws RetrofitException {
        return leftSideApi.get().getInfosForSideMenu(selectedTeamId);
    }

    public ResCommon createPublicTopic(String entityName, String topicDescription, boolean isAutojoin) throws RetrofitException {
        final ReqCreateTopic reqCreateTopic = new ReqCreateTopic();
        reqCreateTopic.teamId = selectedTeamId;
        reqCreateTopic.name = entityName;
        reqCreateTopic.description = topicDescription;
        reqCreateTopic.autoJoin = isAutojoin;
        return channelApi.get().createChannel(selectedTeamId, reqCreateTopic);
    }

    public ResCommon createPrivateGroup(String entityName, String topicDescription, boolean isAutojoin) throws RetrofitException {
        final ReqCreateTopic reqCreateTopic = new ReqCreateTopic();
        reqCreateTopic.teamId = selectedTeamId;
        reqCreateTopic.name = entityName;
        reqCreateTopic.description = topicDescription;
        reqCreateTopic.autoJoin = isAutojoin;
        return groupApi.get().createPrivateGroup(selectedTeamId, reqCreateTopic);
    }

    public ResCommon joinChannel(long id) throws RetrofitException {
        return channelApi.get().joinTopic(id, new ReqDeleteTopic(selectedTeamId));
    }

    public ResCommon leaveChannel(final long id) throws RetrofitException {
        return channelApi.get().leaveTopic(id, new ReqDeleteTopic(selectedTeamId));
    }

    public ResCommon leavePrivateGroup(final long id) throws RetrofitException {
        return groupApi.get().leaveGroup(id, new ReqTeam(selectedTeamId));
    }

    public ResCommon modifyChannelName(final long id, String name) throws RetrofitException {
        ReqModifyTopicName entityInfo = new ReqModifyTopicName();
        entityInfo.teamId = selectedTeamId;
        entityInfo.name = name;
        return channelApi.get().modifyPublicTopicName(selectedTeamId, entityInfo, id);
    }

    public ResCommon modifyPrivateGroupName(final long id, String name) throws RetrofitException {
        ReqModifyTopicName entityInfo = new ReqModifyTopicName();
        entityInfo.teamId = selectedTeamId;
        entityInfo.name = name;
        return groupApi.get().modifyGroupName(selectedTeamId, entityInfo, id);
    }

    public ResCommon deleteChannel(final long id) throws RetrofitException {
        return channelApi.get().deleteTopic(id, new ReqDeleteTopic(selectedTeamId));
    }

    public ResCommon deletePrivateGroup(final long id) throws RetrofitException {
        return groupApi.get().deleteGroup(selectedTeamId, id);
    }

    public ResCommon inviteChannel(final long id, final List<Long> invitedUsers) throws RetrofitException {
        return channelApi.get().invitePublicTopic(id, new ReqInviteTopicUsers(invitedUsers, selectedTeamId));
    }

    public ResCommon invitePrivateGroup(final long id, final List<Long> invitedUsers) throws RetrofitException {
        return groupApi.get().inviteGroup(id, new ReqInviteTopicUsers(invitedUsers, selectedTeamId));
    }

    public ResCommon enableFavorite(final long entityId) throws RetrofitException {
        return starredEntityApi.get().enableFavorite(new ReqTeam(selectedTeamId), entityId);
    }

    public ResCommon disableFavorite(final long entityId) throws RetrofitException {
        return starredEntityApi.get().disableFavorite(selectedTeamId, entityId);
    }

    public ResLeftSideMenu.User getUserProfile(final long entityId) throws RetrofitException {
        return profileApi.get().getMemberProfile(selectedTeamId, entityId);
    }

    public ResLeftSideMenu.User updateUserProfile(final long entityId, final ReqUpdateProfile reqUpdateProfile) throws RetrofitException {
        return profileApi.get().updateMemberProfile(entityId, reqUpdateProfile);
    }

    public ResCommon updateMemberName(final long entityId, final ReqProfileName profileName) throws RetrofitException {
        return profileApi.get().updateMemberName(entityId, profileName);
    }

    public ResLeftSideMenu.User updateMemberEmail(long entityId, String email) throws RetrofitException {
        return profileApi.get().updateMemberEmail(entityId, new ReqAccountEmail(email));
    }

    public ResFileDetail getFileDetail(final long messageId) throws RetrofitException {
        return messageApi.get().getFileDetail(selectedTeamId, messageId);
    }

    public ResCommon sendMessageComment(final long messageId, String comment, List<MentionObject> mentions) throws RetrofitException {
        ReqSendComment reqSendComment = new ReqSendComment(comment, mentions);
        return commentApi.get().sendMessageComment(messageId, selectedTeamId, reqSendComment);
    }

    public ResCommon shareMessage(final long messageId, long cdpIdToBeShared) throws RetrofitException {
        final ReqShareMessage reqShareMessage = new ReqShareMessage();
        reqShareMessage.shareEntity = cdpIdToBeShared;
        reqShareMessage.teamId = selectedTeamId;
        return messageApi.get().shareMessage(reqShareMessage, messageId);
    }

    public ResCommon unshareMessage(final long messageId, long cdpIdToBeunshared) throws RetrofitException {
        final ReqUnshareMessage reqUnshareMessage = new ReqUnshareMessage(selectedTeamId, cdpIdToBeunshared);
        return messageApi.get().unshareMessage(reqUnshareMessage, messageId);
    }

    public ResCommon deleteMessageComment(final long messageId, final long feedbackId) throws RetrofitException {
        return commentApi.get().deleteMessageComment(selectedTeamId, feedbackId, messageId);
    }

    public ResCommon deleteFile(final long fileId) throws RetrofitException {
        return fileApi.get().deleteFile(fileId, selectedTeamId);
    }

    public ResCommon modifyChannelDescription(long entityId, String description) throws RetrofitException {
        ReqModifyTopicDescription entityInfo = new ReqModifyTopicDescription();
        entityInfo.teamId = selectedTeamId;
        entityInfo.description = description;
        return channelApi.get().modifyPublicTopicDescription(selectedTeamId, entityInfo, entityId);
    }

    public ResCommon modifyPrivateGroupDescription(long entityId, String description) throws RetrofitException {
        ReqModifyTopicDescription entityInfo = new ReqModifyTopicDescription();
        entityInfo.teamId = selectedTeamId;
        entityInfo.description = description;
        return groupApi.get().modifyGroupDescription(selectedTeamId, entityInfo, entityId);
    }

    public ResCommon modifyChannelAutoJoin(long entityId, boolean autoJoin) throws RetrofitException {
        ReqModifyTopicAutoJoin topicAutoJoin = new ReqModifyTopicAutoJoin();
        topicAutoJoin.teamId = selectedTeamId;
        topicAutoJoin.autoJoin = autoJoin;
        return channelApi.get().modifyPublicTopicAutoJoin(selectedTeamId, topicAutoJoin, entityId);
    }

    public long getSelectedTeamId() {
        return selectedTeamId;
    }
}
