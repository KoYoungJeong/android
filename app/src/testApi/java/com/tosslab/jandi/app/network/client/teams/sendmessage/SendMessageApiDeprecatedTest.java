package com.tosslab.jandi.app.network.client.teams.sendmessage;

import com.tosslab.jandi.app.OkHttpClientTestFactory;
import com.tosslab.jandi.app.ValidationUtil;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqSendMessages;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SendMessageApiDeprecatedTest {

    private SendMessageApi.Api api;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        OkHttpClientTestFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        api = RetrofitBuilder.getInstance().create(SendMessageApi.Api.class);
    }

    @Test
    public void sendMessage() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.sendMessage(1, 1, new ReqSendMessages()).execute())).isFalse();
    }


}