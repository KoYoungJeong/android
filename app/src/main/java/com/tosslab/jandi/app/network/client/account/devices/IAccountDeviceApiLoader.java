package com.tosslab.jandi.app.network.client.account.devices;

import com.tosslab.jandi.app.network.manager.ApiExecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ReqDeviceToken;
import com.tosslab.jandi.app.network.models.ReqNotificationRegister;
import com.tosslab.jandi.app.network.models.ReqNotificationTarget;
import com.tosslab.jandi.app.network.models.ReqSubscibeToken;

/**
 * Created by tee on 15. 6. 20..
 */
public interface IAccountDeviceApiLoader {

    public IExecutor loadRegisterNotificationTokenByAccountDeviceApi(ReqNotificationRegister reqNotificationRegister);

    public IExecutor loadDeleteNotificationTokenByAccountDeviceApi(ReqDeviceToken reqDeviceToken);

    public IExecutor loadSubscribeStateNotificationByAccountDeviceApi(ReqSubscibeToken reqDeviceToken);

    public IExecutor loadGetNotificationBadgeByAccountDeviceApi(ReqNotificationTarget reqNotificationTarget);

}
