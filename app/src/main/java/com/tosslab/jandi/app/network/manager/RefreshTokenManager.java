package com.tosslab.jandi.app.network.manager;

import android.content.Context;

import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.springframework.web.client.HttpStatusCodeException;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Steve SeongUg Jung on 14. 12. 16..
 */
public class RefreshTokenManager {

    private static RefreshTokenManager refreshTokenManager;

    private Context context;

    private CopyOnWriteArrayList<Request> refreshRequestList;
    private CopyOnWriteArrayList<Callback> callbackList;

    private AtomicBoolean isLogining;

    private RefreshTokenManager(Context context) {
        this.context = context;
        this.refreshRequestList = new CopyOnWriteArrayList<Request>();
        this.callbackList = new CopyOnWriteArrayList<Callback>();
        this.isLogining = new AtomicBoolean(false);
    }

    private static RefreshTokenManager getInstance(Context context) {
        if (refreshTokenManager == null) {
            refreshTokenManager = new RefreshTokenManager(context);
        }
        return refreshTokenManager;
    }
    
    

    public void requestRefresh(Request<ResAccessToken> refreshRequest, Callback callback) {

        refreshRequestList.add(refreshRequest);

        if (!isLogining.get() && refreshRequestList.size() > 0) {
            // Define Logining...
            
        }
    }


    private static class RefreshSync implements Runnable {

        private Request<ResAccessToken> request;
        private Callback callback;

        private RefreshSync(Request<ResAccessToken> request, Callback callback) {
            this.request = request;
            this.callback = callback;
        }

        @Override
        public void run() {
            ResAccessToken accessToken = null;
            int loginRetryCount = 0;
            while (accessToken == null && loginRetryCount <= 3) {
                ++loginRetryCount;
                try {
                    accessToken = request.request();
                    if (callback != null) {
                        callback.onResponse(accessToken);
                    }
                    return;
                } catch (HttpStatusCodeException e) {
                    e.printStackTrace();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }

            if (callback != null) {
                callback.onResponse(null);
            }
        }
    }

    private interface Callback {
        void onResponse(ResAccessToken token);
    }

}
