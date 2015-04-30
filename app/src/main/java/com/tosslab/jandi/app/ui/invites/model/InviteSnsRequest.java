package com.tosslab.jandi.app.ui.invites.model;

import android.content.Context;

import com.tosslab.jandi.app.network.manager.Request;
import com.tosslab.jandi.app.utils.JandiNetworkException;

/**
 * Created by Bill Minwook Heo on 15. 4. 22..
 */
public class InviteSnsRequest implements Request<String> {

    private final Context context;
    private final int teamId;

    public InviteSnsRequest(Context context, int teamId) {
        this.context = context;
        this.teamId = teamId;
    }

    @Override
    public String request() throws JandiNetworkException {
        return null;
    }
}
