package com.tosslab.jandi.app.network.client.publictopic.messages;

import com.tosslab.jandi.app.network.JandiRestClient;
import com.tosslab.jandi.app.network.JandiRestClient_;
import com.tosslab.jandi.app.network.JandiV2HttpAuthentication;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqSendMessage;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.shadows.ShadowLog;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(RobolectricGradleTestRunner.class)
public class ChannelMessageApiClientTest {


    private JandiRestClient jandiRestClient_;
    private ChannelMessageApiClient channelMessageApiClient;
    private ResLeftSideMenu sideMenu;

    @Before
    public void setUp() throws Exception {

        jandiRestClient_ = new JandiRestClient_(Robolectric.application);
        channelMessageApiClient = new ChannelMessageApiClient_(Robolectric.application);
        ResAccessToken accessToken = getAccessToken();

        jandiRestClient_.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));
        channelMessageApiClient.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));

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
    public void testSendPublicTopicMessage() throws Exception {

        ResLeftSideMenu.Channel entity = null;
        for (ResLeftSideMenu.Entity entity1 : sideMenu.entities) {
            if (entity1 instanceof ResLeftSideMenu.Channel) {
                ResLeftSideMenu.Channel channel = (ResLeftSideMenu.Channel) entity1;
                entity = channel;
                break;
            }
        }
        ;

        ReqSendMessage message = new ReqSendMessage();
        message.content = "hahaha. test";
        message.teamId = sideMenu.team.id;
        message.type = entity.type;

        ResCommon resCommon = channelMessageApiClient.sendPublicTopicMessage(message, entity.id);

        assertThat(resCommon, is(notNullValue()));

    }

    @Test
    public void testGetPublicTopicMessages() throws Exception {

    }

    @Test
    public void testGetPublicTopicMessagesUpdated() throws Exception {

    }

    @Test
    public void testModifyPublicTopicMessage() throws Exception {

    }

    @Test
    public void testDeletePublicTopicMessage() throws Exception {

    }
}