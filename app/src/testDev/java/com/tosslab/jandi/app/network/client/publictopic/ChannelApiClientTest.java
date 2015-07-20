package com.tosslab.jandi.app.network.client.publictopic;

import com.tosslab.jandi.app.local.database.JandiDatabaseOpenHelper;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqDeleteTopic;
import com.tosslab.jandi.app.network.models.ResCommon;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
public class ChannelApiClientTest {

    @Before
    public void setUp() throws Exception {

        BaseInitUtil.initData(Robolectric.application);

    }

    @Test
    public void testDeleteChannel() throws Exception {

        int teamId = AccountRepository.getRepository().getAccountTeams().get(0).getTeamId();

        ReqCreateTopic reqCreateTopic = new ReqCreateTopic();
        reqCreateTopic.teamId = teamId;
        reqCreateTopic.name = "Test Create Public Topic";
        ResCommon channelByChannelApi = RequestApiManager.getInstance().createChannelByChannelApi(reqCreateTopic);

        int createId = channelByChannelApi.id;

        assertTrue(createId > 0);

        ResCommon resCommon = RequestApiManager.getInstance().deleteTopicByChannelApi(createId, new ReqDeleteTopic(teamId));

        assertThat(resCommon, is(notNullValue()));

    }

    @After
    public void tearDown() throws Exception {
        JandiDatabaseOpenHelper.getInstance(Robolectric.application).getWritableDatabase().close();
    }


}