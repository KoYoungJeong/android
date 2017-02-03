package com.tosslab.jandi.app.network.client.privatetopic;

import com.tosslab.jandi.app.OkHttpClientTestFactory;
import com.tosslab.jandi.app.ValidationUtil;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqInviteTopicUsers;
import com.tosslab.jandi.app.network.models.ReqModifyTopicDescription;
import com.tosslab.jandi.app.network.models.ReqModifyTopicName;
import com.tosslab.jandi.app.network.models.ReqTeam;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class GroupApiDeprecatedTest {

    private GroupApi.Api api;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        OkHttpClientTestFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        api = RetrofitBuilder.getInstance().create(GroupApi.Api.class);
    }

    @Test
    public void createPrivateGroup() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.createPrivateGroup(1, new ReqCreateTopic()).execute())).isFalse();
    }

    @Test
    public void modifyGroupName() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.modifyGroupName(1, 1, new ReqModifyTopicName()).execute())).isFalse();
    }

    @Test
    public void modifyGroupDescription() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.modifyGroupDescription(1, 1, new ReqModifyTopicDescription()).execute())).isFalse();
    }

    @Test
    public void deleteGroup() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.deleteGroup(1, 1).execute())).isFalse();
    }

    @Test
    public void leaveGroup() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.leaveGroup(1, new ReqTeam(1)).execute())).isFalse();
    }

    @Test
    public void inviteGroup() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.inviteGroup(1, new ReqInviteTopicUsers(new ArrayList<Long>(), 1)).execute())).isFalse();
    }

    @Test
    public void modifyReadOnly() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("isAnnouncement", true);
        assertThat(ValidationUtil.isDeprecated(api.modifyReadOnly(1, 1,map).execute())).isFalse();

    }
}