package com.tosslab.jandi.app.network.client.privatetopic.messages;

import com.tosslab.jandi.app.local.database.JandiDatabaseOpenHelper;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.client.JandiRestClient_;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessage;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;
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

        BaseInitUtil.initData(Robolectric.application);

        jandiRestClient_ = new JandiRestClient_(Robolectric.application);
        groupMessageApiClient = new GroupMessageApiClient_(Robolectric.application);

        jandiRestClient_.setAuthentication(TokenUtil.getRequestAuthentication(Robolectric.application));
        groupMessageApiClient.setAuthentication(TokenUtil.getRequestAuthentication(Robolectric.application));

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
        ResMessages groupMessages = groupMessageApiClient.getGroupMessages(sideMenu.team.id, privateTopic.id);
        ResMessages.TextMessage textMessage = null;
        for (ResMessages.Link message : groupMessages.records) {
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

        ResMessages groupMessages = groupMessageApiClient.getGroupMessages(sideMenu.team.id, privateTopic.id);

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

    @Ignore
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

    @Ignore
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