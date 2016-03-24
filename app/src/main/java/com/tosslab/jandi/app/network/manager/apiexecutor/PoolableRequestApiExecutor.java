package com.tosslab.jandi.app.network.manager.apiexecutor;

import android.support.v4.util.Pools;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.token.TokenRequestManager;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.services.SignOutService;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import java.io.IOException;

import retrofit2.Response;

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

    public <RESULT> RESULT execute(Executor<Response<RESULT>> apiExecutor) throws RetrofitException {

        try {
            Response<RESULT> response = apiExecutor.execute();
            if (response.isSuccessful()) {
                RESULT object = response.body();
                retryCnt = 0;
                return object;
            } else {
                return handleException(apiExecutor, response, null);
            }
        } catch (Exception e) {
            return handleException(apiExecutor, null, e);
        }

    }

    private <RESULT> RESULT handleException(Executor<Response<RESULT>> apiExecutor, Response response, Exception e) throws RetrofitException {

        // 현재(2015/6) 시나리오엔 존재하지 않지만 Client측의 Network Connection에러를 UI단에 던지기 위한 코드 추가
        if (!isActiveNetwork()) {
            throw RetrofitException.create(response.code());
        }

        if (e instanceof RuntimeException) {
            throw RetrofitException.create(400);
        } else if (e instanceof IOException) {
            if (retryCnt < RETRY_COUNT) {
                retryCnt++;
                return execute(apiExecutor);
            } else {
                retryCnt = 0;
                throw RetrofitException.create(400);
            }
        } else if (response == null) {
            throw RetrofitException.create(400);
        } else {
            int status = response.code();
            if (status == JandiConstants.NetworkError.UNAUTHORIZED) {
                ResAccessToken accessToken = TokenRequestManager.getInstance().refreshToken();
                if (accessToken != null) {
                    try {
                        return apiExecutor.execute().body();
                    } catch (Exception e1) {
                        // unknown exception
                        throw RetrofitException.create(401);
                    }
                } else {
                    // unauthorized exception
                    SignOutService.start();
                    throw RetrofitException.create(401);
                }
            } else {
                // exception, not unauthorized
                throw RetrofitException.create(400);
            }
        }
    }

    private boolean isActiveNetwork() {
        return NetworkCheckUtil.isConnected();
    }
}