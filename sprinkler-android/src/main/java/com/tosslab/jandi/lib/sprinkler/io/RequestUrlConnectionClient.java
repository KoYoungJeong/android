package com.tosslab.jandi.lib.sprinkler.io;

import com.tosslab.jandi.lib.sprinkler.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit.client.Request;
import retrofit.client.UrlConnectionClient;

/**
 * Created by tonyjs on 15. 7. 28..
 */
@Deprecated
final class RequestUrlConnectionClient extends UrlConnectionClient {

    private static final int CONNECTION_TIMEOUT = 7 * 1000;
    private static final int READ_TIMEOUT = 7 * 1000;

    @Override
    protected HttpURLConnection openConnection(Request request) throws IOException {
        HttpURLConnection connection = super.openConnection(request);
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.addRequestProperty("Connection", "close");
        return connection;
    }
}
