package com.tosslab.jandi.app.ui.team.select.model;

import android.content.Context;

import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.client.JandiRestClient_;
import com.tosslab.jandi.app.network.manager.Request;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;

/**
 * Created by Steve SeongUg Jung on 14. 12. 17..
 */
public class AccountInfoRequest implements Request<ResAccountInfo> {

    private final Context context;
    private final JandiRestClient jandiRestClient;

    private AccountInfoRequest(Context context, JandiRestClient jandiRestClient) {
        this.context = context;
        this.jandiRestClient = jandiRestClient;
    }

    public static AccountInfoRequest create(Context context) {
        return new AccountInfoRequest(context, new JandiRestClient_(context));
    }

    @Override
    public ResAccountInfo request() throws JandiNetworkException {

        jandiRestClient.setAuthentication(TokenUtil.getRequestAuthentication(context));

        return jandiRestClient.getAccountInfo();
    }
}
