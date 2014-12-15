package com.tosslab.jandi.app.network.client.profile;

import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.network.spring.JandiV2HttpMessageConverter;
import com.tosslab.jandi.app.network.spring.LoggerInterceptor;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.androidannotations.annotations.rest.Accept;
import org.androidannotations.annotations.rest.Post;
import org.androidannotations.annotations.rest.Put;
import org.androidannotations.annotations.rest.RequiresAuthentication;
import org.androidannotations.annotations.rest.Rest;
import org.springframework.http.HttpAuthentication;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;

/**
 * Created by Steve SeongUg Jung on 14. 12. 15..
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
public interface ProfileApiClient {

    void setHeader(String name, String value);

    void setAuthentication(HttpAuthentication auth);


    @Put("/members/{memberId}/profile")
    @RequiresAuthentication
    ResLeftSideMenu.User updateUserProfile(int memberId, ReqUpdateProfile reqUpdateProfile);

    @Post("/members/profile")
    @RequiresAuthentication
    @Deprecated
    ResLeftSideMenu.User updateUserProfile(ReqUpdateProfile reqUpdateProfile);

    @Put("/members/{memberId}/name")
    @RequiresAuthentication
    ResCommon updateUserName(int memberId, ReqProfileName reqProfileName);

    @Post("/members/name")
    @RequiresAuthentication
    @Deprecated
    ResLeftSideMenu.User updateUserName(ReqUpdateProfile reqUpdateProfile);

}
