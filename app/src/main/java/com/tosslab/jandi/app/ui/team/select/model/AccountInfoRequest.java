package com.tosslab.jandi.app.ui.team.select.model;

import android.content.Context;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.client.JandiRestClient_;
import com.tosslab.jandi.app.network.client.JandiRestV2Client;
import com.tosslab.jandi.app.network.client.RestAdapterFactory;
import com.tosslab.jandi.app.network.manager.Request;
import com.tosslab.jandi.app.network.models.ResAccountInfo;

/**
 * Created by Steve SeongUg Jung on 14. 12. 17..
 */
public class AccountInfoRequest implements Request<ResAccountInfo> {

    private final Context context;

    private AccountInfoRequest(Context context, JandiRestClient jandiRestClient) {
        this.context = context;
    }

    public static AccountInfoRequest create(Context context) {
        return new AccountInfoRequest(context, new JandiRestClient_(context));
    }

    @Override
    public ResAccountInfo request() {
        return RestAdapterFactory.getRestAdapter(JandiConstants.REST_TYPE_AUTH).create(JandiRestV2Client.class).getAccountInfo();
    }
}