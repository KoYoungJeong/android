package com.tosslab.jandi.app.ui.team.select.model;

import android.content.Context;

import com.tosslab.jandi.app.network.client.invitation.InvitationApiClient;
import com.tosslab.jandi.app.network.client.invitation.InvitationApiClient_;
import com.tosslab.jandi.app.network.manager.Request;
import com.tosslab.jandi.app.network.models.ReqInvitationConfirm;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;

/**
 * Created by Steve SeongUg Jung on 14. 12. 18..
 */
public class AcceptInviteRequest implements Request<ResTeamDetailInfo> {

    private final Context context;
    private final InvitationApiClient invitationApiClient;
    private final String teamToken;
    private final String email;
    private final String myName;
    private final String invitationId;


    private AcceptInviteRequest(Context context, InvitationApiClient invitationApiClient, String teamToken, String email, String myName, String invitationId) {
        this.context = context;
        this.invitationApiClient = invitationApiClient;
        this.teamToken = teamToken;
        this.email = email;
        this.myName = myName;
        this.invitationId = invitationId;
    }

    public static AcceptInviteRequest create(Context context, String teamToken, String email, String myName, String invitationId) {
        return new AcceptInviteRequest(context, new InvitationApiClient_(context), teamToken, email, myName, invitationId);
    }


    @Override
    public ResTeamDetailInfo request() throws JandiNetworkException {

        invitationApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));

        //ReqInvitationConfirmOrIgnore reqInvitationConfirmOrIgnore = new ReqInvitationConfirmOrIgnore(ReqInvitationConfirmOrIgnore.Type.ACCEPT.getType());
        //return invitationApiClient.confirmOrDeclineInvitation(reqInvitationConfirmOrIgnore, invitationId);

        ReqInvitationConfirm reqInvitationConfirm = new ReqInvitationConfirm(teamToken, ReqInvitationConfirm.Type.ACCEPT.getType(), myName, email);
        return invitationApiClient.confirmInvitation(reqInvitationConfirm);
    }
}
