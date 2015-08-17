package com.tosslab.jandi.app.network.client.direct.message;

import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessageV3;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.JandiRobolectricGradleTestRunner;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.httpclient.FakeHttp;

import java.sql.Timestamp;
import java.util.List;

import retrofit.RetrofitError;

import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(JandiRobolectricGradleTestRunner.class)
public class DirectMessageApiClientTest {

    private ResLeftSideMenu sideMenu;

    @Before
    public void setUp() throws Exception {

        sideMenu = getSideMenu();

        FakeHttp.getFakeHttpLayer().interceptHttpRequests(false);

        System.setProperty("robolectric.logging", "stdout");
        ShadowLog.stream = System.out;

    }
    @After
    public void tearDown() throws Exception {
        BaseInitUtil.releaseDatabase();

    }

    private ResLeftSideMenu getSideMenu() {
        ResLeftSideMenu infosForSideMenu = RequestApiManager.getInstance().getInfosForSideMenuByMainRest(279);
        return infosForSideMenu;
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
        List<ResMessages.Link> messages = directMessages.records;
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
            directMessages = RequestApiManager.getInstance().getDirectMessagesByDirectMessageApi(sideMenu.team.id, user.id);
        } catch (RetrofitError e) {
            fail(e.getResponse().getBody().toString());

        }

        assertThat(directMessages, is(notNullValue()));

        System.out.println(directMessages);

    }

    @Test
    public void testGetDirectMessagesUpdated() throws Exception {

        ResLeftSideMenu.User user = getUser();

        ResUpdateMessages directMessagesUpdated = null;
        try {
            directMessagesUpdated = RequestApiManager.getInstance().getDirectMessagesUpdatedByDirectMessageApi(sideMenu.team.id, user.id, 10);
        } catch (RetrofitError e) {
            fail(e.getResponse().getBody().toString());
        }


        assertThat(directMessagesUpdated, is(notNullValue()));


    }

    @Test
    public void testSendDirectMessage() throws Exception {
        ResLeftSideMenu.User user = getUser();
        ReqSendMessageV3 reqSendMessage = new ReqSendMessageV3("create_" + new Timestamp(System.currentTimeMillis()), null);

        ResCommon resCommon = null;
        try {
            resCommon = RequestApiManager.getInstance().sendDirectMessageByDirectMessageApi(user.id, user.teamId, reqSendMessage);
        } catch (RetrofitError e) {
            fail(e.getResponse().getBody().toString());
        }

        assertThat(resCommon, is(notNullValue()));
    }

    @Test
    public void testModifyDirectMessage() throws Exception {

        ResLeftSideMenu.User user = getUser();

        ResMessages directMessages = RequestApiManager.getInstance().getDirectMessagesByDirectMessageApi(sideMenu.team.id, user.id);

        ResMessages.TextMessage textMessage = getMyTextMessage(directMessages);

        ResCommon resCommon = null;
        try {
            ReqModifyMessage message = new ReqModifyMessage();
            message.teamId = sideMenu.team.id;
            message.content = "mod_" + new Timestamp(System.currentTimeMillis());

            resCommon = RequestApiManager.getInstance().modifyDirectMessageByDirectMessageApi(message, user.id, textMessage.id);
        } catch (RetrofitError e) {
            fail(e.getResponse().getBody().toString());
        }

        assertThat(resCommon, is(notNullValue()));
    }

    @Test
    public void testDeleteDirectMessage() throws Exception {

        ResLeftSideMenu.User user = getUser();

        ResMessages directMessages = RequestApiManager.getInstance().getDirectMessagesByDirectMessageApi(sideMenu.team.id, user.id);

        ResMessages.TextMessage textMessage = getMyTextMessage(directMessages);

        ResCommon resCommon = null;
        try {
            resCommon = RequestApiManager.getInstance().deleteDirectMessageByDirectMessageApi(sideMenu.team.id, user.id, textMessage.id);
        } catch (RetrofitError e) {
            fail(e.getResponse().getBody().toString());
        }

        assertThat(resCommon, is(notNullValue()));
    }
}