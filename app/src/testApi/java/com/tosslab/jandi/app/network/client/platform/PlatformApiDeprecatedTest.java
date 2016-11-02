package com.tosslab.jandi.app.network.client.platform;

import com.tosslab.jandi.app.OkHttpClientTestFactory;
import com.tosslab.jandi.app.ValidationUtil;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqUpdatePlatformStatus;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PlatformApiDeprecatedTest {

    private PlatformApi.Api api;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        OkHttpClientTestFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        api = RetrofitBuilder.getInstance().create(PlatformApi.Api.class);
    }

    @Test
    public void updatePlatformStatus() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.updatePlatformStatus(new ReqUpdatePlatformStatus(true)).execute())).isFalse();
    }


}