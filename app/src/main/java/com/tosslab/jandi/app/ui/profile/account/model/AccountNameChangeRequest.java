package com.tosslab.jandi.app.ui.profile.account.model;

import android.content.Context;

import com.tosslab.jandi.app.network.client.settings.AccountProfileClient;
import com.tosslab.jandi.app.network.client.settings.AccountProfileClient_;
import com.tosslab.jandi.app.network.manager.Request;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;

/**
 * Created by Steve SeongUg Jung on 14. 12. 23..
 */
public class AccountNameChangeRequest implements Request<ResAccountInfo> {

    private final Context context;
    private final String name;

    private AccountNameChangeRequest(Context context, String name) {
        this.context = context;
        this.name = name;
    }

    public static AccountNameChangeRequest create(Context context, String name) {
        return new AccountNameChangeRequest(context, name);
    }

    @Override
    public ResAccountInfo request() throws JandiNetworkException {

        AccountProfileClient accountProfileClient = new AccountProfileClient_(context);
        accountProfileClient.setAuthentication(TokenUtil.getRequestAuthentication(context));

        return accountProfileClient.changeName(new ReqProfileName(name));

    }
}
