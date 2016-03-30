package com.tosslab.jandi.app.network.client.account.devices;

import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;

import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitAdapterBuilder;
import com.tosslab.jandi.app.network.models.ReqDeviceToken;
import com.tosslab.jandi.app.network.models.ReqNotificationRegister;
import com.tosslab.jandi.app.network.models.ReqNotificationTarget;
import com.tosslab.jandi.app.network.models.ReqSubscibeToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import rx.Observable;
import rx.observers.TestSubscriber;
import setup.BaseInitUtil;

@RunWith(AndroidJUnit4.class)
public class DeviceApiTest {

    private static final String SAMPLE_TOKEN = "sdkjfhlakjdfhlkajsdhflkajshdf";

    private DeviceApi deviceApi;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @Before
    public void setUp() throws Exception {
        deviceApi = new DeviceApi(RetrofitAdapterBuilder.newInstance());

    }

    @Test
    public void testRegisterNotificationToken() throws Exception {
        ResAccountInfo accountInfo = deviceApi.registerNotificationToken(new ReqNotificationRegister("android", SAMPLE_TOKEN));

        TestSubscriber<ResAccountInfo.UserDevice> subscriber = TestSubscriber.create();
        Observable.from(accountInfo.getDevices())
                .filter(userDevice -> TextUtils.equals(userDevice.getToken(), SAMPLE_TOKEN))
                .subscribe(subscriber);

        subscriber.assertValueCount(1);
        subscriber.assertNoErrors();
        subscriber.assertCompleted();
    }

    @Test
    public void testDeleteNotificationToken() throws Exception {
        deviceApi.registerNotificationToken(new ReqNotificationRegister("android", SAMPLE_TOKEN));
        ResAccountInfo accountInfo = deviceApi.deleteNotificationToken(new ReqDeviceToken(SAMPLE_TOKEN));

        TestSubscriber<ResAccountInfo.UserDevice> subscriber = TestSubscriber.create();
        Observable.from(accountInfo.getDevices())
                .filter(userDevice -> TextUtils.equals(userDevice.getToken(), SAMPLE_TOKEN))
                .subscribe(subscriber);

        subscriber.assertNoValues();
        subscriber.assertNoErrors();
        subscriber.assertCompleted();
    }

    @Test
    public void testSubscribeStateNotification() throws Exception {
        deviceApi.registerNotificationToken(new ReqNotificationRegister("android", SAMPLE_TOKEN));
        ResAccountInfo accountInfo = deviceApi.subscribeStateNotification(new ReqSubscibeToken(SAMPLE_TOKEN, false));
        TestSubscriber<ResAccountInfo.UserDevice> subscriber = TestSubscriber.create();
        Observable.from(accountInfo.getDevices())
                .filter(userDevice -> TextUtils.equals(userDevice.getToken(), SAMPLE_TOKEN))
                .filter(userDevice -> !userDevice.isSubscribe())
                .subscribe(subscriber);

        subscriber.assertValueCount(1);
        subscriber.assertNoErrors();
        subscriber.assertCompleted();
    }

    @Ignore
    @Test
    public void testGetNotificationBadge() throws Exception {
        deviceApi.getNotificationBadge(new ReqNotificationTarget(""));
    }
}