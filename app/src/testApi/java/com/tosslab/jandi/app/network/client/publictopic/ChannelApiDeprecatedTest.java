package com.tosslab.jandi.app.network.client.publictopic;

import com.tosslab.jandi.app.OkHttpClientTestFactory;
import com.tosslab.jandi.app.ValidationUtil;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqDeleteTopic;
import com.tosslab.jandi.app.network.models.ReqInviteTopicUsers;
import com.tosslab.jandi.app.network.models.ReqModifyTopicAutoJoin;
import com.tosslab.jandi.app.network.models.ReqModifyTopicDescription;
import com.tosslab.jandi.app.network.models.ReqModifyTopicName;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ChannelApiDeprecatedTest {

    private ChannelApi.Api api;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        OkHttpClientTestFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        api = RetrofitBuilder.getInstance().create(ChannelApi.Api.class);
    }

    @Test
    public void createChannel() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.createChannel(1, new ReqCreateTopic()).execute())).isFalse();
    }

    @Test
    public void modifyPublicTopicName() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.modifyPublicTopicName(1,1, new ReqModifyTopicName()).execute())).isFalse();
    }

    @Test
    public void modifyPublicTopicDescription() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.modifyPublicTopicDescription(1,1, new ReqModifyTopicDescription()).execute())).isFalse();
    }

    @Test
    public void modifyPublicTopicAutoJoin() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.modifyPublicTopicAutoJoin(1,1, new ReqModifyTopicAutoJoin()).execute())).isFalse();
    }

    @Test
    public void deleteTopic() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.deleteTopic(1, new ReqDeleteTopic(1)).execute())).isFalse();
    }

    @Test
    public void joinTopic() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.joinTopic(1, new ReqDeleteTopic(1)).execute())).isFalse();
    }

    @Test
    public void leaveTopic() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.leaveTopic(1, new ReqDeleteTopic(1)).execute())).isFalse();
    }

    @Test
    public void invitePublicTopic() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.invitePublicTopic(1, new ReqInviteTopicUsers(new ArrayList<Long>(),1)).execute())).isFalse();
    }

    @Test
    public void modifyReadOnly() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("isAnnouncement", true);
        assertThat(ValidationUtil.isDeprecated(api.modifyReadOnly(1, 1,map).execute())).isFalse();

    }

}