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


    public IExecutor<?> setExecutorRegisterNotificationToken(ReqNotificationRegister reqNotificationRegister);

    public IExecutor<?> setExecutorDeleteNotificationToken(ReqDeviceToken reqDeviceToken);

    public IExecutor<?> setExecutorSubscribeStateNotification(ReqSubscibeToken reqDeviceToken);

    public IExecutor<?> setExecutorGetNotificationBadge(ReqNotificationTarget reqNotificationTarget);

}
