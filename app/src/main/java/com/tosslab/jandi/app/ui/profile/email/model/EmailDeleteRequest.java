package com.tosslab.jandi.app.ui.profile.email.model;

import android.content.Context;

import com.tosslab.jandi.app.network.client.account.emails.AccountEmailsApiClient;
import com.tosslab.jandi.app.network.client.account.emails.AccountEmailsApiClient_;
import com.tosslab.jandi.app.network.manager.Request;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.spring.JandiV2HttpMessageConverter;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;

/**
 * Created by Steve SeongUg Jung on 15. 1. 13..
 */
public class EmailDeleteRequest implements Request<ResAccountInfo> {

    private final Context context;
    private final String email;
    private final AccountEmailsApiClient accountEmailsApiClient;

    private EmailDeleteRequest(Context context, String email, AccountEmailsApiClient accountEmailsApiClient_) {
        this.context = context;
        this.email = email;
        this.accountEmailsApiClient = accountEmailsApiClient_;
    }

    public static EmailDeleteRequest create(Context context, String email) {
        return new EmailDeleteRequest(context, email, new AccountEmailsApiClient_(context));
    }

    @Override
    public ResAccountInfo request() throws JandiNetworkException {
        accountEmailsApiClient.setHeader("Accept", JandiV2HttpMessageConverter.APPLICATION_VERSION_FULL_NAME + ", application/json");
        accountEmailsApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
        return accountEmailsApiClient.deleteEmail(new ReqAccountEmail(email));
    }
}
