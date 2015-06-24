package com.tosslab.jandi.app.network.client.teams;

import com.tosslab.jandi.app.network.manager.ApiExecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ReqCreateNewTeam;
import com.tosslab.jandi.app.network.models.ReqInvitationMembers;

/**
 * Created by tee on 15. 6. 23..
 */
public interface ITeamApiLoader {

    IExecutor loadCreateNewTeamByTeamApi(ReqCreateNewTeam req);

    IExecutor loadGetMemberProfileByTeamApi(int teamId, int memberId);

    IExecutor loadInviteToTeamByTeamApi(int teamId, ReqInvitationMembers invitationMembers);

    IExecutor loadGetTeamInfoByTeamApi(int teamId);

}
