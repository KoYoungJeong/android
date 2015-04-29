package com.tosslab.jandi.app.ui.team.info.model;

import android.content.Context;

import com.tosslab.jandi.app.network.client.teams.TeamsApiClient;
import com.tosslab.jandi.app.network.client.teams.TeamsApiClient_;
import com.tosslab.jandi.app.network.manager.Request;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;

/**
 * Created by Bill Minwook Heo on 15. 4. 28..
 */
public class TeamInfoRequest implements Request<ResTeamDetailInfo.InviteTeam> {
    private final Context context;
    private final TeamsApiClient teamsApiClient;
    private final int teamId;

    private TeamInfoRequest(Context context, TeamsApiClient teamsApiClient1, int teamId) {
        this.context = context;
        this.teamsApiClient = teamsApiClient1;
        this.teamId = teamId;
    }

    public static TeamInfoRequest create(Context context, int teamId) {
        return new TeamInfoRequest(context, new TeamsApiClient_(context), teamId);
    }

    @Override
    public ResTeamDetailInfo.InviteTeam request() throws JandiNetworkException {
        teamsApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));

        return teamsApiClient.getTeamInfo(teamId);
    }
}
