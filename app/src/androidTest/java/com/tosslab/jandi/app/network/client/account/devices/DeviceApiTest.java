package com.tosslab.jandi.app.network.client.account.devices;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.client.main.LoginApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.network.models.PushToken;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqPushToken;
import com.tosslab.jandi.app.network.models.ReqSubscribeToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResDeviceSubscribe;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(AndroidJUnit4.class)
public class DeviceApiTest {

    private static final String SAMPLE_TOKEN = "sdkjfhlakjdfhlkajsdhflkajshdf";

    private DeviceApi deviceApi;
    private ResAccessToken accessToken;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @Before
    public void setUp() throws Exception {
        deviceApi = new DeviceApi(InnerApiRetrofitBuilder.getInstance());
        accessToken = TokenUtil.getTokenObject();

    }

    @Test
    public void testUpdatePushToken() throws Exception {
        ResCommon resCommon = deviceApi.updatePushToken(accessToken.getDeviceId(), new ReqPushToken(Arrays.asList(new PushToken("gcm", "asdad"))));
        assertThat(resCommon).isNotNull();
    }

    @Test
    public void testUpdateSubscribe() throws Exception {
        ResDeviceSubscribe resDeviceSubscribe = deviceApi.updateSubscribe(accessToken.getDeviceId(), new ReqSubscribeToken(false));
        assertThat(resDeviceSubscribe).isNotNull();
        assertThat(resDeviceSubscribe.getId()).isEqualTo(accessToken.getDeviceId());
        assertThat(resDeviceSubscribe.isSubscribe()).isFalse();

        resDeviceSubscribe = deviceApi.updateSubscribe(accessToken.getDeviceId(), new ReqSubscribeToken(true));
        assertThat(resDeviceSubscribe).isNotNull();
        assertThat(resDeviceSubscribe.getId()).isEqualTo(accessToken.getDeviceId());
        assertThat(resDeviceSubscribe.isSubscribe()).isTrue();
    }

    @Ignore
    @Test
    public void testDeleteDevice() throws Exception {
        accessToken = new LoginApi(InnerApiRetrofitBuilder.getInstance()).getAccessToken(
                ReqAccessToken.createPasswordReqToken(BaseInitUtil.TEST_EMAIL, BaseInitUtil.TEST_PASSWORD));
        TokenUtil.saveTokenInfoByPassword(accessToken);
        ResDeviceSubscribe resDeviceSubscribe = deviceApi.deleteDevice(accessToken.getDeviceId());
        assertThat(resDeviceSubscribe).isNotNull();
        assertThat(resDeviceSubscribe.getId()).isEqualTo(accessToken.getDeviceId());
        assertThat(resDeviceSubscribe.isSubscribe()).isFalse();
    }
}