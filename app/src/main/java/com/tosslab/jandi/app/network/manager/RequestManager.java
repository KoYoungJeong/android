package com.tosslab.jandi.app.network.manager;

import android.content.Context;

import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * Created by Steve SeongUg Jung on 14. 12. 16..
 */
public class RequestManager<ResponseObject> {

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
                        LogUtil.e("Retry Fail" + request.getClass() + " : " + e1.getStatusCode().value() + " : " + e1.getResponseBodyAsString(), e1);
                        throw new JandiNetworkException(e1);
                    }
                } else {
                    // unauthorized exception
                    LogUtil.e("Refresh Token Fail : " + request.getClass() + " : " + e.getStatusCode().value() + " : " + e.getResponseBodyAsString(), e);
                    throw new JandiNetworkException(e);
                }
            } else {
                // exception, not unauthorized
                JandiSocketService.stopService(context);
                LogUtil.e("Request Fail : " + request.getClass() + " : " + e.getStatusCode().value() + " : " + e.getResponseBodyAsString(), e);
                throw new JandiNetworkException(e);
            }
        } catch (Exception e) {
            LogUtil.e("Unknown Request Error : " + request.getClass() + " : " + e.getMessage(), e);
            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
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
                LogUtil.e("Refresh Token Fail : " + e.getStatusCode().value() + " : " + e.getResponseBodyAsString());
                if (e.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                    return null;
                }
            }
        }

        return accessToken;
    }
}
