package com.tosslab.jandi.app.network.client.privatetopic.messages;

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

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(RobolectricGradleTestRunner.class)
public class GroupMessageApiClientTest {

    private JandiRestClient jandiRestClient_;
    private GroupMessageApiClient groupMessageApiClient;
    private ResLeftSideMenu sideMenu;

    @Before
    public void setUp() throws Exception {

        jandiRestClient_ = new JandiRestClient_(Robolectric.application);
        groupMessageApiClient = new GroupMessageApiClient_(Robolectric.application);

        ResAccessToken accessToken = getAccessToken();

        jandiRestClient_.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));
        groupMessageApiClient.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));

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


    private ResLeftSideMenu.PrivateGroup getPrivateTopic() {
        ResLeftSideMenu.PrivateGroup entity = null;
        for (ResLeftSideMenu.Entity entity1 : sideMenu.joinEntities) {
            if (entity1 instanceof ResLeftSideMenu.PrivateGroup) {
                ResLeftSideMenu.PrivateGroup channel = (ResLeftSideMenu.PrivateGroup) entity1;
                entity = channel;
                break;
            }
        }
        return entity;
    }

    private ResMessages.TextMessage getMyTextMessage(ResLeftSideMenu.PrivateGroup privateTopic) {
        ResMessages groupMessages = groupMessageApiClient.getGroupMessages(sideMenu.team.id, privateTopic.id, -1, 20);
        ResMessages.TextMessage textMessage = null;
        for (ResMessages.Link message : groupMessages.messages) {
            if (message.message instanceof ResMessages.TextMessage && message.message.writerId == sideMenu.user.id) {
                textMessage = (ResMessages.TextMessage) message.message;
                break;
            }
        }
        return textMessage;
    }

    @Test
    public void testGetGroupMessages() throws Exception {

        ResLeftSideMenu.PrivateGroup privateTopic = getPrivateTopic();

        ResMessages groupMessages = groupMessageApiClient.getGroupMessages(sideMenu.team.id, privateTopic.id, -1, 20);

        assertThat(groupMessages, is(notNullValue()));

        System.out.println(groupMessages);

    }

    @Test
    public void testGetGroupMessagesUpdated() throws Exception {
        ResLeftSideMenu.PrivateGroup privateTopic = getPrivateTopic();

        ResUpdateMessages groupMessagesUpdated = groupMessageApiClient.getGroupMessagesUpdated(sideMenu.team.id, privateTopic.id, System.currentTimeMillis());

        assertThat(groupMessagesUpdated, is(notNullValue()));

        System.out.println(groupMessagesUpdated);
    }

    @Test
    public void testSendGroupMessage() throws Exception {

        ResLeftSideMenu.PrivateGroup privateTopic = getPrivateTopic();

        ReqSendMessage reqSendMessage = new ReqSendMessage();
        reqSendMessage.teamId = sideMenu.team.id;
        reqSendMessage.type = "string";
        reqSendMessage.content = "create_" + new Timestamp(System.currentTimeMillis());
        ResCommon resCommon = groupMessageApiClient.sendGroupMessage(reqSendMessage, privateTopic.id);

        assertThat(resCommon, is(notNullValue()));

    }

    @Test
    public void testModifyPrivateGroupMessage() throws Exception {
        ResLeftSideMenu.PrivateGroup privateTopic = getPrivateTopic();

        ResMessages.TextMessage textMessage = getMyTextMessage(privateTopic);

        ReqModifyMessage reqModifyMessages = new ReqModifyMessage();
        reqModifyMessages.teamId = sideMenu.team.id;
        reqModifyMessages.content = "mod_" + new Timestamp(System.currentTimeMillis());
        ResCommon resCommon1 = groupMessageApiClient.modifyPrivateGroupMessage(reqModifyMessages, privateTopic.id, textMessage.id);

        assertThat(resCommon1, is(notNullValue()));

    }

    @Test
    public void testDeletePrivateGroupMessage() throws Exception {

        ResLeftSideMenu.PrivateGroup privateTopic = getPrivateTopic();
        ResMessages.TextMessage myTextMessage = getMyTextMessage(privateTopic);

        ResCommon resCommon = null;
        try {
            resCommon = groupMessageApiClient.deletePrivateGroupMessage(sideMenu.team.id, privateTopic.id, myTextMessage.id);
        } catch (HttpStatusCodeException e) {
            fail(e.getResponseBodyAsString());
        }


        assertThat(resCommon, is(notNullValue()));

    }
}