package com.tosslab.jandi.app.ui.invites.model;

import android.content.Context;

import com.tosslab.jandi.app.network.client.invitation.InvitationApiClient;
import com.tosslab.jandi.app.network.client.invitation.InvitationApiClient_;
import com.tosslab.jandi.app.network.manager.Request;
import com.tosslab.jandi.app.network.models.ReqInvitationMembers;
import com.tosslab.jandi.app.network.models.ResInvitationMembers;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.LanguageUtil;
import com.tosslab.jandi.app.utils.TokenUtil;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 14. 12. 28..
 */
public class InviteRequest implements Request<List<ResInvitationMembers>> {

    private final Context context;
    private final int teamId;
    private final List<String> invites;

    public InviteRequest(Context context, int teamId, List<String> invites) {
        this.context = context;
        this.teamId = teamId;
        this.invites = invites;
    }

    @Override
    public List<ResInvitationMembers> request() throws JandiNetworkException {

        InvitationApiClient apiClient = new InvitationApiClient_(context);
        apiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));

        return apiClient.inviteMembers(new ReqInvitationMembers(teamId, invites, LanguageUtil.getLanguage(context)));
    }
}
