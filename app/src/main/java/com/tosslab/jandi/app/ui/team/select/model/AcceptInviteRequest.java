package com.tosslab.jandi.app.ui.team.select.model;

import android.content.Context;

import com.tosslab.jandi.app.network.client.invitation.InvitationApiClient;
import com.tosslab.jandi.app.network.client.invitation.InvitationApiClient_;
import com.tosslab.jandi.app.network.manager.Request;
import com.tosslab.jandi.app.network.models.ReqInvitationConfirmOrIgnore;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;

/**
 * Created by Steve SeongUg Jung on 14. 12. 18..
 */
public class AcceptInviteRequest implements Request<ResTeamDetailInfo> {

    private final Context context;
    private final InvitationApiClient invitationApiClient;
    private final String invitationId;
    private final String type;


    private AcceptInviteRequest(Context context, InvitationApiClient invitationApiClient, String invitationId, String type) {
        this.context = context;
        this.invitationApiClient = invitationApiClient;
        this.invitationId = invitationId;
        this.type = type;
    }

    public static AcceptInviteRequest create(Context context, String invitationId, String type) {
        return new AcceptInviteRequest(context, new InvitationApiClient_(context), invitationId, type);
    }


    @Override
    public ResTeamDetailInfo request() throws JandiNetworkException {

        invitationApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));

        ReqInvitationConfirmOrIgnore reqInvitationConfirmOrIgnore = new ReqInvitationConfirmOrIgnore(ReqInvitationConfirmOrIgnore.Type.ACCEPT.getType());
        return invitationApiClient.confirmOrDeclineInvitation(invitationId, reqInvitationConfirmOrIgnore);

        //ReqInvitationConfirm reqInvitationConfirm = new ReqInvitationConfirm(teamToken, ReqInvitationConfirm.Type.ACCEPT.getType(), myName, email);
        //return invitationApiClient.confirmInvitation(reqInvitationConfirm);
    }
}
