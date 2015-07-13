package com.tosslab.jandi.app.network.client.account.devices;

import com.tosslab.jandi.app.network.models.ReqDeviceToken;
import com.tosslab.jandi.app.network.models.ReqNotificationRegister;
import com.tosslab.jandi.app.network.models.ReqNotificationTarget;
import com.tosslab.jandi.app.network.models.ReqSubscibeToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;

import retrofit.RetrofitError;

public interface IAccountDeviceApiAuth {

    ResAccountInfo registerNotificationTokenByAccountDeviceApi(ReqNotificationRegister reqNotificationRegister) throws RetrofitError;

    ResAccountInfo deleteNotificationTokenByAccountDeviceApi(ReqDeviceToken reqDeviceToken) throws RetrofitError;

    ResAccountInfo subscribeStateNotificationByAccountDeviceApi(ReqSubscibeToken reqDeviceToken) throws RetrofitError;

    ResCommon getNotificationBadgeByAccountDeviceApi(ReqNotificationTarget reqNotificationTarget) throws RetrofitError;

}
