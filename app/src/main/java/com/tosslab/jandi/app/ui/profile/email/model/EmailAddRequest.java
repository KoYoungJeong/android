package com.tosslab.jandi.app.ui.profile.email.model;

import android.content.Context;

import com.tosslab.jandi.app.network.client.account.emails.AccountEmailsApiClient;
import com.tosslab.jandi.app.network.client.account.emails.AccountEmailsApiClient_;
import com.tosslab.jandi.app.network.manager.Request;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.LanguageUtil;
import com.tosslab.jandi.app.utils.TokenUtil;

/**
 * Created by Steve SeongUg Jung on 15. 1. 13..
 */
public class EmailAddRequest implements Request<ResAccountInfo> {

    private final Context context;
    private final String email;
    private AccountEmailsApiClient accountEmailsApiClient;

    private EmailAddRequest(Context context, String email, AccountEmailsApiClient accountEmailsApiClient) {
        this.context = context;
        this.email = email;
        this.accountEmailsApiClient = accountEmailsApiClient;
    }

    public static EmailAddRequest create(Context context, String email) {
        return new EmailAddRequest(context, email, new AccountEmailsApiClient_(context));
    }

    @Override
    public ResAccountInfo request() throws JandiNetworkException {
        accountEmailsApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));


        ReqAccountEmail reqAccountEmail = new ReqAccountEmail(email, LanguageUtil.getLanguage(context));
        return accountEmailsApiClient.requestAddEmail(reqAccountEmail);
    }
}