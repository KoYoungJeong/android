package com.tosslab.jandi.app.network.client.teams;

import com.tosslab.jandi.app.network.models.ReqCreateAnnouncement;
import com.tosslab.jandi.app.network.models.ReqCreateFolder;
import com.tosslab.jandi.app.network.models.ReqCreateNewTeam;
import com.tosslab.jandi.app.network.models.ReqInvitationMembers;
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

    ResLeftSideMenu.User getMemberProfileByTeamApi(int teamId, int memberId) throws RetrofitError;

    List<ResInvitationMembers> inviteToTeamByTeamApi(int teamId, ReqInvitationMembers invitationMembers) throws RetrofitError;

    ResTeamDetailInfo.InviteTeam getTeamInfoByTeamApi(int teamId) throws RetrofitError;

    ResAnnouncement getAnnouncement(int teamId, int topicId) throws RetrofitError;

    ResCommon createAnnouncement(int teamId, int topicId, ReqCreateAnnouncement reqCreateAnnouncement) throws RetrofitError;

    ResCommon updateAnnouncementStatus(int teamId, int memberId, ReqUpdateAnnouncementStatus reqUpdateAnnouncementStatus) throws RetrofitError;

    ResCommon deleteAnnouncement(int teamId, int topicId) throws RetrofitError;

    ResMessages.OriginalMessage getMessage(int teamId, int messageId) throws RetrofitError;

    ResStarMentioned getMentionedMessagesByTeamApi(int teamId, Integer messageId, int count) throws RetrofitError;

    StarMentionedMessageObject registStarredMessageByTeamApi(int teamId, int messageId) throws RetrofitError;

    ResCommon unregistStarredMessageByTeamApi(int teamId, int messageId) throws RetrofitError;

    ResStarMentioned getStarredMessagesByTeamApi(int teamId, Integer starredId,
                                                 int count, String type) throws RetrofitError;


    ResCommon updateTopicPushSubscribe(int teamId, int topicId, ReqUpdateTopicPushSubscribe reqUpdateTopicPushSubscribe) throws RetrofitError;

    ResCreateFolder createFolderByTeamApi(int teamId, ReqCreateFolder reqCreateFolder) throws RetrofitError;

    ResCommon deleteFolderByTeamApi(int teamId, int folderId) throws RetrofitError;

    ResUpdateFolder updateFolderByTeamApi(int teamId, int folderId, ReqUpdateFolder reqUpdateFolder) throws RetrofitError;

    List<ResFolder> getFoldersByTeamApi(int teamId) throws RetrofitError;

    List<ResFolderItem> getFolderItemsByTeamApi(int teamId) throws RetrofitError;

    ResRegistFolderItem registFolderItemByTeamApi(int teamId, int folderId, ReqRegistFolderItem reqRegistFolderItem) throws RetrofitError;

    ResCommon deleteFolderItemByTeamApi(int teamId, int folderId, int itemId) throws RetrofitError;

}
