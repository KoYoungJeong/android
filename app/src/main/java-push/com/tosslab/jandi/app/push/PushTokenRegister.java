package com.tosslab.jandi.app.push;

import android.text.TextUtils;

import com.tosslab.jandi.app.local.orm.repositories.PushTokenRepository;
import com.tosslab.jandi.app.network.client.account.devices.DeviceApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.PushToken;
import com.tosslab.jandi.app.network.models.ReqPushToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.utils.TokenUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

import dagger.Lazy;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class PushTokenRegister {

    private static PushTokenRegister tokenRegister;

    private PublishSubject<Object> tokenQueue;
    private Lazy<DeviceApi> deviceApi;

    private PushTokenRegister() {
        deviceApi = () -> new DeviceApi(RetrofitBuilder.newInstance());
        tokenQueue = PublishSubject.create();

        tokenQueue
                .throttleLast(200, TimeUnit.MILLISECONDS)
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .filter(o -> {
                    ResAccessToken accessToken = TokenUtil.getTokenObject();
                    return accessToken != null
                            && !TextUtils.isEmpty(accessToken.getAccessToken())
                            && !TextUtils.isEmpty(accessToken.getDeviceId());
                })
                .filter(o -> !PushTokenRepository.getInstance().getPushTokenList().isEmpty())
                .map(o -> TokenUtil.getTokenObject())
                .subscribe(accessToken -> {
                    String deviceId = accessToken.getDeviceId();
                    List<PushToken> pushTokenList = PushTokenRepository.getInstance().getPushTokenList();
                    try {
                        ReqPushToken reqPushToken = new ReqPushToken(pushTokenList);
                        deviceApi.get().updatePushToken(deviceId, reqPushToken);
                    } catch (RetrofitException e) {
                        e.printStackTrace();
                    }
                });
    }

    public static PushTokenRegister getInstance() {
        if (tokenRegister == null) {
            tokenRegister = new PushTokenRegister();
        }
        return tokenRegister;
    }

    public void updateToken() {
        tokenQueue.onNext(new Object());
    }


}
