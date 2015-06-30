package com.tosslab.jandi.app.network.client.account.password;

import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RestAdapterBuilder;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqChangePassword;
import com.tosslab.jandi.app.network.models.ResCommon;

/**
 * Created by Steve SeongUg Jung on 15. 6. 30..
 */
public class AccountPasswordApiV2ClientImpl implements AccountPasswordApiV2Client {
    private AccountPasswordApiV2ClientImpl() {
    }

    public static AccountPasswordApiV2ClientImpl create() {
        return new AccountPasswordApiV2ClientImpl();
    }

    @Override
    public ResCommon resetPassword(ReqAccountEmail reqAccountEmail) {
        return RestAdapterBuilder
                .newInstance(AccountPasswordApiV2Client.class)
                .create()
                .resetPassword(reqAccountEmail);
    }

    @Override
    public ResCommon changePassword(ReqChangePassword reqConfirmEmail) {
        return RestAdapterBuilder.newInstance(AccountPasswordApiV2Client.class)
                .create()
                .changePassword(reqConfirmEmail);

    }
}
