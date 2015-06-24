//package com.tosslab.jandi.app.network.client.messages;
//
//import com.tosslab.jandi.app.JandiConstantsForFlavors;
//import com.tosslab.jandi.app.network.models.ReqShareMessage;
//import com.tosslab.jandi.app.network.models.ReqUnshareMessage;
//import com.tosslab.jandi.app.network.models.ResCommon;
//import com.tosslab.jandi.app.network.models.ResFileDetail;
//import com.tosslab.jandi.app.network.spring.JandiV2HttpMessageConverter;
//import com.tosslab.jandi.app.network.spring.LoggerInterceptor;
//
//import org.androidannotations.annotations.rest.Accept;
//import org.androidannotations.annotations.rest.Get;
//import org.androidannotations.annotations.rest.Put;
//import org.androidannotations.annotations.rest.RequiresAuthentication;
//import org.androidannotations.annotations.rest.Rest;
//import org.springframework.http.HttpAuthentication;
//import org.springframework.http.converter.ByteArrayHttpMessageConverter;
//import org.springframework.http.converter.FormHttpMessageConverter;
//import org.springframework.http.converter.StringHttpMessageConverter;
//
///**
// * Created by Steve SeongUg Jung on 14. 12. 15..
// */
//@Rest(
//        rootUrl = JandiConstantsForFlavors.SERVICE_ROOT_URL + "inner-api",
//        converters = {
//                JandiV2HttpMessageConverter.class,
//                ByteArrayHttpMessageConverter.class,
//                FormHttpMessageConverter.class,
//                StringHttpMessageConverter.class},
//        interceptors = {LoggerInterceptor.class}
//)
//
//@Deprecated
//@Accept(JandiV2HttpMessageConverter.APPLICATION_VERSION_FULL_NAME)
//public interface MessagesApiClient {
//
//    void setHeader(String name, String value);
//
//    void setAuthentication(HttpAuthentication auth);
//
//    // Message Detail
//    @Get("/messages/{messageId}?teamId={teamId}")
//    @RequiresAuthentication
//    ResFileDetail getFileDetail(int teamId, int messageId);
//
//    // Share Message
//    @Put("/messages/{messageId}/share")
//    @RequiresAuthentication
//    ResCommon shareMessage(ReqShareMessage share, int messageId);
//
//    // Unshare Message
//    @Put("/messages/{messageId}/unshare")
//    @RequiresAuthentication
//    ResCommon unshareMessage(ReqUnshareMessage share, int messageId);
//
//}
