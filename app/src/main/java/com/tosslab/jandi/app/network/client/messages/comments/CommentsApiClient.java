//package com.tosslab.jandi.app.network.client.messages.comments;
//
//import com.tosslab.jandi.app.JandiConstantsForFlavors;
//import com.tosslab.jandi.app.network.models.ReqSendComment;
//import com.tosslab.jandi.app.network.models.ResCommon;
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
//public interface CommentsApiClient {
//
//    void setHeader(String name, String value);
//
//    void setAuthentication(HttpAuthentication auth);
//
//    // Send Comment
//    @Post("/messages/{messageId}/comment")
//    @RequiresAuthentication
//    ResCommon sendMessageComment(ReqSendComment comment, int messageId);
//
//    // Modify comment
//    @Put("/messages/{messageId}/comments/{commentId}")
//    @RequiresAuthentication
//    ResCommon modifyMessageComment(ReqSendComment comment, int messageId, int commentId);
//
//    // Delete comment
//    @Delete("/messages/{messageId}/comments/{commentId}?teamId={teamId}")
//    @RequiresAuthentication
//    ResCommon deleteMessageComment(int teamId, int messageId, int commentId);
//
//
//}
