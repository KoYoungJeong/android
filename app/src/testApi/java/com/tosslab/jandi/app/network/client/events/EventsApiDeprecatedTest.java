package com.tosslab.jandi.app.network.client.events;

import com.tosslab.jandi.app.OkHttpClientTestFactory;
import com.tosslab.jandi.app.ValidationUtil;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EventsApiDeprecatedTest {

    private EventsApi.Api api;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        OkHttpClientTestFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        api = RetrofitBuilder.getInstance().create(EventsApi.Api.class);
    }

    @Test
    public void getEventHistory() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.getEventHistory(1, 1))).isFalse();
    }

    @Test
    public void getEventHistory1() throws Exception {

        assertThat(ValidationUtil.isDeprecated(api.getEventHistory(1, 1, 1))).isFalse();
    }

}