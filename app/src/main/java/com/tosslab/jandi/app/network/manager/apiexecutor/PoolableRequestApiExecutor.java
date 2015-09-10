package com.tosslab.jandi.app.network.manager.apiexecutor;

import android.support.v4.util.Pools;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.network.exception.ConnectionNotFoundException;
import com.tosslab.jandi.app.network.manager.restapiclient.JacksonConvertedSimpleRestApiClient;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 6. 20..
 */
public class PoolableRequestApiExecutor {

    public static final int MAX_POOL_SIZE = 10;
    private static final int RETRY_COUNT = 2;
    private static final Pools.SynchronizedPool sExecutorPool = new Pools.SynchronizedPool(MAX_POOL_SIZE);
    //    private static final AwaitablePool sExecutorPool = new AwaitablePool(MAX_POOL_SIZE);
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
            if (e.getResponse().getStatus() == JandiConstants.NetworkError.UNAUTHORIZED) {
                ResAccessToken accessToken = refreshToken();
                if (accessToken != null) {
                    try {
                        return apiExecutor.execute();
                    } catch (RetrofitError e1) {
                        // unknown exception
                        LogUtil.e("Retry Fail");
                        throw e1;
                    }
                } else {
                    // unauthorized exception
                    LogUtil.e("Refresh Token Fail", e);
                    throw e;
                }
            } else {
                // exception, not unauthorized
//                JandiSocketService.stopService(JandiApplication.getContext());
                LogUtil.e("Request Fail", e);
                throw e;
            }
        } else {
            throw e;
        }
    }

    //Todo 네트워크 상태를 체크하는 공용 클래스 생성 필요 - 중복 사용이 예상되는 메서드
    public boolean isActiveNetwork() {
        return NetworkCheckUtil.isConnected();
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
                accessToken = new JacksonConvertedSimpleRestApiClient().getAccessTokenByMainRest(refreshReqToken);
                TokenUtil.saveTokenInfoByRefresh(accessToken);
            } catch (RetrofitError e) {
                LogUtil.e("Refresh Token Fail", e);
                if (e.getResponse().getStatus() != JandiConstants.NetworkError.UNAUTHORIZED) {
                    return null;
                }
            }
        }
        LogUtil.e(accessToken.toString());
        return accessToken;
    }
}