package com.tosslab.jandi.app.network.client.publictopic.messages;

import com.tosslab.jandi.app.network.JandiRestClient;
import com.tosslab.jandi.app.network.JandiRestClient_;
import com.tosslab.jandi.app.network.spring.JandiV2HttpAuthentication;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessage;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.shadows.ShadowLog;
import org.springframework.web.client.HttpStatusCodeException;

import java.sql.Timestamp;

import static junit.framework.Assert.fail;
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


    private ResLeftSideMenu.Channel getChannel() {
        ResLeftSideMenu.Channel entity = null;
        for (ResLeftSideMenu.Entity entity1 : sideMenu.joinEntities) {
            if (entity1 instanceof ResLeftSideMenu.Channel) {
                ResLeftSideMenu.Channel channel = (ResLeftSideMenu.Channel) entity1;
                entity = channel;
                break;
            }
        }
        return entity;
    }

    @Test
    public void testSendPublicTopicMessage() throws Exception {

        ResLeftSideMenu.Channel entity = getChannel();

        ReqSendMessage message = new ReqSendMessage();
        message.content = "hahaha. test";
        message.teamId = sideMenu.team.id;
        message.type = entity.type;

        ResCommon resCommon = channelMessageApiClient.sendPublicTopicMessage(message, entity.id);

        assertThat(resCommon, is(notNullValue()));

    }

    @Test
    public void testGetPublicTopicMessages() throws Exception {

        ResLeftSideMenu.Channel channel = getChannel();

        ResMessages publicTopicMessages = null;
        try {
            publicTopicMessages = channelMessageApiClient.getPublicTopicMessages(sideMenu.team.id, channel.id, 33391, 20);
        } catch (HttpStatusCodeException e) {
            fail(e.getResponseBodyAsString());
        }

        assertThat(publicTopicMessages, is(notNullValue()));

        System.out.println(publicTopicMessages);


    }

    @Test
    public void testGetPublicTopicMessagesUpdated() throws Exception {

        ResLeftSideMenu.Channel channel = getChannel();

        ResUpdateMessages publicTopicMessages = null;
        try {
            publicTopicMessages = channelMessageApiClient.getPublicTopicUpdatedMessages(sideMenu.team.id, channel.id, 33391);
        } catch (HttpStatusCodeException e) {
            fail(e.getResponseBodyAsString());
        }

        assertThat(publicTopicMessages, is(notNullValue()));

        System.out.println(publicTopicMessages);

    }

    @Test
    public void testModifyPublicTopicMessage() throws Exception {

        ResLeftSideMenu.Channel channel = getChannel();

        ResUpdateMessages publicTopicMessages = channelMessageApiClient.getPublicTopicUpdatedMessages(sideMenu.team.id, channel.id, 33391);

        ResMessages.Link myMessage = null;

        for (ResMessages.Link message : publicTopicMessages.updateInfo.messages) {
            if (message.message != null && message.message.writer.id == sideMenu.user.id) {
                myMessage = message;
                break;
            }
        }

        ReqModifyMessage message = new ReqModifyMessage();
        message.teamId = 279;
        message.content = "zzzz" + new Timestamp(System.currentTimeMillis());
        ResCommon resCommon = null;
        try {
            resCommon = channelMessageApiClient.modifyPublicTopicMessage(message, channel.id, myMessage.messageId);
        } catch (HttpStatusCodeException e) {
            fail(e.getResponseBodyAsString());
        }

        assertThat(resCommon, is(notNullValue()));


    }

    @Test
    public void testDeletePublicTopicMessage() throws Exception {

        ResLeftSideMenu.Channel channel = getChannel();

        ResUpdateMessages publicTopicMessages = channelMessageApiClient.getPublicTopicUpdatedMessages(sideMenu.team.id, channel.id, 33391);

        ResMessages.Link myMessage = null;

        for (ResMessages.Link message : publicTopicMessages.updateInfo.messages) {
            if (message.message != null && message.message.writer.id == sideMenu.user.id) {
                myMessage = message;
                break;
            }
        }

        System.out.printf("%s : %s\n", myMessage.message.writer.name, ((ResMessages.TextMessage) myMessage.message).content.body);

        ResCommon resCommon = null;
        try {
            resCommon = channelMessageApiClient.deletePublicTopicMessage(sideMenu.team.id, channel.id, myMessage.messageId);
            // TODO Fail 발생함...확인해봐야함
        } catch (HttpStatusCodeException e) {
            fail(e.getResponseBodyAsString());
        }

        assertThat(resCommon, is(notNullValue()));

        // 다음 테스트를 위해 메세지 전송
        testSendPublicTopicMessage();


    }
}