package com.tosslab.jandi.app.network.spring;

import org.apache.http.client.methods.HttpUriRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.net.URI;

/**
 * Created by Steve SeongUg Jung on 14. 12. 15..
 */
public class HttpRequestFactory extends HttpComponentsClientHttpRequestFactory {

    @Override
    protected HttpUriRequest createHttpRequest(HttpMethod httpMethod, URI uri) {

        if (HttpMethod.DELETE == httpMethod) {

            return new HttpDeleteRequest(uri);
        }

        return super.createHttpRequest(httpMethod, uri);
    }
}
