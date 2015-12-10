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

    private static RequestManager sInstance;
    private boolean isCanceled = false;
    private RestAdapter restAdapter;

    public static RequestManager get() {
        if (sInstance == null) {
            sInstance = new RequestManager();
        }
        return sInstance;
    }

    private RequestManager() {
        RequestConfig config = getRequestConfig();
        restAdapter = new RestAdapter.Builder()
                .setClient(new OkConnectionClient())
                .setEndpoint(config.getEndPoint())
                .setLogLevel(config.getLogLevel())
                .build();
    }

    public <CLIENT> CLIENT getClient(Class<CLIENT> client) {
        return restAdapter.create(client);
    }

    public <RESPONSE> RESPONSE request(Request<RESPONSE> request) throws RetrofitError {
        RESPONSE response = null;
        try {
            response = request.performRequest();
            Logger.i(TAG, "Request success.");
        } catch (RetrofitError error) {
            Logger.i(TAG, "Request fail");
            throw error;
        }
        return response;
    }

    public <RESPONSE> RESPONSE requestWithRetry(Request<RESPONSE> request) throws RetrofitError {
        RESPONSE response = null;
        int i = 0;
        while (i <= RETRY_COUNT) {
            try {
                response = request.performRequest();
                Logger.i(TAG, "Request success.");
                break;
            } catch (RetrofitError error) {
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

    static class RequestConfigRelease implements RequestConfig {

        @Override
        public String getEndPoint() {
            return "https://track.jandi.com";
        }

        @Override
        public RestAdapter.LogLevel getLogLevel() {
            return RestAdapter.LogLevel.NONE;
        }
    }

    static class RequestConfigDev implements RequestConfig {

        @Override
        public String getEndPoint() {
            return "https://dev-tracker.sprinklr.io:50079";
        }

        @Override
        public RestAdapter.LogLevel getLogLevel() {
            return RestAdapter.LogLevel.FULL;
        }
    }

    interface RequestConfig {
        String getEndPoint();

        RestAdapter.LogLevel getLogLevel();
    }

}
