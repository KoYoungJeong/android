package com.tosslab.jandi.app.network.manager;

import android.content.Context;

import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.apache.log4j.Logger;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * Created by Steve SeongUg Jung on 14. 12. 16..
 */
public class RequestManager<ResponseObject> {
    private final static Logger logger = Logger.getLogger(RequestManager.class);

    private final Context context;
    private Request<ResponseObject> request;

    private RequestManager(Context context, Request<ResponseObject> request) {
        this.context = context;
        this.request = request;
    }

    public static <ResponseObject> RequestManager<ResponseObject> newInstance(Context context, Request<ResponseObject> request) {
        return new RequestManager<ResponseObject>(context, request);
    }

    public ResponseObject request() throws JandiNetworkException {
        try {
            return request.request();
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().value() == 401) {

                ResAccessToken accessToken = refreshToken();
                if (accessToken != null) {
                    try {
                        return request.request();
                    } catch (HttpStatusCodeException e1) {
                        // unknown exception
                        logger.debug("Retry Fail" + request.getClass() + " : " + e1.getStatusCode().value() + " : " + e1.getResponseBodyAsString());
                        throw new JandiNetworkException(e1);
                    }
                } else {
                    // unauthorized exception
                    logger.debug("Refresh Token Fail : " + request.getClass() + " : " + e.getStatusCode().value() + " : " + e.getResponseBodyAsString());
                    throw new JandiNetworkException(e);
                }
            } else {
                // exception, not unauthorized
                logger.debug("Request Fail : " + request.getClass() + " : " + e.getStatusCode().value() + " : " + e.getResponseBodyAsString());
                throw new JandiNetworkException(e);
            }
        }

    }

    private ResAccessToken refreshToken() {
        ResAccessToken accessToken = null;
        int loginRetryCount = 0;

        while (accessToken == null && loginRetryCount <= 3) {
            ++loginRetryCount;
            try {
                // Request Access token, and save token
                accessToken = new TokenRefreshRequest(context, JandiPreference.getRefreshToken(context)).request();
            } catch (HttpStatusCodeException e) {
                logger.error("Refresh Token Fail : " + e.getStatusCode().value() + " : " + e.getResponseBodyAsString());
            }
        }

        return accessToken;
    }
}
