package com.tosslab.jandi.app.ui.profile.account.model;

import android.content.Context;

import com.tosslab.jandi.app.network.client.settings.AccountProfileClient;
import com.tosslab.jandi.app.network.client.settings.AccountProfileClient_;
import com.tosslab.jandi.app.network.manager.Request;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;

/**
 * Created by Steve SeongUg Jung on 14. 12. 23..
 */
public class PrimaryEmailChangeRequest implements Request<ResAccountInfo> {

    private final Context context;
    private final String email;

    private PrimaryEmailChangeRequest(Context context, String email) {
        this.context = context;
        this.email = email;
    }

    public static PrimaryEmailChangeRequest create(Context context, String email) {
        return new PrimaryEmailChangeRequest(context, email);
    }

    @Override
    public ResAccountInfo request() throws JandiNetworkException {

        AccountProfileClient accountProfileClient = new AccountProfileClient_(context);
        accountProfileClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
        return accountProfileClient.changePrimaryEmail(new ReqAccountEmail(email));
    }
}
