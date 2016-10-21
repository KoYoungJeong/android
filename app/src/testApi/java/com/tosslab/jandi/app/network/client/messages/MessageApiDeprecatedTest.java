package com.tosslab.jandi.app.network.client.messages;

import com.tosslab.jandi.app.OkHttpClientTestFactory;
import com.tosslab.jandi.app.ValidationUtil;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqMentionMarkerUpdate;
import com.tosslab.jandi.app.network.models.ReqNull;
import com.tosslab.jandi.app.network.models.ReqSetMarker;
import com.tosslab.jandi.app.network.models.ReqShareMessage;
import com.tosslab.jandi.app.network.models.ReqUnshareMessage;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class MessageApiDeprecatedTest {

    private MessageApi.Api api;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        OkHttpClientTestFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        api = RetrofitBuilder.getInstance().create(MessageApi.Api.class);
    }

    @Test
    public void setMarker() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.setMarker(1, new ReqSetMarker(1, 1, "")).execute())).isFalse();
    }

    @Test
    public void getFileDetail() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.getFileDetail(1, 1).execute())).isFalse();
    }

    @Test
    public void shareMessage() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.shareMessage(1, new ReqShareMessage()).execute())).isFalse();
    }

    @Test
    public void unshareMessage() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.unshareMessage(1, new ReqUnshareMessage(1, 1)).execute())).isFalse();
    }

    @Test
    public void getRoomUpdateMessage() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.getRoomUpdateMessage(1, 1, 1).execute())).isFalse();
        fail("It is deprecated");
    }

    @Test
    public void getMessage() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.getMessage(1, 1).execute())).isFalse();
    }

    @Test
    public void getStarredMessages() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.getStarredMessages(1, 1, 1, "").execute())).isFalse();
    }

    @Test
    public void registStarredMessage() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.registStarredMessage(1, 1, new ReqNull()).execute())).isFalse();
    }

    @Test
    public void unregistStarredMessage() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.unregistStarredMessage(1, 1).execute())).isFalse();
    }

    @Test
    public void getMentionedMessages() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.getMentionedMessages(1, 1, 1).execute())).isFalse();
    }

    @Test
    public void updateMentionMarker() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.updateMentionMarker(1, ReqMentionMarkerUpdate.create(1)).execute())).isFalse();
    }


}