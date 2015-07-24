package com.tosslab.jandi.lib.sprinkler.io;

import android.util.Log;

import com.tosslab.jandi.lib.sprinkler.Logger;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;

import retrofit.RestAdapter;
import retrofit.RetrofitError;

/**
 * Created by tonyjs on 15. 7. 21..
 */
final class RequestManager {
    public static final String TAG = Logger.makeTag(RequestManager.class);

    public interface Request<RESPONSE> {
        RESPONSE performRequest() throws RetrofitError;
    }

    private static final int RETRY_COUNT = 3;

    private boolean isCanceled = false;
    private RestAdapter restAdapter;

    public RequestManager() {
        RequestConfig config = getRequestConfig();
        restAdapter = new RestAdapter.Builder()
                .setEndpoint(config.getEndPoint())
                .setLogLevel(config.getLogLevel())
                .build();
    }

    public <CLIENT> CLIENT getClient(Class<CLIENT> client) {
        return restAdapter.create(client);
    }

    public <RESPONSE> RESPONSE request(Request<RESPONSE> request) throws RetrofitError {
        RESPONSE response = null;
        int i = 0;
        while (i <= RETRY_COUNT) {
            try {
                response = request.performRequest();
                Logger.i(TAG, "Request success.");
                break;
            } catch (RetrofitError retrofitError) {
                if (isCanceled) {
                    Logger.i(TAG, "Request has cancelled.");
                    throw retrofitError;
                }
                Logger.print(retrofitError);
                if (i >= RETRY_COUNT) {
                    Log.i(TAG, "Request fail - retry has exceeded.");
                    throw retrofitError;
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
        if (Sprinkler.IS_DEBUG_MODE) {
            return new RequestConfigDev();
        }
//        return new RequestConfigRelease();
        return new RequestConfigDev();
    }

    static class RequestConfigRelease implements RequestConfig {

        @Override
        public String getEndPoint() {
            return "http://112.219.215.148:50080/log";
        }

        @Override
        public RestAdapter.LogLevel getLogLevel() {
            return RestAdapter.LogLevel.NONE;
        }
    }

    static class RequestConfigDev implements RequestConfig {

        @Override
        public String getEndPoint() {
            return "http://112.219.215.148:50080/log";
        }

        @Override
        public RestAdapter.LogLevel getLogLevel() {
            return RestAdapter.LogLevel.BASIC;
        }
    }

    interface RequestConfig {
        String getEndPoint();

        RestAdapter.LogLevel getLogLevel();
    }

}
