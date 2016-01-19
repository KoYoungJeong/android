package com.tosslab.jandi.app.network.client.teams;

import com.tosslab.jandi.app.network.manager.apiexecutor.IExecutor;
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
public interface ITeamApiLoader {

    IExecutor<ResTeamDetailInfo> loadCreateNewTeamByTeamApi(ReqCreateNewTeam req);

    IExecutor<ResLeftSideMenu.User> loadGetMemberProfileByTeamApi(long teamId, long memberId);

    IExecutor<List<ResInvitationMembers>> loadInviteToTeamByTeamApi(int teamId, ReqInvitationMembers invitationMembers);

    IExecutor<ResTeamDetailInfo.InviteTeam> loadGetTeamInfoByTeamApi(long teamId);

    IExecutor<ResAnnouncement> loadGetAnnouncement(int teamId, int topicId);

    IExecutor<ResCommon> loadCreateAnnouncement(int teamId, int topicId,
                                                ReqCreateAnnouncement reqCreateAnnouncement);

    IExecutor<ResCommon> loadUpdateAnnouncementStatus(int teamId, int memberId,
                                                      ReqUpdateAnnouncementStatus reqUpdateAnnouncementStatus);

    IExecutor<ResCommon> loadDeleteAnnouncement(int teamId, int topicId);

    IExecutor<ResMessages.OriginalMessage> loadGetMessage(long teamId, long topicId);

    IExecutor<ResStarMentioned> loadGetMentionedMessagesByTeamApi(int teamId, Integer messageId, int count);

    IExecutor<StarMentionedMessageObject> loadRegistStarredMessageByTeamApi(long teamId, long messageId);

    IExecutor<ResCommon> loadUnregistStarredMessageByTeamApi(long teamId, long messageId);

    IExecutor<ResStarMentioned> loadGetStarredMessagesByTeamApi(int teamId, Integer starredId,
                                                                int count, String type);

    IExecutor<ResCommon> loadUpdateTopicPushSubscribe(int teamId, int topicId,
                                                      ReqUpdateTopicPushSubscribe reqUpdateTopicPushSubscribe);

    IExecutor<ResCreateFolder> loadCreateFolderByTeamApi(int teamId, ReqCreateFolder reqCreateFolder);

    IExecutor<ResCommon> loadDeleteFolderByTeamApi(int teamId, int folderId);

    IExecutor<ResUpdateFolder> loadUpdateFolderByTeamApi(int teamId, int folderId,
                                                         ReqUpdateFolder reqUpdateFolder);

    IExecutor<List<ResFolder>> loadGetFoldersByTeamApi(int teamId);

    IExecutor<List<ResFolderItem>> loadGetFolderItemsByTeamApi(int teamId);

    IExecutor<ResRegistFolderItem> loadRegistFolderItemByTeamApi(int teamId, int folderId,
                                                                 ReqRegistFolderItem reqRegistFolderItem);

    IExecutor<ResCommon> loadDeleteFolderItemByTeamApi(int teamId, int folderId, int itemId);

    IExecutor<ResCommon> loadKickUserFromTopic(int teamId, int topicId, ReqMember member);

    IExecutor<ResCommon> loadAssignToTopicOwner(int teamId, int topicId, ReqOwner member);

    IExecutor<ResMessages.FileMessage> loadEnableFileExternalLink(long teamId, long fileId);

    IExecutor<ResMessages.FileMessage> loadDisableFileExternalLink(long teamId, long fileId);
}
