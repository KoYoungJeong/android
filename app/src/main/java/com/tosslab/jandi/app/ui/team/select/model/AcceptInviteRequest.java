package com.tosslab.jandi.app.ui.team.select.model;

import android.content.Context;

import com.tosslab.jandi.app.network.client.invitation.InvitationApiClient;
import com.tosslab.jandi.app.network.client.invitation.InvitationApiClient_;
import com.tosslab.jandi.app.network.manager.Request;
import com.tosslab.jandi.app.network.models.ReqInvitationConfirm;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.ui.team.select.to.Team;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 14. 12. 18..
 */
public class AcceptInviteRequest implements Request<List<ResTeamDetailInfo>> {

    private final Context context;
    private final InvitationApiClient invitationApiClient;
    private final Team team;
    private final String myName;

    private AcceptInviteRequest(Context context, InvitationApiClient invitationApiClient, Team team, String myName) {
        this.context = context;
        this.invitationApiClient = invitationApiClient;
        this.team = team;
        this.myName = myName;
    }

    public static AcceptInviteRequest create(Context context, Team team, String myName) {
        return new AcceptInviteRequest(context, new InvitationApiClient_(context), team, myName);
    }


    @Override
    public List<ResTeamDetailInfo> request() throws JandiNetworkException {

        invitationApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));

        ReqInvitationConfirm reqInvitationConfirm = new ReqInvitationConfirm(team.getToken(), ReqInvitationConfirm.Type.ACCEPT.getType(), myName, team.getUserEmail());
        return invitationApiClient.confirmInvitation(reqInvitationConfirm);
    }
}
