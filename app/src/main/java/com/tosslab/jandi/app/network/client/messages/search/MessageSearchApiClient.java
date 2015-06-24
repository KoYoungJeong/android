package com.tosslab.jandi.app.network.client.messages.search;

import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.network.models.ResMessageSearch;
import com.tosslab.jandi.app.network.spring.JandiV2HttpMessageConverter;
import com.tosslab.jandi.app.network.spring.LoggerInterceptor;

import org.androidannotations.annotations.rest.Accept;
import org.androidannotations.annotations.rest.Get;
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

@Deprecated
@Accept(JandiV2HttpMessageConverter.APPLICATION_VERSION_FULL_NAME)
public interface MessageSearchApiClient {

    void setHeader(String name, String value);

    void setAuthentication(HttpAuthentication auth);

    // Message Search
    @Get("/teams/{teamId}/search/messages?q={query}&page={page}&perPage={perPage}&writerId={writerId}&entityId={entityId}")
    @RequiresAuthentication
    ResMessageSearch searchMessages(int teamId, String query, int page, int perPage, int writerId, int entityId);

    @Get("/teams/{teamId}/search/messages?q={query}&page={page}&perPage={perPage}&entityId={entityId}")
    @RequiresAuthentication
    ResMessageSearch searchMessagesByEntityId(int teamId, String query, int page, int perPage, int entityId);

    @Get("/teams/{teamId}/search/messages?q={query}&page={page}&perPage={perPage}&writerId={writerId}")
    @RequiresAuthentication
    ResMessageSearch searchMessagesByWriterId(int teamId, String query, int page, int perPage, int writerId);

    @Get("/teams/{teamId}/search/messages?q={query}&page={page}&perPage={perPage}")
    @RequiresAuthentication
    ResMessageSearch searchMessages(int teamId, String query, int page, int perPage);

}
