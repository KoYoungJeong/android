package com.tosslab.jandi.app.network.client.publictopic.messages;

import com.tosslab.jandi.app.OkHttpClientTestFactory;
import com.tosslab.jandi.app.ValidationUtil;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ChannelMessageApiTest {

    private ChannelMessageApi.Api api;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        OkHttpClientTestFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        api = RetrofitBuilder.getInstance().create(ChannelMessageApi.Api.class);
    }

    @Test
    public void getPublicTopicMessages() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.getPublicTopicMessages(1,1,1,1).execute())).isFalse();
    }

    @Test
    public void getPublicTopicUpdatedMessagesForMarker() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.getPublicTopicUpdatedMessagesForMarker(1,1,1).execute())).isFalse();
    }

    @Test
    public void getPublicTopicUpdatedMessagesForMarker1() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.getPublicTopicUpdatedMessagesForMarker(1,1,1, 1).execute())).isFalse();
    }

    @Test
    public void getPublicTopicMarkerMessages() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.getPublicTopicMarkerMessages(1,1,1,1).execute())).isFalse();
    }

    @Test
    public void deletePublicTopicMessage() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.deletePublicTopicMessage(1,1,1).execute())).isFalse();
    }


}