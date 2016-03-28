package com.tosslab.jandi.app.network.client.account.devices;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqDeviceToken;
import com.tosslab.jandi.app.network.models.ReqNotificationRegister;
import com.tosslab.jandi.app.network.models.ReqNotificationTarget;
import com.tosslab.jandi.app.network.models.ReqSubscibeToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HTTP;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public class DeviceApi extends ApiTemplate<DeviceApi.Api> {

    public DeviceApi() {
        super(Api.class);
    }

    public ResAccountInfo registerNotificationToken(ReqNotificationRegister reqNotificationRegister) throws RetrofitException {
        return call(() -> getApi().registerNotificationToken(reqNotificationRegister));
    }

    ResAccountInfo deleteNotificationToken(ReqDeviceToken reqDeviceToken) throws RetrofitException {
        return call(() -> getApi().deleteNotificationToken(reqDeviceToken));
    }

    ResAccountInfo subscribeStateNotification(ReqSubscibeToken reqDeviceToken) throws RetrofitException {
        return call(() -> getApi().subscribeStateNotification(reqDeviceToken));
    }

    @Deprecated
    ResCommon getNotificationBadge(ReqNotificationTarget reqNotificationTarget) throws RetrofitException {
        return call(() -> getApi().getNotificationBadge(reqNotificationTarget));
    }

    interface Api {

        // Notification Token 등록
        @POST("/account/devices")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResAccountInfo> registerNotificationToken(@Body ReqNotificationRegister reqNotificationRegister);

        // Notification Token 삭제
        @HTTP(path = "/account/devices", method = "DELETE", hasBody = true)
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResAccountInfo> deleteNotificationToken(@Body ReqDeviceToken reqDeviceToken);

        // Notification 켜고 끄기
        @PUT("/account/devices")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResAccountInfo> subscribeStateNotification(@Body ReqSubscibeToken reqDeviceToken);

        // ios 뱃지
        @PUT("/account/devices/badge")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        @Deprecated
        Call<ResCommon> getNotificationBadge(@Body ReqNotificationTarget reqNotificationTarget);

    }
}
