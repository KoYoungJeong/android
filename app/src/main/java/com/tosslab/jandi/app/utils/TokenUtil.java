package com.tosslab.jandi.app.utils;

import android.text.TextUtils;

import com.tosslab.jandi.app.local.orm.repositories.AccessTokenRepository;
import com.tosslab.jandi.app.network.models.ResAccessToken;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Steve SeongUg Jung on 14. 12. 17..
 */
public class TokenUtil {

    public static boolean saveTokenInfoByPassword(ResAccessToken accessToken) {
        return saveTokenInfoByRefresh(accessToken);
    }

    public static boolean saveTokenInfoByRefresh(ResAccessToken accessToken) {
        return TokenManager.getInstance().updateToken(accessToken);
    }

    public static void clearTokenInfo() {
        TokenManager.getInstance().clearToken();
    }

    public static String getRequestAuthentication() {
        ResAccessToken accessToken = TokenManager.getInstance().getToken();
        return String.format("%s %s", accessToken.getTokenType(), accessToken.getAccessToken());
    }

    public static String getRefreshToken() {
        return TokenManager.getInstance().getToken().getRefreshToken();
    }

    public static ResAccessToken getTokenObject() {
        return TokenManager.getInstance().getToken();
    }

    public static String getAccessToken() {
        return TokenManager.getInstance().getToken().getAccessToken();
    }

    static void refresh() {
        TokenManager.getInstance().refresh();
    }

    private static class TokenManager {
        static TokenManager tokenManager;
        private ResAccessToken cachedToken;

        private Lock lock;

        public TokenManager() {
            cachedToken = AccessTokenRepository.getRepository().getAccessToken();
            lock = new ReentrantLock();
        }

        synchronized static TokenManager getInstance() {
            if (tokenManager == null) {
                tokenManager = new TokenManager();
            }
            return tokenManager;
        }

        boolean updateToken(ResAccessToken newToken) {
            try {
                lock.lock();
                if (newToken == null) {
                    return false;
                }

                boolean result = AccessTokenRepository.getRepository().upsertAccessToken(newToken);

                if (!result) {
                    return false;
                }

                if (!TextUtils.isEmpty(newToken.getRefreshToken())) {
                    cachedToken.setRefreshToken(newToken.getRefreshToken());
                }

                if (!TextUtils.isEmpty(newToken.getAccessToken())) {

                    cachedToken.setAccessToken(newToken.getAccessToken());
                }

                if (!TextUtils.isEmpty(newToken.getTokenType())) {
                    cachedToken.setTokenType(newToken.getTokenType());
                }
                if (!TextUtils.isEmpty(newToken.getExpireTime())) {
                    cachedToken.setExpireTime(newToken.getExpireTime());
                }

                return true;

            } finally {
                lock.unlock();
            }

        }

        boolean clearToken() {
            try {
                lock.lock();
                boolean result = AccessTokenRepository.getRepository().deleteAccessToken();

                if (!result) {
                    return false;
                }
                cachedToken.set_id(0);
                cachedToken.setTokenType("");
                cachedToken.setAccessToken("");
                cachedToken.setRefreshToken("");
                cachedToken.setExpireTime("");
                return true;
            } finally {
                lock.unlock();
            }


        }

        ResAccessToken getToken() {
            try {
                lock.lock();
                return cachedToken;
            } finally {
                lock.unlock();
            }


        }

        public void refresh() {
            try {
                lock.lock();
                cachedToken = AccessTokenRepository.getRepository().getAccessToken();
            } finally {
                lock.unlock();
            }
        }
    }


}
