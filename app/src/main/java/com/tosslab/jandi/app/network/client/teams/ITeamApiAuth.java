package com.tosslab.jandi.app.network.client.teams;

import com.tosslab.jandi.app.network.models.ReqCreateAnnouncement;
import com.tosslab.jandi.app.network.models.ReqCreateFolder;
import com.tosslab.jandi.app.network.models.ReqCreateNewTeam;
import com.tosslab.jandi.app.network.models.ReqInvitationMembers;
import com.tosslab.jandi.app.network.models.ReqMember;
import com.tosslab.jandi.app.network.models.ReqOwner;
import com.tosslab.jandi.app.network.models.ReqRegistFolderItem;
import com.tosslab.jandi.app.network.models.ReqUpdateAnnouncementStatus;
import com.tosslab.jandi.app.network.models.ReqUpdateFolder;
import com.tosslab.jandi.app.network.models.ReqUpdateTopicPushSubscribe;
import com.tosslab.jandi.app.network.models.ResAnnouncement;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResCreateFolder;
import com.tosslab.jandi.app.network.models.ResFolder;
import com.tosslab.jandi.app.network.models.ResFolderItem;
import com.tosslab.jandi.app.network.models.ResInvitationMembers;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResRegistFolderItem;
import com.tosslab.jandi.app.network.models.ResStarMentioned;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.network.models.ResUpdateFolder;
import com.tosslab.jandi.app.network.models.commonobject.StarMentionedMessageObject;

import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 6. 23..
 */
public interface ITeamApiAuth {

    ResTeamDetailInfo createNewTeamByTeamApi(ReqCreateNewTeam req) throws RetrofitError;

    ResLeftSideMenu.User getMemberProfileByTeamApi(long teamId, long memberId) throws RetrofitError;

    List<ResInvitationMembers> inviteToTeamByTeamApi(long teamId, ReqInvitationMembers invitationMembers) throws RetrofitError;

    ResTeamDetailInfo.InviteTeam getTeamInfoByTeamApi(long teamId) throws RetrofitError;

    ResAnnouncement getAnnouncement(long teamId, long topicId) throws RetrofitError;

    ResCommon createAnnouncement(long teamId, long topicId, ReqCreateAnnouncement reqCreateAnnouncement) throws RetrofitError;

    ResCommon updateAnnouncementStatus(long teamId, long memberId, ReqUpdateAnnouncementStatus reqUpdateAnnouncementStatus) throws RetrofitError;

    ResCommon deleteAnnouncement(long teamId, long topicId) throws RetrofitError;

    ResMessages.OriginalMessage getMessage(long teamId, long messageId) throws RetrofitError;

    ResStarMentioned getMentionedMessagesByTeamApi(long teamId, long messageId, int count) throws RetrofitError;

    StarMentionedMessageObject registStarredMessageByTeamApi(long teamId, long messageId) throws RetrofitError;

    ResCommon unregistStarredMessageByTeamApi(long teamId, long messageId) throws RetrofitError;

    ResStarMentioned getStarredMessagesByTeamApi(long teamId, long starredId,
                                                 int count, String type) throws RetrofitError;


    ResCommon updateTopicPushSubscribe(long teamId, long topicId, ReqUpdateTopicPushSubscribe reqUpdateTopicPushSubscribe) throws RetrofitError;

    ResCreateFolder createFolderByTeamApi(long teamId, ReqCreateFolder reqCreateFolder) throws RetrofitError;

    ResCommon deleteFolderByTeamApi(long teamId, long folderId) throws RetrofitError;

    ResUpdateFolder updateFolderByTeamApi(long teamId, long folderId, ReqUpdateFolder reqUpdateFolder) throws RetrofitError;

    List<ResFolder> getFoldersByTeamApi(long teamId) throws RetrofitError;

    List<ResFolderItem> getFolderItemsByTeamApi(long teamId) throws RetrofitError;

    ResRegistFolderItem registFolderItemByTeamApi(long teamId, long folderId, ReqRegistFolderItem reqRegistFolderItem) throws RetrofitError;

    ResCommon deleteFolderItemByTeamApi(long teamId, long folderId, long itemId) throws RetrofitError;

    ResCommon kickUserFromTopic(long teamId, long topicId, ReqMember member);

    ResCommon assignToTopicOwner(long teamId, long topicId, ReqOwner owner);

    ResMessages.FileMessage enableFileExternalLink(long teamId, long fileId);

    ResMessages.FileMessage disableFileExternalLink(long teamId, long fileId);

}
