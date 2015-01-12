package com.tosslab.jandi.app.ui.profile.email.model;

import android.content.Context;

import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.client.JandiRestClient_;
import com.tosslab.jandi.app.network.manager.Request;
import com.tosslab.jandi.app.network.models.ReqUpdatePrimaryEmailInfo;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;

/**
 * Created by Steve SeongUg Jung on 15. 1. 13..
 */
public class EmailChooseRequest implements Request<ResAccountInfo> {

    private final Context context;
    private final String email;
    private final JandiRestClient jandiRestClient;

    private EmailChooseRequest(Context context, String email, JandiRestClient jandiRestClient) {
        this.context = context;
        this.email = email;
        this.jandiRestClient = jandiRestClient;
    }

    public static EmailChooseRequest create(Context context, String email) {
        return new EmailChooseRequest(context, email, new JandiRestClient_(context));
    }

    @Override
    public ResAccountInfo request() throws JandiNetworkException {
        jandiRestClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
        return jandiRestClient.updatePrimaryEmail(new ReqUpdatePrimaryEmailInfo(email));
    }
}
