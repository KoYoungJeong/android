package com.tosslab.jandi.app.network.client.direct.message;

import com.tosslab.jandi.app.OkHttpClientTestFactory;
import com.tosslab.jandi.app.ValidationUtil;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DirectMessageApiDeprecatedTest {

    private DirectMessageApi.Api api;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        OkHttpClientTestFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        api = RetrofitBuilder.getInstance().create(DirectMessageApi.Api.class);
    }

    @Test
    public void getDirectMessages() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.getDirectMessages(1, 1, 1, 1))).isFalse();
    }

    @Test
    public void getDirectMessagesUpdatedForMarker() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.getDirectMessagesUpdatedForMarker(1, 1, 1))).isFalse();
    }

    @Test
    public void getDirectMessagesUpdatedForMarker1() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.getDirectMessagesUpdatedForMarker(1, 1, 1, 1))).isFalse();

    }

    @Test
    public void getDirectMarkerMessages() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.getDirectMarkerMessages(1,1,1,1))).isFalse();
    }

    @Test
    public void deleteDirectMessage() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.deleteDirectMessage(1,1,1))).isFalse();
    }


}