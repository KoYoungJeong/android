package com.tosslab.jandi.app.network.client.messages.search;

import com.tosslab.jandi.app.OkHttpClientTestFactory;
import com.tosslab.jandi.app.ValidationUtil;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageSearchApiDeprecatedTest {

    private MessageSearchApi.Api api;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        OkHttpClientTestFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        api = RetrofitBuilder.getInstance().create(MessageSearchApi.Api.class);
    }

    @Test
    public void searchMessages() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.searchMessages(1,"12",1,1).execute())).isFalse();
    }

    @Test
    public void searchMessages1() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.searchMessages(1,"12",1,1,1,1).execute())).isFalse();
    }

    @Test
    public void searchMessagesByEntityId() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.searchMessagesByEntityId(1,"",1,1,1).execute())).isFalse();
    }

    @Test
    public void searchMessagesByWriterId() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.searchMessagesByWriterId(1,"12",1,1,1).execute())).isFalse();
    }


}