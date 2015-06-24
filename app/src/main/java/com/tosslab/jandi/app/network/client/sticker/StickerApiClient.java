//package com.tosslab.jandi.app.network.client.sticker;
//
//import com.tosslab.jandi.app.JandiConstantsForFlavors;
//import com.tosslab.jandi.app.network.models.ResCommon;
//import com.tosslab.jandi.app.network.models.sticker.ReqSendSticker;
//import com.tosslab.jandi.app.network.spring.JandiV2HttpMessageConverter;
//import com.tosslab.jandi.app.network.spring.LoggerInterceptor;
//
//import org.androidannotations.annotations.rest.Accept;
//import org.androidannotations.annotations.rest.Post;
//import org.androidannotations.annotations.rest.RequiresAuthentication;
//import org.androidannotations.annotations.rest.Rest;
//import org.springframework.http.HttpAuthentication;
//import org.springframework.http.converter.ByteArrayHttpMessageConverter;
//import org.springframework.http.converter.FormHttpMessageConverter;
//import org.springframework.http.converter.StringHttpMessageConverter;
//
///**
// * Created by Steve SeongUg Jung on 15. 6. 8..
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
//public interface StickerApiClient {
//    void setHeader(String name, String value);
//
//    void setAuthentication(HttpAuthentication auth);
//
//    @Post("/stickers")
//    @RequiresAuthentication
//    ResCommon sendSticker(ReqSendSticker reqSendSticker);
//
//    @Post("/stickers/comment")
//    @RequiresAuthentication
//    ResCommon sendStickerComment(ReqSendSticker reqSendSticker);
//
//}
