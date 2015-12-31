package com.tosslab.jandi.app.network.manager.apiexecutor;

import android.support.v4.util.Pools;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.network.exception.ConnectionNotFoundException;
import com.tosslab.jandi.app.network.manager.token.TokenRequestManager;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.services.SignOutService;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import retrofit.RetrofitError;
import retrofit.mime.TypedByteArray;

/**
 * Created by tee on 15. 6. 20..
 */
public class PoolableRequestApiExecutor {

    public static final int MAX_POOL_SIZE = 10;
    private static final int RETRY_COUNT = 2;
    private static final Pools.SynchronizedPool sExecutorPool = new Pools.SynchronizedPool(MAX_POOL_SIZE);

    private int retryCnt = 0;

    private PoolableRequestApiExecutor() {
    }

    public static PoolableRequestApiExecutor obtain() {
        PoolableRequestApiExecutor requestApiExecutor = (PoolableRequestApiExecutor) sExecutorPool.acquire();
        return (requestApiExecutor != null) ? requestApiExecutor : new PoolableRequestApiExecutor();
    }

    public void recycle() {
        sExecutorPool.release(this);
    }

    public <RESULT> RESULT execute(IExecutor<RESULT> apiExecutor) {

        try {
            RESULT object = apiExecutor.execute();
            retryCnt = 0;
            return object;
        } catch (RetrofitError e) {
            return handleException(e, apiExecutor);
        }

    }

    private <RESULT> RESULT handleException(RetrofitError e, IExecutor<RESULT> apiExecutor) {

        // 현재(2015/6) 시나리오엔 존재하지 않지만 Client측의 Network Connection에러를 UI단에 던지기 위한 코드 추가
        if (!isActiveNetwork()) {
            LogUtil.e("Disconnect Network : " + e.getUrl());
            throw RetrofitError.unexpectedError(JandiConstantsForFlavors.SERVICE_INNER_API_URL,
                    new ConnectionNotFoundException());
        }

        if (e.getKind() == RetrofitError.Kind.NETWORK) {
            if (retryCnt < RETRY_COUNT) {
                retryCnt++;
                return execute(apiExecutor);
            } else {
                retryCnt = 0;
                throw e;
            }
        } else if (e.getKind() == RetrofitError.Kind.HTTP) {
            if (e.getResponse().getBody() != null) {
                LogUtil.e("HTTP Error : " + new String(((TypedByteArray) e.getResponse().getBody()).getBytes()));
            }
            int status = e.getResponse().getStatus();
            if (status == JandiConstants.NetworkError.UNAUTHORIZED) {
                LogUtil.e("UNAUTHORIZED : " + e.getUrl());
                ResAccessToken accessToken = TokenRequestManager.getInstance().refreshToken();
                if (accessToken != null) {
                    LogUtil.i("Refresh Token Success : " + e.getUrl());
                    try {
                        return apiExecutor.execute();
                    } catch (RetrofitError e1) {
                        // unknown exception
                        LogUtil.e("Retry Fail : " + e.getUrl());
                        throw e1;
                    }
                } else {
                    // unauthorized exception
                    LogUtil.e("Refresh Token Fail : " + e.getUrl());
                    SignOutService.start();
                    throw e;
                }
            } else {
                // exception, not unauthorized
                LogUtil.e("Request Fail : " + e.getUrl());
                throw e;
            }
        } else {
            throw e;
        }
    }

    private boolean isActiveNetwork() {
        return NetworkCheckUtil.isConnected();
    }
}