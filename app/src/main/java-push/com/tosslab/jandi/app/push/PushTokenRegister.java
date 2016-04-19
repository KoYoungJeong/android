package com.tosslab.jandi.app.push;

import com.tosslab.jandi.app.local.orm.repositories.AccessTokenRepository;
import com.tosslab.jandi.app.local.orm.repositories.PushTokenRepository;
import com.tosslab.jandi.app.network.client.account.devices.DeviceApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.PushToken;
import com.tosslab.jandi.app.network.models.ReqPushToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;

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
                .subscribe(o -> {
                    ResAccessToken accessToken = AccessTokenRepository.getRepository().getAccessToken();
                    String deviceId = accessToken.getDeviceId();
                    List<PushToken> pushTokenList = PushTokenRepository.getInstance().getPushTokenList();
                    if (!pushTokenList.isEmpty()) {
                        try {
                            ReqPushToken reqPushToken = new ReqPushToken(pushTokenList);
                            deviceApi.get().updatePushToken(deviceId, reqPushToken);
                        } catch (RetrofitException e) {
                            e.printStackTrace();
                        }
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
