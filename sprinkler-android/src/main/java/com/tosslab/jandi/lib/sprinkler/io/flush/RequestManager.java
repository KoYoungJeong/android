package com.tosslab.jandi.lib.sprinkler.io.flush;

import android.util.Log;

import com.tosslab.jandi.lib.sprinkler.util.Logger;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;

import java.util.Collection;
import java.util.Collections;

import okhttp3.Interceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by tonyjs on 15. 7. 21..
 */
final class RequestManager {
    public static final String TAG = Logger.makeTag(RequestManager.class);
    private static final int RETRY_COUNT = 3;
    private static RequestManager sInstance;
    private boolean isCanceled = false;
    private Retrofit retrofit;
    private RequestManager() {
        RequestConfig config = getRequestConfig();
        retrofit = new Retrofit.Builder()
                .client(OkConnectionClient.getDefaultClient())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(config.getEndPoint())
                .build();
    }

    public static RequestManager get() {
        if (sInstance == null) {
            sInstance = new RequestManager();
        }
        return sInstance;
    }

    public <CLIENT> CLIENT getClient(Class<CLIENT> client) {
        return retrofit.create(client);
    }

    public <RESPONSE> RESPONSE request(Request<RESPONSE> request) throws Exception {
        RESPONSE response = null;
        try {
            response = request.performRequest();
            Logger.i(TAG, "Request success.");
        } catch (Exception error) {
            Logger.i(TAG, "Request fail");
            throw error;
        }
        return response;
    }

    public <RESPONSE> RESPONSE requestWithRetry(Request<RESPONSE> request) throws Exception {
        RESPONSE response = null;
        int i = 0;
        while (i <= RETRY_COUNT) {
            try {
                response = request.performRequest();
                Logger.i(TAG, "Request success.");
                break;
            } catch (Exception error) {
                if (isCanceled) {
                    Logger.i(TAG, "Request has cancelled.");
                    throw error;
                }
                Logger.print(error);
                if (i >= RETRY_COUNT) {
                    Log.i(TAG, "Request fail - retry has exceeded.");
                    throw error;
                }

                i++;
                Logger.i(TAG, "Request retry - " + i);
            }
        }
        return response;
    }

    public void stop() {
        isCanceled = true;
    }

    private RequestConfig getRequestConfig() {
        if (Sprinkler.IS_FOR_DEV) {
            return new RequestConfigDev();
        }
        return new RequestConfigRelease();
    }

    public interface Request<RESPONSE> {
        RESPONSE performRequest() throws Exception;
    }

    interface RequestConfig {

        String getEndPoint();
    }

    static class RequestConfigRelease implements RequestConfig {

        @Override
        public String getEndPoint() {
            return "https://track.jandi.com/";
        }
    }

    static class RequestConfigDev implements RequestConfig {

        @Override
        public String getEndPoint() {
            return "https://dev-tracker.sprinklr.io:50079/";
        }
    }

}
