package com.tosslab.jandi.app.ui.team.select.model;

import android.content.Context;

import com.tosslab.jandi.app.network.client.invitation.InvitationApiClient;
import com.tosslab.jandi.app.network.client.invitation.InvitationApiClient_;
import com.tosslab.jandi.app.network.manager.Request;
import com.tosslab.jandi.app.network.models.ReqInvitationConfirmOrIgnore;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.ui.team.select.to.Team;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;

/**
 * Created by Steve SeongUg Jung on 14. 12. 18..
 */
public class IgnoreInviteRequest implements Request<ResTeamDetailInfo> {

    private final Context context;
    private final InvitationApiClient invitationApiClient;
    private final Team team;

    private IgnoreInviteRequest(Context context, InvitationApiClient invitationApiClient, Team team) {
        this.context = context;
        this.invitationApiClient = invitationApiClient;
        this.team = team;
    }

    public static IgnoreInviteRequest create(Context context, Team team) {
        return new IgnoreInviteRequest(context, new InvitationApiClient_(context), team);
    }


    @Override
    public ResTeamDetailInfo request() throws JandiNetworkException {

        invitationApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));

        //ReqInvitationConfirm reqInvitationConfirm = new ReqInvitationConfirm(team.getToken(), ReqInvitationConfirm.Type.DECLINE.getType(), "", "");
        //return invitationApiClient.declineInvitation(reqInvitationConfirm);

        ReqInvitationConfirmOrIgnore reqInvitationConfirmOrIgnore = new ReqInvitationConfirmOrIgnore(ReqInvitationConfirmOrIgnore.Type.DECLINE.getType());
        return invitationApiClient.confirmOrDeclineInvitation(team.getInvitationId(), reqInvitationConfirmOrIgnore);
    }
}
