package com.tosslab.jandi.app.network.client.rooms;

import com.tosslab.jandi.app.OkHttpClientTestFactory;
import com.tosslab.jandi.app.ValidationUtil;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqMember;
import com.tosslab.jandi.app.network.models.ReqOwner;
import com.tosslab.jandi.app.network.models.ReqUpdateTopicPushSubscribe;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.network.models.messages.ReqTextMessage;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class RoomsApiDeprecatedTest {

    private RoomsApi.Api api;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        OkHttpClientTestFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        api = RetrofitBuilder.getInstance().create(RoomsApi.Api.class);
    }

    @Test
    public void updateTopicPushSubscribe() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.updateTopicPushSubscribe(1,1,new ReqUpdateTopicPushSubscribe(true)).execute())).isFalse();
    }

    @Test
    public void kickUserFromTopic() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.kickUserFromTopic(1, 1, new ReqMember(1)).execute())).isFalse();
    }

    @Test
    public void assignToTopicOwner() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.assignToTopicOwner(1,1,new ReqOwner(1)).execute())).isFalse();
    }

    @Test
    public void sendMessage() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.sendMessage(1,1,new ReqTextMessage("asd", new ArrayList<MentionObject>())).execute())).isFalse();
    }


}