package com.tosslab.jandi.app.network.client.direct.message;

import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.client.JandiRestClient_;
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
import java.util.List;

import static junit.framework.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class DirectMessageApiClientTest {

    private JandiRestClient jandiRestClient_;
    private ResLeftSideMenu sideMenu;
    private DirectMessageApiClient directMessageApiClient;


    @Before
    public void setUp() throws Exception {

        jandiRestClient_ = new JandiRestClient_(Robolectric.application);
        directMessageApiClient = new DirectMessageApiClient_(Robolectric.application);
        ResAccessToken accessToken = getAccessToken();

        jandiRestClient_.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));
        directMessageApiClient.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));

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

    private ResLeftSideMenu.User getUser() {
        ResLeftSideMenu.User user = null;
        for (ResLeftSideMenu.Entity joinEntity : sideMenu.entities) {
            if (joinEntity instanceof ResLeftSideMenu.User) {
                user = (ResLeftSideMenu.User) joinEntity;
                break;
            }
        }
        return user;
    }

    private ResMessages.TextMessage getMyTextMessage(ResMessages directMessages) {
        List<ResMessages.Link> messages = directMessages.messages;
        ResMessages.TextMessage textMessage = null;
        for (ResMessages.Link message : messages) {
            if (message.message instanceof ResMessages.TextMessage && message.message.writerId == sideMenu.user.id) {
                textMessage = (ResMessages.TextMessage) message.message;
                break;
            }
        }
        return textMessage;
    }

    @Test
    public void testGetDirectMessages() throws Exception {


        ResLeftSideMenu.User user = getUser();

        ResMessages directMessages = null;
        try {
            directMessages = directMessageApiClient.getDirectMessages(sideMenu.team.id, user.id, -1, 20);
        } catch (HttpStatusCodeException e) {
            fail(e.getResponseBodyAsString());

        }

        assertThat(directMessages, is(notNullValue()));

        System.out.println(directMessages);

    }

    @Test
    public void testGetDirectMessagesUpdated() throws Exception {

        ResLeftSideMenu.User user = getUser();

        ResUpdateMessages directMessagesUpdated = null;
        try {
            directMessagesUpdated = directMessageApiClient.getDirectMessagesUpdated(sideMenu.team.id, user.id, System.currentTimeMillis());
        } catch (HttpStatusCodeException e) {
            fail(e.getResponseBodyAsString());
        }


        assertThat(directMessagesUpdated, is(notNullValue()));


    }

    @Test
    public void testSendDirectMessage() throws Exception {
        ResLeftSideMenu.User user = getUser();
        ReqSendMessage reqSendMessage = new ReqSendMessage();
        reqSendMessage.type = "string";
        reqSendMessage.teamId = sideMenu.team.id;
        reqSendMessage.content = "create_" + new Timestamp(System.currentTimeMillis());

        ResCommon resCommon = null;
        try {
            resCommon = directMessageApiClient.sendDirectMessage(reqSendMessage, user.id);
        } catch (HttpStatusCodeException e) {
            fail(e.getResponseBodyAsString());
        }

        assertThat(resCommon, is(notNullValue()));
    }

    @Test
    public void testModifyDirectMessage() throws Exception {

        ResLeftSideMenu.User user = getUser();

        ResMessages directMessages = directMessageApiClient.getDirectMessages(sideMenu.team.id, user.id, -1, 100);

        ResMessages.TextMessage textMessage = getMyTextMessage(directMessages);

        ResCommon resCommon = null;
        try {
            ReqModifyMessage message = new ReqModifyMessage();
            message.teamId = sideMenu.team.id;
            message.content = "mod_" + new Timestamp(System.currentTimeMillis());

            resCommon = directMessageApiClient.modifyDirectMessage(message, user.id, textMessage.id);
        } catch (HttpStatusCodeException e) {
            fail(e.getResponseBodyAsString());
        }

        assertThat(resCommon, is(notNullValue()));
    }

    @Test
    public void testDeleteDirectMessage() throws Exception {

        ResLeftSideMenu.User user = getUser();

        ResMessages directMessages = directMessageApiClient.getDirectMessages(sideMenu.team.id, user.id, -1, 100);

        ResMessages.TextMessage textMessage = getMyTextMessage(directMessages);

        ResCommon resCommon = null;
        try {
            resCommon = directMessageApiClient.deleteDirectMessage(sideMenu.team.id, user.id, textMessage.id);
        } catch (HttpStatusCodeException e) {
            fail(e.getResponseBodyAsString());
        }

        assertThat(resCommon, is(notNullValue()));
    }
}