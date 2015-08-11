package com.tosslab.jandi.app.network.client.privatetopic.messages;

import com.tosslab.jandi.app.local.database.JandiDatabaseOpenHelper;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessageV3;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.sql.Timestamp;

import retrofit.RetrofitError;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(RobolectricGradleTestRunner.class)
public class GroupMessageApiClientTest {

    private ResLeftSideMenu sideMenu;

    @Before
    public void setUp() throws Exception {

        BaseInitUtil.initData(RuntimeEnvironment.application);

        sideMenu = getSideMenu();

    }

    @After
    public void tearDown() throws Exception {
        JandiDatabaseOpenHelper.getInstance(RuntimeEnvironment.application).getWritableDatabase().close();
    }


    private ResLeftSideMenu getSideMenu() {

        ResLeftSideMenu infosForSideMenu = RequestApiManager.getInstance().
                getInfosForSideMenuByMainRest(AccountRepository.getRepository().getAccountTeams().get(0).getTeamId());

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
        ResMessages groupMessages = RequestApiManager.getInstance().
                getGroupMessagesByGroupMessageApi(sideMenu.team.id, privateTopic.id);
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

        ResMessages groupMessages = RequestApiManager.getInstance().
                getGroupMessagesByGroupMessageApi(sideMenu.team.id, privateTopic.id);

        assertThat(groupMessages, is(notNullValue()));

        System.out.println(groupMessages);

    }

    @Test
    public void testGetGroupMessagesUpdated() throws Exception {
        ResLeftSideMenu.PrivateGroup privateTopic = getPrivateTopic();

        ResUpdateMessages groupMessagesUpdated = RequestApiManager.getInstance().getGroupMessagesUpdatedByGroupMessageApi(sideMenu.team.id, privateTopic.id, 10);

        assertThat(groupMessagesUpdated, is(notNullValue()));

        System.out.println(groupMessagesUpdated);
    }

    @Test
    public void testSendGroupMessage() throws Exception {

        ResLeftSideMenu.PrivateGroup privateTopic = getPrivateTopic();

        ReqSendMessage reqSendMessage = new ReqSendMessage();
        ReqSendMessageV3 reqSendMessageV3 = new ReqSendMessageV3("create_" + new Timestamp(System.currentTimeMillis()), null);
        ResCommon resCommon = RequestApiManager.getInstance().
                sendGroupMessageByGroupMessageApi(privateTopic.id, reqSendMessage.teamId, reqSendMessageV3);

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
        ResCommon resCommon1 = RequestApiManager.getInstance().
                modifyPrivateGroupMessageByGroupMessageApi(reqModifyMessages, privateTopic.id, textMessage.id);

        assertThat(resCommon1, is(notNullValue()));

    }

    @Ignore
    @Test
    public void testDeletePrivateGroupMessage() throws Exception {

        ResLeftSideMenu.PrivateGroup privateTopic = getPrivateTopic();
        ResMessages.TextMessage myTextMessage = getMyTextMessage(privateTopic);

        ResCommon resCommon = null;
        try {
            resCommon = RequestApiManager.getInstance().
                    deletePrivateGroupMessageByGroupMessageApi(sideMenu.team.id, privateTopic.id, myTextMessage.id);
        } catch (RetrofitError e) {
            fail(e.getResponse().getBody().toString());
        }

        assertThat(resCommon, is(notNullValue()));

    }
}