package com.tosslab.jandi.app.network.manager.token;

import android.text.TextUtils;

import com.tosslab.jandi.app.network.client.main.LoginApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class TokenRequestManager {
    public static final String TAG = TokenRequestManager.class.getSimpleName();

    private static final int RETRY_COUNT = 3;
    private static final int SKIP_TERM = 500;

    private static TokenRequestManager instance;
    private Lock lock;

    private LatestTokenInfo latestTokenInfo;

    private TokenRequestManager() {
        lock = new ReentrantLock();
    }

    public synchronized static TokenRequestManager getInstance() {
        if (instance == null) {
            instance = new TokenRequestManager();
        }
        return instance;
    }

    public ResAccessToken refreshToken() {
        lock.lock();

        // DB에 토큰 정보가 저장이 안되어 있는 경우에 대한 방어코드
        ResAccessToken savedResAccessToken = TokenUtil.getTokenObject();
        if (savedResAccessToken == null || TextUtils.isEmpty(savedResAccessToken.getRefreshToken())) {
            LogUtil.e(TAG, "Token is empty");
            lock.unlock();
            return null;
        }

        if (latestTokenInfo != null) {
            Date latestSavedDate = latestTokenInfo.getDate();
            if (System.currentTimeMillis() - latestSavedDate.getTime() <= SKIP_TERM) {
                LogUtil.i(TAG, "Token is already exists");
                lock.unlock();
                return latestTokenInfo.getAccessToken();
            }
        }

        int loginRetryCount = 0;

        String refreshToken = savedResAccessToken.getRefreshToken();

        ReqAccessToken refreshReqToken = ReqAccessToken.createRefreshReqToken(refreshToken);

        ResAccessToken accessToken = null;
        while (loginRetryCount < RETRY_COUNT) {
            try {
                accessToken = requestRefreshTokenAndSave(refreshReqToken);

                LogUtil.i(TAG, "RefreshToekn Success.");
                latestTokenInfo = new LatestTokenInfo(accessToken, new Date());

                break;
            } catch (Exception e) {
                loginRetryCount++;
            }
        }

        lock.unlock();
        return accessToken;
    }

    private ResAccessToken requestRefreshTokenAndSave(ReqAccessToken reqAccessToken) throws Exception {
        ResAccessToken accessToken = new LoginApi(InnerApiRetrofitBuilder.getInstance()).getAccessToken(reqAccessToken);
        TokenUtil.saveTokenInfoByRefresh(accessToken);
        return accessToken;
    }

    private static class LatestTokenInfo {
        private ResAccessToken accessToken;
        private Date date;

        public LatestTokenInfo(ResAccessToken accessToken, Date date) {
            this.accessToken = accessToken;
            this.date = date;
        }

        public ResAccessToken getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(ResAccessToken accessToken) {
            this.accessToken = accessToken;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }

}
