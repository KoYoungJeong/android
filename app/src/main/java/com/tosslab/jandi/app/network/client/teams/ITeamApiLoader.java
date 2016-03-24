package com.tosslab.jandi.app.network.client.teams;

import com.tosslab.jandi.app.network.manager.apiexecutor.Executor;
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

    Executor<ResTeamDetailInfo> loadCreateNewTeamByTeamApi(ReqCreateNewTeam req);

    Executor<ResLeftSideMenu.User> loadGetMemberProfileByTeamApi(long teamId, long memberId);

    Executor<List<ResInvitationMembers>> loadInviteToTeamByTeamApi(long teamId, ReqInvitationMembers invitationMembers);

    Executor<ResTeamDetailInfo.InviteTeam> loadGetTeamInfoByTeamApi(long teamId);

    Executor<ResAnnouncement> loadGetAnnouncement(long teamId, long topicId);

    Executor<ResCommon> loadCreateAnnouncement(long teamId, long topicId,
                                                ReqCreateAnnouncement reqCreateAnnouncement);

    Executor<ResCommon> loadUpdateAnnouncementStatus(long teamId, long memberId,
                                                      ReqUpdateAnnouncementStatus reqUpdateAnnouncementStatus);

    Executor<ResCommon> loadDeleteAnnouncement(long teamId, long topicId);

    Executor<ResMessages.OriginalMessage> loadGetMessage(long teamId, long topicId);

    Executor<ResStarMentioned> loadGetMentionedMessagesByTeamApi(long teamId, long messageId, int count);

    Executor<StarMentionedMessageObject> loadRegistStarredMessageByTeamApi(long teamId, long messageId);

    Executor<ResCommon> loadUnregistStarredMessageByTeamApi(long teamId, long messageId);

    Executor<ResStarMentioned> loadGetStarredMessagesByTeamApi(long teamId, long messageId,
                                                                int count, String type);

    Executor<ResCommon> loadUpdateTopicPushSubscribe(long teamId, long topicId,
                                                      ReqUpdateTopicPushSubscribe reqUpdateTopicPushSubscribe);

    Executor<ResCreateFolder> loadCreateFolderByTeamApi(long teamId, ReqCreateFolder reqCreateFolder);

    Executor<ResCommon> loadDeleteFolderByTeamApi(long teamId, long folderId);

    Executor<ResUpdateFolder> loadUpdateFolderByTeamApi(long teamId, long folderId,
                                                         ReqUpdateFolder reqUpdateFolder);

    Executor<List<ResFolder>> loadGetFoldersByTeamApi(long teamId);

    Executor<List<ResFolderItem>> loadGetFolderItemsByTeamApi(long teamId);

    Executor<ResRegistFolderItem> loadRegistFolderItemByTeamApi(long teamId, long folderId,
                                                                 ReqRegistFolderItem reqRegistFolderItem);

    Executor<ResCommon> loadDeleteFolderItemByTeamApi(long teamId, long folderId, long itemId);

    Executor<ResCommon> loadKickUserFromTopic(long teamId, long topicId, ReqMember member);

    Executor<ResCommon> loadAssignToTopicOwner(long teamId, long topicId, ReqOwner owner);

    Executor<ResMessages.FileMessage> loadEnableFileExternalLink(long teamId, long fileId);

    Executor<ResMessages.FileMessage> loadDisableFileExternalLink(long teamId, long fileId);
}
