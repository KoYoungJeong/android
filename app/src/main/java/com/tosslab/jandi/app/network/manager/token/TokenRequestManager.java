package com.tosslab.jandi.app.network.manager.token;

import com.tosslab.jandi.app.network.manager.restapiclient.JacksonConvertedSimpleRestApiClient;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import retrofit.RetrofitError;

public class TokenRequestManager {
    private static TokenRequestManager instance;
    Queue<String> queue;
    private Lock lock;


    private ResAccessToken lastestToken;

    private TokenRequestManager() {
        queue = new ConcurrentLinkedQueue<>();
        lock = new ReentrantLock();
    }

    synchronized public static TokenRequestManager getInstance() {
        if (instance == null) {
            instance = new TokenRequestManager();
        }
        return instance;
    }

    public ResAccessToken get(String refreshToken) {
        queue.offer(refreshToken);

        lock.lock();

        ResAccessToken accessToken = null;

        if (lastestToken != null) {
            accessToken = lastestToken;
            queue.poll();
            if (queue.peek() == null) {
                lastestToken = null;
            }
            lock.unlock();
            return accessToken;
        }

        try {
            ReqAccessToken refreshReqToken = ReqAccessToken
                    .createRefreshReqToken(refreshToken);
            accessToken = new JacksonConvertedSimpleRestApiClient()
                    .getAccessTokenByMainRest(refreshReqToken);

            queue.poll();
            if (queue.peek() != null) {
                lastestToken = accessToken;
            } else {
                lastestToken = null;
            }

        } catch (RetrofitError e) {
            queue.poll();
            lastestToken = null;
        } finally {
            lock.unlock();
        }

        return accessToken;
    }
}
