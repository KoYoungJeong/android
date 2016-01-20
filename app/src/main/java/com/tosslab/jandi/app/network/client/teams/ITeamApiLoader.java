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

    IExecutor<List<ResInvitationMembers>> loadInviteToTeamByTeamApi(long teamId, ReqInvitationMembers invitationMembers);

    IExecutor<ResTeamDetailInfo.InviteTeam> loadGetTeamInfoByTeamApi(long teamId);

    IExecutor<ResAnnouncement> loadGetAnnouncement(long teamId, long topicId);

    IExecutor<ResCommon> loadCreateAnnouncement(long teamId, long topicId,
                                                ReqCreateAnnouncement reqCreateAnnouncement);

    IExecutor<ResCommon> loadUpdateAnnouncementStatus(long teamId, long memberId,
                                                      ReqUpdateAnnouncementStatus reqUpdateAnnouncementStatus);

    IExecutor<ResCommon> loadDeleteAnnouncement(long teamId, long topicId);

    IExecutor<ResMessages.OriginalMessage> loadGetMessage(long teamId, long topicId);

    IExecutor<ResStarMentioned> loadGetMentionedMessagesByTeamApi(long teamId, long messageId, int count);

    IExecutor<StarMentionedMessageObject> loadRegistStarredMessageByTeamApi(long teamId, long messageId);

    IExecutor<ResCommon> loadUnregistStarredMessageByTeamApi(long teamId, long messageId);

    IExecutor<ResStarMentioned> loadGetStarredMessagesByTeamApi(long teamId, long messageId,
                                                                int count, String type);

    IExecutor<ResCommon> loadUpdateTopicPushSubscribe(long teamId, long topicId,
                                                      ReqUpdateTopicPushSubscribe reqUpdateTopicPushSubscribe);

    IExecutor<ResCreateFolder> loadCreateFolderByTeamApi(long teamId, ReqCreateFolder reqCreateFolder);

    IExecutor<ResCommon> loadDeleteFolderByTeamApi(long teamId, long folderId);

    IExecutor<ResUpdateFolder> loadUpdateFolderByTeamApi(long teamId, long folderId,
                                                         ReqUpdateFolder reqUpdateFolder);

    IExecutor<List<ResFolder>> loadGetFoldersByTeamApi(long teamId);

    IExecutor<List<ResFolderItem>> loadGetFolderItemsByTeamApi(long teamId);

    IExecutor<ResRegistFolderItem> loadRegistFolderItemByTeamApi(long teamId, long folderId,
                                                                 ReqRegistFolderItem reqRegistFolderItem);

    IExecutor<ResCommon> loadDeleteFolderItemByTeamApi(long teamId, long folderId, long itemId);

    IExecutor<ResCommon> loadKickUserFromTopic(long teamId, long topicId, ReqMember member);

    IExecutor<ResCommon> loadAssignToTopicOwner(long teamId, long topicId, ReqOwner owner);

    IExecutor<ResMessages.FileMessage> loadEnableFileExternalLink(long teamId, long fileId);

    IExecutor<ResMessages.FileMessage> loadDisableFileExternalLink(long teamId, long fileId);
}
