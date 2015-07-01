package com.tosslab.jandi.app.network.client.account.devices;

import com.tosslab.jandi.app.network.manager.ApiExecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ReqDeviceToken;
import com.tosslab.jandi.app.network.models.ReqNotificationRegister;
import com.tosslab.jandi.app.network.models.ReqNotificationTarget;
import com.tosslab.jandi.app.network.models.ReqSubscibeToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;

/**
 * Created by tee on 15. 6. 20..
 */
public interface IAccountDeviceApiLoader {

    IExecutor<ResAccountInfo> loadRegisterNotificationTokenByAccountDeviceApi(ReqNotificationRegister reqNotificationRegister);

    IExecutor<ResAccountInfo> loadDeleteNotificationTokenByAccountDeviceApi(ReqDeviceToken reqDeviceToken);

    IExecutor<ResAccountInfo> loadSubscribeStateNotificationByAccountDeviceApi(ReqSubscibeToken reqDeviceToken);

    IExecutor<ResCommon> loadGetNotificationBadgeByAccountDeviceApi(ReqNotificationTarget reqNotificationTarget);

}
