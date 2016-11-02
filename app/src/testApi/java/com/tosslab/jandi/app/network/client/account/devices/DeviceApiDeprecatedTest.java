package com.tosslab.jandi.app.network.client.account.devices;

import com.tosslab.jandi.app.OkHttpClientTestFactory;
import com.tosslab.jandi.app.ValidationUtil;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.PushToken;
import com.tosslab.jandi.app.network.models.ReqPushToken;
import com.tosslab.jandi.app.network.models.ReqSubscribeToken;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class DeviceApiDeprecatedTest {

    private DeviceApi.Api api;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        OkHttpClientTestFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        api = RetrofitBuilder.getInstance().create(DeviceApi.Api.class);
    }

    @Test
    public void updatePushToken() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.updatePushToken("asd", new ReqPushToken(Arrays.asList(new PushToken("as","askdj")))).execute())).isFalse();
    }

    @Test
    public void updateSubscribe() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.updateSubScribe("asd", new ReqSubscribeToken(true)).execute())).isFalse();
    }

    @Test
    public void deleteDevice() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.deleteDevice("adsjkhalskdjh").execute())).isFalse();
    }


}