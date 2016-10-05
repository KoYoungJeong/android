package com.tosslab.jandi.app.push;

import android.text.TextUtils;
import android.util.Log;

import com.tosslab.jandi.app.local.orm.repositories.PushTokenRepository;
import com.tosslab.jandi.app.network.client.account.devices.DeviceApi;
import com.tosslab.jandi.app.network.client.main.LoginApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.PushToken;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqPushToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

import dagger.Lazy;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class PushTokenRegister {
    public static final String TAG = PushTokenRegister.class.getSimpleName();

    private static PushTokenRegister tokenRegister;

    private PublishSubject<Object> tokenQueue;
    private Lazy<DeviceApi> deviceApi;

    private PushTokenRegister() {
        deviceApi = () -> new DeviceApi(RetrofitBuilder.getInstance());
        tokenQueue = PublishSubject.create();

        tokenQueue
                .throttleLast(200, TimeUnit.MILLISECONDS)
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .filter(o -> {
                    ResAccessToken accessToken = TokenUtil.getTokenObject();
                    return accessToken != null
                            && !TextUtils.isEmpty(accessToken.getAccessToken());
                })
                .filter(o -> !PushTokenRepository.getInstance().getPushTokenList().isEmpty())
                .map(o -> TokenUtil.getTokenObject())
                .subscribe(accessToken -> {
                    try {
                        String deviceId = accessToken.getDeviceId();
                        if (TextUtils.isEmpty(deviceId)) {
                            LogUtil.e(TAG, "deviceId is empty. try to refresh token");
                            accessToken = refreshToken(accessToken);
                            deviceId = accessToken.getDeviceId();
                        }

                        LogUtil.d(TAG, "deviceId = " + deviceId);

                        List<PushToken> pushTokenList = PushTokenRepository.getInstance().getPushTokenList();
                        ReqPushToken reqPushToken = new ReqPushToken(pushTokenList);
                        deviceApi.get().updatePushToken(deviceId, reqPushToken);
                        JandiPreference.setLatestPushTokenUpdate(System.currentTimeMillis());
                    } catch (Exception e) {
                        LogUtil.e(TAG, Log.getStackTraceString(e));
                    }
                });
    }

    public static PushTokenRegister getInstance() {
        if (tokenRegister == null) {
            tokenRegister = new PushTokenRegister();
        }
        return tokenRegister;
    }

    private ResAccessToken refreshToken(ResAccessToken accessToken) throws RetrofitException {
        ReqAccessToken refreshReqToken =
                ReqAccessToken.createRefreshReqToken(accessToken.getRefreshToken());
        ResAccessToken resAccessToken = new LoginApi(RetrofitBuilder.getInstance())
                .getAccessToken(refreshReqToken);

        TokenUtil.saveTokenInfoByRefresh(resAccessToken);
        return resAccessToken;
    }

    public void updateToken() {
        tokenQueue.onNext(new Object());
    }


}
