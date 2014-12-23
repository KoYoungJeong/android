package com.tosslab.jandi.app.network.client.settings;

/**
 * Created by Steve SeongUg Jung on 14. 12. 15..
 */

import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.spring.JandiV2HttpMessageConverter;
import com.tosslab.jandi.app.network.spring.LoggerInterceptor;

import org.androidannotations.annotations.rest.Accept;
import org.androidannotations.annotations.rest.Post;
import org.androidannotations.annotations.rest.Put;
import org.androidannotations.annotations.rest.RequiresAuthentication;
import org.androidannotations.annotations.rest.Rest;
import org.springframework.http.HttpAuthentication;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;

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
public interface AccountProfileClient {

    void setHeader(String name, String value);

    void setAuthentication(HttpAuthentication auth);

    @Post("/settings/name")
    @RequiresAuthentication
    ResAccountInfo changeName(ReqProfileName reqProfileName);

    @Put("/settings/primaryEmail")
    @RequiresAuthentication
    ResAccountInfo changePrimaryEmail(ReqAccountEmail reqAccountEmail);

}
