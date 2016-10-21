package com.tosslab.jandi.app.network.client.privatetopic.messages;

import com.tosslab.jandi.app.OkHttpClientTestFactory;
import com.tosslab.jandi.app.ValidationUtil;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GroupMessageApiDeprecatedTest {

    private GroupMessageApi.Api api;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        OkHttpClientTestFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        api = RetrofitBuilder.getInstance().create(GroupMessageApi.Api.class);
    }

    @Test
    public void getGroupMessages() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.getGroupMessages(1, 1, 1, 1).execute())).isFalse();
    }

    @Test
    public void getGroupMessagesUpdatedForMarker() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.getGroupMessagesUpdatedForMarker(1, 1, 1).execute())).isFalse();
    }

    @Test
    public void getGroupMessagesUpdatedForMarker1() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.getGroupMessagesUpdatedForMarker(1, 1, 1, 1).execute())).isFalse();
    }

    @Test
    public void getGroupMarkerMessages() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.getGroupMarkerMessages(1,1,1,1).execute())).isFalse();
    }

    @Test
    public void deletePrivateGroupMessage() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.deletePrivateGroupMessage(1,1,1).execute())).isFalse();
    }


}