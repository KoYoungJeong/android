package com.tosslab.jandi.lib.sprinkler.io;

import java.io.IOException;
import java.net.HttpURLConnection;

import retrofit.client.Request;
import retrofit.client.UrlConnectionClient;

/**
 * Created by tonyjs on 15. 7. 28..
 */
final class RequestUrlConnectionClient extends UrlConnectionClient {

    private static final int CONNECTION_TIMEOUT = 7 * 1000;
    private static final int READ_TIMEOUT = 7 * 1000;

    @Override
    protected HttpURLConnection openConnection(Request request) throws IOException {
        HttpURLConnection connection = super.openConnection(request);
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        return super.openConnection(request);
    }
}
