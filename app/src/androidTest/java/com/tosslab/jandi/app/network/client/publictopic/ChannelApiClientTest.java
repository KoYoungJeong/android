package com.tosslab.jandi.app.network.client.publictopic;

import com.tosslab.jandi.app.local.database.JandiDatabaseOpenHelper;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.client.JandiRestClient_;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqDeleteTopic;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.springframework.web.client.HttpStatusCodeException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@RunWith(RobolectricGradleTestRunner.class)
public class ChannelApiClientTest {

    private JandiRestClient jandiRestClient_;
    private ChannelApiClient channelApiClient;
    private ResLeftSideMenu sideMenu;

    @Before
    public void setUp() throws Exception {

        BaseInitUtil.initData(Robolectric.application);

        jandiRestClient_ = new JandiRestClient_(Robolectric.application);
        channelApiClient = new ChannelApiClient_(Robolectric.application);

        jandiRestClient_.setAuthentication(TokenUtil.getRequestAuthentication(Robolectric.application));
        channelApiClient.setAuthentication(TokenUtil.getRequestAuthentication(Robolectric.application));

        sideMenu = getSideMenu();


    }

    @After
    public void tearDown() throws Exception {
        JandiDatabaseOpenHelper.getInstance(Robolectric.application).getWritableDatabase().close();
    }


    private ResLeftSideMenu getSideMenu() {

        ResLeftSideMenu infosForSideMenu = jandiRestClient_.getInfosForSideMenu(JandiAccountDatabaseManager.getInstance(Robolectric.application).getUserTeams().get(0).getTeamId());

        return infosForSideMenu;
    }

    private ResAccessToken getAccessToken() {

        jandiRestClient_.setHeader("Content-Type", "application/json");

        ResAccessToken accessToken = jandiRestClient_.getAccessToken(ReqAccessToken.createPasswordReqToken(BaseInitUtil.TEST_ID, BaseInitUtil.TEST_PASSWORD));
        System.out.println("========= Get Access Token =========");
        return accessToken;
    }

    @Ignore
    @Test
    public void testCreateTopic() throws Exception {


        ReqCreateTopic reaCreateTopic = new ReqCreateTopic();
        reaCreateTopic.name = "test123123";
        reaCreateTopic.teamId = 279;

        ResCommon result = null;
        try {
            result = channelApiClient.createChannel(reaCreateTopic);
        } catch (HttpStatusCodeException e) {
            System.out.println(e.getResponseBodyAsString());
            fail();
        }

        System.out.println(result);

    }

    @Ignore
    @Test
    public void testModifyChannelName() throws Exception {


        ReqCreateTopic reaCreateTopic = new ReqCreateTopic();
        reaCreateTopic.name = "test12312443";
        reaCreateTopic.teamId = 279;

        ResCommon resCommon = null;
        try {
            resCommon = channelApiClient.modifyPublicTopicName(reaCreateTopic, 6808);
        } catch (HttpStatusCodeException e) {
            System.out.println(e.getResponseBodyAsString());
            fail();
        }

        assertNotNull(resCommon);
    }

    @Ignore
    @Test
    public void testDeleteChannel() throws Exception {

        ResLeftSideMenu infosForSideMenu = jandiRestClient_.getInfosForSideMenu(279);

        int myChannelId = 0;
        for (ResLeftSideMenu.Entity entity : infosForSideMenu.entities) {

            if (entity instanceof ResLeftSideMenu.Channel) {
                ResLeftSideMenu.Channel channel = (ResLeftSideMenu.Channel) entity;

                if (channel.ch_creatorId == 285) {
                    myChannelId = channel.id;
                    break;
                }
            }
        }

        if (myChannelId == 0) {
            fail();
        }

        ReqDeleteTopic reqDeleteTopic = new ReqDeleteTopic(279);
        reqDeleteTopic.teamId = 279;

        ResCommon resCommon = null;
        try {
            resCommon = channelApiClient.deleteTopic(myChannelId, reqDeleteTopic);
        } catch (HttpStatusCodeException e) {
            System.out.println(e.getResponseBodyAsString());
            fail();
        }

        assertNotNull(resCommon);

    }

    @Ignore
    @Test
    public void testLeave_JoinTopic() throws Exception {


        ResLeftSideMenu infosForSideMenu = jandiRestClient_.getInfosForSideMenu(279);

        int myChannelId = 0;
        for (ResLeftSideMenu.Entity entity : infosForSideMenu.entities) {

            if (entity instanceof ResLeftSideMenu.Channel) {
                ResLeftSideMenu.Channel channel = (ResLeftSideMenu.Channel) entity;

                if (channel.ch_creatorId != infosForSideMenu.user.id) {
                    myChannelId = channel.id;
                    break;
                }
            }
        }

        ReqDeleteTopic reqDeleteTopic = new ReqDeleteTopic(279);
        reqDeleteTopic.teamId = 279;
        ResCommon resCommon = channelApiClient.leaveTopic(myChannelId, reqDeleteTopic);

        assertNotNull("Leave Fail", resCommon);

        ResCommon resCommon1 = channelApiClient.joinTopic(myChannelId, reqDeleteTopic);
        assertNotNull("Join Fail", resCommon1);
    }

}