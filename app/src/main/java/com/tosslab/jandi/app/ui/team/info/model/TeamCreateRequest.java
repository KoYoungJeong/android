package com.tosslab.jandi.app.ui.team.info.model;

import android.content.Context;

import com.tosslab.jandi.app.network.client.teams.TeamsApiClient;
import com.tosslab.jandi.app.network.client.teams.TeamsApiClient_;
import com.tosslab.jandi.app.network.manager.Request;
import com.tosslab.jandi.app.network.models.ReqCreateNewTeam;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;

/**
 * Created by Steve SeongUg Jung on 14. 12. 18..
 */
public class TeamCreateRequest implements Request<ResTeamDetailInfo> {

    private final Context context;
    private final TeamsApiClient teamsApiClient;
    private final ReqCreateNewTeam reqCreateNewTeam;

    private TeamCreateRequest(Context context, TeamsApiClient teamsApiClient, ReqCreateNewTeam reqCreateNewTeam) {
        this.context = context;
        this.teamsApiClient = teamsApiClient;
        this.reqCreateNewTeam = reqCreateNewTeam;
    }

    public static TeamCreateRequest create(Context context, ReqCreateNewTeam reqCreateNewTeam) {
        return new TeamCreateRequest(context, new TeamsApiClient_(context), reqCreateNewTeam);
    }


    @Override
    public ResTeamDetailInfo request() throws JandiNetworkException {

        teamsApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));

        return teamsApiClient.createNewTeam(reqCreateNewTeam);
    }
}
