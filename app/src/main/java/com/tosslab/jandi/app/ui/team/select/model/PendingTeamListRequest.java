package com.tosslab.jandi.app.ui.team.select.model;

import android.content.Context;

import com.tosslab.jandi.app.network.client.invitation.InvitationApiClient;
import com.tosslab.jandi.app.network.client.invitation.InvitationApiClient_;
import com.tosslab.jandi.app.network.manager.Request;
import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 14. 12. 17..
 */
public class PendingTeamListRequest implements Request<List<ResPendingTeamInfo>> {


    private final Context context;
    private final InvitationApiClient invitationApiClient;

    private PendingTeamListRequest(Context context, InvitationApiClient invitationApiClient) {
        this.context = context;
        this.invitationApiClient = invitationApiClient;
    }

    public static PendingTeamListRequest create(Context context) {
        return new PendingTeamListRequest(context, new InvitationApiClient_(context));
    }

    @Override
    public List<ResPendingTeamInfo> request() throws JandiNetworkException {
        invitationApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));

        return invitationApiClient.getPedingTeamInfo();
    }
}
