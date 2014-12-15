package com.tosslab.jandi.app.network.spring;

import org.apache.http.client.methods.HttpRequestBase;

import java.net.URI;

/**
 * Created by Steve SeongUg Jung on 14. 12. 15..
 */
public class HttpDeleteRequest extends HttpRequestBase {

    public HttpDeleteRequest(URI uri) {
        super();
        setURI(uri);
    }

    @Override
    public String getMethod() {
        return "DELETE";
    }
}
