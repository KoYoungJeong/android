package com.tosslab.jandi.app.network.client.teams;

import com.tosslab.jandi.app.network.manager.apiexecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ReqCreateNewTeam;
import com.tosslab.jandi.app.network.models.ReqInvitationMembers;
import com.tosslab.jandi.app.network.models.ResInvitationMembers;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;

import java.util.List;

/**
 * Created by tee on 15. 6. 23..
 */
public interface ITeamApiLoader {

    IExecutor<ResTeamDetailInfo> loadCreateNewTeamByTeamApi(ReqCreateNewTeam req);

    IExecutor<ResLeftSideMenu.User> loadGetMemberProfileByTeamApi(int teamId, int memberId);

    IExecutor<List<ResInvitationMembers>> loadInviteToTeamByTeamApi(int teamId, ReqInvitationMembers invitationMembers);

    IExecutor<ResTeamDetailInfo.InviteTeam> loadGetTeamInfoByTeamApi(int teamId);

}
