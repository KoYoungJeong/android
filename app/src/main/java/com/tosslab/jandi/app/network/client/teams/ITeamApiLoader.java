package com.tosslab.jandi.app.network.client.teams;

import com.tosslab.jandi.app.network.manager.apiexecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ReqCreateAnnouncement;
import com.tosslab.jandi.app.network.models.ReqCreateNewTeam;
import com.tosslab.jandi.app.network.models.ReqInvitationMembers;
import com.tosslab.jandi.app.network.models.ReqUpdateAnnouncementStatus;
import com.tosslab.jandi.app.network.models.ResAnnouncement;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResInvitationMembers;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResStarMentioned;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.network.models.commonobject.StarMentionedMessageObject;

import java.util.List;

/**
 * Created by tee on 15. 6. 23..
 */
public interface ITeamApiLoader {

    IExecutor<ResTeamDetailInfo> loadCreateNewTeamByTeamApi(ReqCreateNewTeam req);

    IExecutor<ResLeftSideMenu.User> loadGetMemberProfileByTeamApi(int teamId, int memberId);

    IExecutor<List<ResInvitationMembers>> loadInviteToTeamByTeamApi(int teamId, ReqInvitationMembers invitationMembers);

    IExecutor<ResTeamDetailInfo.InviteTeam> loadGetTeamInfoByTeamApi(int teamId);

    IExecutor<ResAnnouncement> loadGetAnnouncement(int teamId, int topicId);

    IExecutor<ResCommon> loadCreateAnnouncement(int teamId, int topicId, ReqCreateAnnouncement reqCreateAnnouncement);

    IExecutor<ResCommon> loadUpdateAnnouncementStatus(int teamId, int memberId, ReqUpdateAnnouncementStatus reqUpdateAnnouncementStatus);

    IExecutor<ResCommon> loadDeleteAnnouncement(int teamId, int topicId);

    IExecutor<ResMessages.OriginalMessage> loadGetMessage(int teamId, int topicId);

    IExecutor<ResStarMentioned> loadGetMentionedMessagesByTeamApi(int teamId, Integer messageId, int count);

    IExecutor<StarMentionedMessageObject> loadRegistStarredMessageByTeamApi(int teamId, int messageId);

    IExecutor<ResCommon> loadUnregistStarredMessageByTeamApi(int teamId, int messageId);

    IExecutor<ResStarMentioned> loadGetStarredMessagesByTeamApi(int teamId, Integer starredId,
                                                                int count, String type);

}
