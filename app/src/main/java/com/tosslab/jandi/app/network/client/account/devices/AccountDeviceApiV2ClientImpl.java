package com.tosslab.jandi.app.network.client.account.devices;

import com.tosslab.jandi.app.network.manager.RestApiClient.RestAdapterFactory.builder.RestAdapterBuilder;
import com.tosslab.jandi.app.network.models.ReqDeviceToken;
import com.tosslab.jandi.app.network.models.ReqNotificationRegister;
import com.tosslab.jandi.app.network.models.ReqNotificationTarget;
import com.tosslab.jandi.app.network.models.ReqSubscibeToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;

/**
 * Created by Steve SeongUg Jung on 15. 6. 27..
 */
public class AccountDeviceApiV2ClientImpl implements AccountDeviceApiV2Client {
    @Override
    public ResAccountInfo registerNotificationToken(ReqNotificationRegister reqNotificationRegister) {

        AccountDeviceApiV2Client accountDeviceApiV2Client = getAccountDeviceApiV2Client();

        return accountDeviceApiV2Client.registerNotificationToken(reqNotificationRegister);
    }

    @Override
    public ResAccountInfo deleteNotificationToken(ReqDeviceToken reqDeviceToken) {
        AccountDeviceApiV2Client client = getAccountDeviceApiV2Client();

        return client.deleteNotificationToken(reqDeviceToken);
    }

    @Override
    public ResAccountInfo subscribeStateNotification(ReqSubscibeToken reqDeviceToken) {
        AccountDeviceApiV2Client client = getAccountDeviceApiV2Client();

        return client.subscribeStateNotification(reqDeviceToken);
    }

    @Override
    public ResCommon getNotificationBadge(ReqNotificationTarget reqNotificationTarget) {
        AccountDeviceApiV2Client client = getAccountDeviceApiV2Client();

        return client.getNotificationBadge(reqNotificationTarget);
    }

    private AccountDeviceApiV2Client getAccountDeviceApiV2Client() {
        return RestAdapterBuilder.newInstance(AccountDeviceApiV2Client.class).create();
    }
}
