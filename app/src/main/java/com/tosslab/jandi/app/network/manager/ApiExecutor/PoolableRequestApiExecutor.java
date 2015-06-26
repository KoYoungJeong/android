package com.tosslab.jandi.app.network.manager.ApiExecutor;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.io.IOException;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 6. 20..
 */
public class PoolableRequestApiExecutor {

    private static final int POOL_NUMBER = 10;
    private static final int RETRY_COUNT = 3;
    //    private static final Pools.SynchronizedPool RequestApiExecutorPool =
//            new Pools.SynchronizedPool(POOL_NUMBER);
    private static final AwaitablePool RequestApiExecutorPool
            = new AwaitablePool(POOL_NUMBER);
    private int retryCnt = 0;

    private PoolableRequestApiExecutor() {
    }

    public static PoolableRequestApiExecutor obtain() {
        PoolableRequestApiExecutor requestApiExecutor = (PoolableRequestApiExecutor) RequestApiExecutorPool.acquire();
        return (requestApiExecutor != null) ? requestApiExecutor : new PoolableRequestApiExecutor();
    }

    public void recycle() {
        RequestApiExecutorPool.release(this);
    }

    public Object execute(IExecutor apiExecutor) {
        try {
            Object object = apiExecutor.execute();
            retryCnt = 0;
            return object;
        } catch (RetrofitError e) {
            if (e.getKind() == RetrofitError.Kind.NETWORK) {
                if (retryCnt < RETRY_COUNT) {
                    retryCnt++;
                    return execute(apiExecutor);
                } else {
                    retryCnt = 0;
                    throw RetrofitError.networkError(JandiConstantsForFlavors.SERVICE_ROOT_URL + "/inner-api", new IOException());
                }
            }

            if (e.getResponse() != null && e.getResponse().getStatus() == JandiConstants.NetworkError.UNAUTHORIZED) {
                ResAccessToken accessToken = refreshToken();
                if (accessToken != null) {
                    try {
                        return apiExecutor.execute();
                    } catch (RetrofitError e1) {
                        // unknown exception
                        LogUtil.e("Retry Fail");
                        //TODO Exception 상세화 필요
                        throw RetrofitError.unexpectedError(JandiConstantsForFlavors.SERVICE_ROOT_URL + "/inner-api", new Exception());
                    }
                } else {
                    // unauthorized exception
                    LogUtil.e("Refresh Token Fail", e);
                    //TODO Exception 상세화 필요
                    throw RetrofitError.unexpectedError(JandiConstantsForFlavors.SERVICE_ROOT_URL + "/inner-api", new Exception());
                }
            } else {
                // exception, not unauthorized
                JandiSocketService.stopSocketServiceIfRunning(JandiApplication.getContext());
                LogUtil.e("Request Fail", e);
                //TODO Exception 상세화 필요
                throw RetrofitError.unexpectedError(JandiConstantsForFlavors.SERVICE_ROOT_URL + "/inner-api", new Exception());
            }
        } catch (Exception e) {
            LogUtil.e("Unknown Request Error : ", e);
            //TODO Exception 상세화 필요
            throw RetrofitError.unexpectedError(JandiConstantsForFlavors.SERVICE_ROOT_URL + "/inner-api", new Exception());
        }
    }

    private ResAccessToken refreshToken() {
        ResAccessToken accessToken = null;
        int loginRetryCount = 0;
        while (accessToken == null && loginRetryCount <= 3) {
            ++loginRetryCount;
            try {
                //Request Access token, and save token
                ReqAccessToken refreshReqToken = ReqAccessToken
                        .createRefreshReqToken(JandiPreference.getRefreshToken(JandiApplication.getContext()));
                accessToken = RequestApiManager.getInstance().getAccessTokenByMainRest(refreshReqToken);
                TokenUtil.saveTokenInfoByRefresh(accessToken);
            } catch (RetrofitError e) {
                LogUtil.e("Refresh Token Fail", e);
                if (e.getResponse().getStatus() != JandiConstants.NetworkError.UNAUTHORIZED) {
                    return null;
                }
            }
        }
        return accessToken;
    }
}