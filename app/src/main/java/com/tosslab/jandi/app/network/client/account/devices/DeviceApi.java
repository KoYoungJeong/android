package com.tosslab.jandi.app.network.client.account.devices;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqAlarmSchedule;
import com.tosslab.jandi.app.network.models.ReqPushToken;
import com.tosslab.jandi.app.network.models.ReqSubscribeToken;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResCommonEmpty;
import com.tosslab.jandi.app.network.models.ResDeviceSubscribe;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public class DeviceApi extends ApiTemplate<DeviceApi.Api> {

    @Inject
    public DeviceApi(InnerApiRetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public ResCommon updatePushToken(String deviceId, ReqPushToken reqPushToken) throws RetrofitException {
        return call(() -> getApi().updatePushToken(deviceId, reqPushToken));
    }

    public ResDeviceSubscribe updateSubscribe(String deviceId, ReqSubscribeToken subscibeToken) throws RetrofitException {
        return call(() -> getApi().updateSubScribe(deviceId, subscibeToken));
    }

    public ResDeviceSubscribe deleteDevice(String deviceId) throws RetrofitException {
        return call(() -> getApi().deleteDevice(deviceId));
    }

    public ResDeviceSubscribe getDeviceInfo(String deviceId) throws RetrofitException {
        return call(() -> getApi().getDeviceInfo(deviceId));
    }

    public ResCommonEmpty setAlarmSchedule(String deviceId, ReqAlarmSchedule reqAlarmSchedule) throws RetrofitException {
        return call(() -> getApi().setAlarmSchedule(deviceId, reqAlarmSchedule));
    }

    interface Api {
        @PUT("devices/{deviceId}/pushToken")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> updatePushToken(@Path("deviceId") String deviceId, @Body ReqPushToken reqPushToken);

        @PUT("devices/{deviceId}/subscribe")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResDeviceSubscribe> updateSubScribe(@Path("deviceId") String deviceId, @Body ReqSubscribeToken subscribe);

        @DELETE("devices/{deviceId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResDeviceSubscribe> deleteDevice(@Path("deviceId") String deviceId);

        @GET("devices/{deviceId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResDeviceSubscribe> getDeviceInfo(@Path("deviceId") String deviceId);

        @PUT("devices/{deviceId}/alarmSchedule")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommonEmpty> setAlarmSchedule(@Path("deviceId") String deviceId
                , @Body ReqAlarmSchedule reqAlarmSchedule);
    }
}
