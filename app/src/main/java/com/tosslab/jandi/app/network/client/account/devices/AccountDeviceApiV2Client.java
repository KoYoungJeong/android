package com.tosslab.jandi.app.network.client.account.devices;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.models.ReqDeviceToken;
import com.tosslab.jandi.app.network.models.ReqNotificationRegister;
import com.tosslab.jandi.app.network.models.ReqNotificationTarget;
import com.tosslab.jandi.app.network.models.ReqSubscibeToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.PUT;

/**
 * Created by tee on 15. 6. 16..
 */
public interface AccountDeviceApiV2Client {

    // Notification Token 등록
    @POST("/account/devices")
    @Headers("Accept :" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResAccountInfo registerNotificationToken(@Body ReqNotificationRegister reqNotificationRegister);

    // Notification Token 삭제
    @DELETE("/account/devices")
    @Headers("Accept :" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResAccountInfo deleteNotificationToken(@Body ReqDeviceToken reqDeviceToken);

    // Notification 켜고 끄기
    @PUT("/account/devices")
    @Headers("Accept :" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResAccountInfo subscribeStateNotification(@Body ReqSubscibeToken reqDeviceToken);

    // ios 뱃지
    @PUT("/account/devices/badge")
    @Headers("Accept :" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon getNotificationBadge(@Body ReqNotificationTarget reqNotificationTarget);

}
