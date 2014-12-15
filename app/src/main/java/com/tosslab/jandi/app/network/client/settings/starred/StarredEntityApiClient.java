package com.tosslab.jandi.app.network.client.settings.starred;

/**
 * Created by Steve SeongUg Jung on 14. 12. 15..
 */

import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.network.spring.JandiV2HttpMessageConverter;
import com.tosslab.jandi.app.network.spring.LoggerInterceptor;
import com.tosslab.jandi.app.network.models.ReqTeam;
import com.tosslab.jandi.app.network.models.ResCommon;

import org.androidannotations.annotations.rest.Accept;
import org.androidannotations.annotations.rest.Delete;
import org.androidannotations.annotations.rest.Post;
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
public interface StarredEntityApiClient {

    void setHeader(String name, String value);

    void setAuthentication(HttpAuthentication auth);

    @Post("/settings/starred/entities/{entityId}")
    @RequiresAuthentication
    ResCommon enableFavorite(ReqTeam reqTeam, int entityId);

    @Delete("/settings/starred/entities/{entityId}?teamId={teamId}")
    @RequiresAuthentication
    ResCommon disableFavorite(int teamId, int entityId);

}
