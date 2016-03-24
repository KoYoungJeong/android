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



/**
 * Created by tee on 15. 6. 23..
 */
public interface ITeamApiAuth {

    ResTeamDetailInfo createNewTeamByTeamApi(ReqCreateNewTeam req) throws IOException;

    ResLeftSideMenu.User getMemberProfileByTeamApi(long teamId, long memberId) throws IOException;

    List<ResInvitationMembers> inviteToTeamByTeamApi(long teamId, ReqInvitationMembers invitationMembers) throws IOException;

    ResTeamDetailInfo.InviteTeam getTeamInfoByTeamApi(long teamId) throws IOException;

    ResAnnouncement getAnnouncement(long teamId, long topicId) throws IOException;

    ResCommon createAnnouncement(long teamId, long topicId, ReqCreateAnnouncement reqCreateAnnouncement) throws IOException;

    ResCommon updateAnnouncementStatus(long teamId, long memberId, ReqUpdateAnnouncementStatus reqUpdateAnnouncementStatus) throws IOException;

    ResCommon deleteAnnouncement(long teamId, long topicId) throws IOException;

    ResMessages.OriginalMessage getMessage(long teamId, long messageId) throws IOException;

    ResStarMentioned getMentionedMessagesByTeamApi(long teamId, long messageId, int count) throws IOException;

    StarMentionedMessageObject registStarredMessageByTeamApi(long teamId, long messageId) throws IOException;

    ResCommon unregistStarredMessageByTeamApi(long teamId, long messageId) throws IOException;

    ResStarMentioned getStarredMessagesByTeamApi(long teamId, long starredId,
                                                 int count, String type) throws IOException;


    ResCommon updateTopicPushSubscribe(long teamId, long topicId, ReqUpdateTopicPushSubscribe reqUpdateTopicPushSubscribe) throws IOException;

    ResCreateFolder createFolderByTeamApi(long teamId, ReqCreateFolder reqCreateFolder) throws IOException;

    ResCommon deleteFolderByTeamApi(long teamId, long folderId) throws IOException;

    ResUpdateFolder updateFolderByTeamApi(long teamId, long folderId, ReqUpdateFolder reqUpdateFolder) throws IOException;

    List<ResFolder> getFoldersByTeamApi(long teamId) throws IOException;

    List<ResFolderItem> getFolderItemsByTeamApi(long teamId) throws IOException;

    ResRegistFolderItem registFolderItemByTeamApi(long teamId, long folderId, ReqRegistFolderItem reqRegistFolderItem) throws IOException;

    ResCommon deleteFolderItemByTeamApi(long teamId, long folderId, long itemId) throws IOException;

    ResCommon kickUserFromTopic(long teamId, long topicId, ReqMember member);

    ResCommon assignToTopicOwner(long teamId, long topicId, ReqOwner owner);

    ResMessages.FileMessage enableFileExternalLink(long teamId, long fileId);

    ResMessages.FileMessage disableFileExternalLink(long teamId, long fileId);

}
