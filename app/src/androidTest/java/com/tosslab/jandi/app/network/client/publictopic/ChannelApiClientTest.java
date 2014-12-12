package com.tosslab.jandi.app.network.client.publictopic;

import com.tosslab.jandi.app.network.JandiRestClient;
import com.tosslab.jandi.app.network.JandiRestClient_;
import com.tosslab.jandi.app.network.JandiV2HttpAuthentication;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqDeleteTopic;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.shadows.ShadowLog;
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

        jandiRestClient_ = new JandiRestClient_(Robolectric.application);
        channelApiClient = new ChannelApiClient_(Robolectric.application);
        ResAccessToken accessToken = getAccessToken();

        jandiRestClient_.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));
        channelApiClient.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));

        sideMenu = getSideMenu();

        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);

        System.setProperty("robolectric.logging", "stdout");
        ShadowLog.stream = System.out;

    }

    private ResLeftSideMenu getSideMenu() {
        ResLeftSideMenu infosForSideMenu = jandiRestClient_.getInfosForSideMenu(279);

        return infosForSideMenu;
    }

    private ResAccessToken getAccessToken() {

        jandiRestClient_.setHeader("Content-Type", "application/json");

        ResAccessToken accessToken = jandiRestClient_.getAccessToken(ReqAccessToken.createPasswordReqToken("mk@tosslab.com", "1234"));
        System.out.println("========= Get Access Token =========");
        return accessToken;
    }

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

        ReqDeleteTopic reqDeleteTopic = new ReqDeleteTopic();
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

        ReqDeleteTopic reqDeleteTopic = new ReqDeleteTopic();
        reqDeleteTopic.teamId = 279;
        ResCommon resCommon = channelApiClient.leaveTopic(myChannelId, reqDeleteTopic);

        assertNotNull("Leave Fail", resCommon);

        ResCommon resCommon1 = channelApiClient.joinTopic(myChannelId, reqDeleteTopic);
        assertNotNull("Join Fail", resCommon1);
    }

}