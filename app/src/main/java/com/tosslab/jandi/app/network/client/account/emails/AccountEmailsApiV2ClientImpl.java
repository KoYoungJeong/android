package com.tosslab.jandi.app.network.client.account.emails;

import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RestAdapterBuilder;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqConfirmEmail;
import com.tosslab.jandi.app.network.models.ResAccountInfo;

/**
 * Created by Steve SeongUg Jung on 15. 6. 30..
 */
public class AccountEmailsApiV2ClientImpl implements AccountEmailsApiV2Client {

    private AccountEmailsApiV2ClientImpl() {
    }

    public static AccountEmailsApiV2ClientImpl create() {
        return new AccountEmailsApiV2ClientImpl();
    }


    @Override
    public ResAccountInfo requestAddEmail(ReqAccountEmail reqAccountEmail) {
        return RestAdapterBuilder.newInstance(AccountEmailsApiV2Client.class).create()
                .requestAddEmail(reqAccountEmail);
    }

    @Override
    public ResAccountInfo confirmEmail(ReqConfirmEmail reqConfirmEmail) {
        return RestAdapterBuilder.newInstance(AccountEmailsApiV2Client.class).create()
                .confirmEmail(reqConfirmEmail);
    }

    @Override
    public ResAccountInfo deleteEmail(ReqAccountEmail reqConfirmEmail) {
        return RestAdapterBuilder.newInstance(AccountEmailsApiV2Client.class).create()
                .deleteEmail(reqConfirmEmail);
    }
}
