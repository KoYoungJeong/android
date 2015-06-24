package com.tosslab.jandi.app.network.client.teams;

import com.tosslab.jandi.app.network.models.ReqCreateNewTeam;
import com.tosslab.jandi.app.network.models.ReqInvitationMembers;
import com.tosslab.jandi.app.network.models.ResInvitationMembers;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;

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

}
