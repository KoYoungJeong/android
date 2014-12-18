package com.tosslab.jandi.app.ui.team.info.model;

import android.content.Context;

import com.tosslab.jandi.app.network.client.JandiRestClient;
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
    private final JandiRestClient jandiRestClient;
    private final ReqCreateNewTeam reqCreateNewTeam;

    private TeamCreateRequest(Context context, JandiRestClient jandiRestClient, ReqCreateNewTeam reqCreateNewTeam) {
        this.context = context;
        this.jandiRestClient = jandiRestClient;
        this.reqCreateNewTeam = reqCreateNewTeam;
    }

    public static TeamCreateRequest create(Context context, JandiRestClient jandiRestClient, ReqCreateNewTeam reqCreateNewTeam) {
        return new TeamCreateRequest(context, jandiRestClient, reqCreateNewTeam);
    }


    @Override
    public ResTeamDetailInfo request() throws JandiNetworkException {

        jandiRestClient.setAuthentication(TokenUtil.getRequestAuthentication(context));

        return jandiRestClient.createNewTeam(reqCreateNewTeam);
    }
}
