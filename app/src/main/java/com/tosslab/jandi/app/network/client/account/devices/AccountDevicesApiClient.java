//package com.tosslab.jandi.app.network.client.account.devices;
//
//import com.tosslab.jandi.app.JandiConstantsForFlavors;
//import com.tosslab.jandi.app.network.models.ReqDeviceToken;
//import com.tosslab.jandi.app.network.models.ReqNotificationRegister;
//import com.tosslab.jandi.app.network.models.ReqNotificationTarget;
//import com.tosslab.jandi.app.network.models.ReqSubscibeToken;
//import com.tosslab.jandi.app.network.models.ResAccountInfo;
//import com.tosslab.jandi.app.network.models.ResCommon;
//import com.tosslab.jandi.app.network.spring.HttpRequestFactory;
//import com.tosslab.jandi.app.network.spring.JandiV2HttpMessageConverter;
//import com.tosslab.jandi.app.network.spring.LoggerInterceptor;
//
//import org.androidannotations.annotations.rest.Accept;
//import org.androidannotations.annotations.rest.Delete;
//import org.androidannotations.annotations.rest.Post;
//import org.androidannotations.annotations.rest.Put;
//import org.androidannotations.annotations.rest.RequiresAuthentication;
//import org.androidannotations.annotations.rest.Rest;
//import org.springframework.http.HttpAuthentication;
//import org.springframework.http.converter.ByteArrayHttpMessageConverter;
//import org.springframework.http.converter.FormHttpMessageConverter;
//import org.springframework.http.converter.StringHttpMessageConverter;
//
///**
// * Created by Steve SeongUg Jung on 14. 12. 12..
// */
//@Rest(
//        rootUrl = JandiConstantsForFlavors.SERVICE_ROOT_URL + "inner-api",
//        converters = {
//                JandiV2HttpMessageConverter.class,
//                ByteArrayHttpMessageConverter.class,
//                FormHttpMessageConverter.class,
//                StringHttpMessageConverter.class},
//        requestFactory = HttpRequestFactory.class,
//        interceptors = {LoggerInterceptor.class}
//)
//
//@Deprecated
//@Accept(JandiV2HttpMessageConverter.APPLICATION_VERSION_FULL_NAME)
//public interface AccountDevicesApiClient {
//
//    void setHeader(String name, String value);
//
//
//    void setAuthentication(HttpAuthentication auth);
//
//    // Notification Token 등록
//    @Post("/account/devices")
//    @RequiresAuthentication
//    ResAccountInfo registerNotificationToken(ReqNotificationRegister reqNotificationRegister);
//
//    // Notification Token 삭제
//    @Delete("/account/devices")
//    @RequiresAuthentication
//    ResAccountInfo deleteNotificationToken(ReqDeviceToken reqDeviceToken);
//
//    // Notification 켜고 끄기
//    @Put("/account/devices")
//    @RequiresAuthentication
//    @Deprecated
//    ResCommon subscribeStateNotification(/*String deviceToken, ReqNotificationSubscribe reqNotificationSubscribe*/);
//
//    // Notification 켜고 끄기
//    @Put("/account/devices")
//    @RequiresAuthentication
//    ResAccountInfo subscribeStateNotification(ReqSubscibeToken reqDeviceToken);
//
//    // ios 뱃지
//    @Put("/account/devices/badge")
//    @RequiresAuthentication
//    ResCommon getNotificationBadge(ReqNotificationTarget reqNotificationTarget);
//}
