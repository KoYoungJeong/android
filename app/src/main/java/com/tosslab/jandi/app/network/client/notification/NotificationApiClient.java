package com.tosslab.jandi.app.network.client.notification;

import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.network.JandiV2HttpMessageConverter;
import com.tosslab.jandi.app.network.LoggerInterceptor;
import com.tosslab.jandi.app.network.models.ReqNotificationRegister;
import com.tosslab.jandi.app.network.models.ReqNotificationSubscribe;
import com.tosslab.jandi.app.network.models.ReqNotificationTarget;
import com.tosslab.jandi.app.network.models.ResCommon;

import org.androidannotations.annotations.rest.Accept;
import org.androidannotations.annotations.rest.Delete;
import org.androidannotations.annotations.rest.Put;
import org.androidannotations.annotations.rest.RequiresAuthentication;
import org.androidannotations.annotations.rest.Rest;
import org.springframework.http.HttpAuthentication;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;

/**
 * Created by Steve SeongUg Jung on 14. 12. 12..
 */
@Rest(
        rootUrl = JandiConstantsForFlavors.SERVICE_ROOT_URL + "inner-api",
        converters = {
                JandiV2HttpMessageConverter.class,
                ByteArrayHttpMessageConverter.class,
                FormHttpMessageConverter.class,
                StringHttpMessageConverter.class},
        interceptors = {LoggerInterceptor.class}
)

@Accept(JandiV2HttpMessageConverter.APPLICATION_VERSION_FULL_NAME)
public interface NotificationApiClient {

    void setHeader(String name, String value);

    void setAuthentication(HttpAuthentication auth);


    // Notification Token 등록
    @Put("/settings/notifications")
    @RequiresAuthentication
    ResCommon registerNotificationToken(ReqNotificationRegister reqNotificationRegister);

    // Notification Token 삭제
    @Delete("/settings/notifications/{deviceToken}")
    @RequiresAuthentication
    ResCommon deleteNotificationToken(String deviceToken);

    // Notification 켜고 끄기
    @Put("/settings/notifications/{deviceToken}/subscribe")
    @RequiresAuthentication
    ResCommon subscribeNotification(String deviceToken, ReqNotificationSubscribe reqNotificationSubscribe);

    // Notification Target 설정
    @Put("/settings/notification/target")
    @RequiresAuthentication
    ResCommon setNotificationTarget(ReqNotificationTarget reqNotificationTarget);
}
