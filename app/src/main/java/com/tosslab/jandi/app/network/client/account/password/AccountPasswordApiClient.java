package com.tosslab.jandi.app.network.client.account.password;

import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqChangePassword;
import com.tosslab.jandi.app.network.models.ReqConfirmEmail;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.spring.HttpRequestFactory;
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
        requestFactory = HttpRequestFactory.class,
        interceptors = {LoggerInterceptor.class}
)

@Accept(JandiV2HttpMessageConverter.APPLICATION_VERSION_FULL_NAME)
public interface AccountPasswordApiClient {

    void setHeader(String name, String value);

    void setAuthentication(HttpAuthentication auth);

    /**
     * reset password
     *
     * @param reqAccountEmail
     * @return
     */
    @Post("/accounts/password/resetToken")
    ResCommon resetPassword(ReqAccountEmail reqAccountEmail);

    /**
     * change password
     *
     * @param reqConfirmEmail
     * @return
     */
    @Put("/accounts/password")
    @RequiresAuthentication
    ResCommon changePassword(ReqChangePassword reqConfirmEmail);

}
