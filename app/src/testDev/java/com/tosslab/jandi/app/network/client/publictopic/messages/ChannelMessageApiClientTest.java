package com.tosslab.jandi.app.network.client.publictopic.messages;

import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessageV3;
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

import java.sql.Timestamp;

import retrofit.RetrofitError;

import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(RobolectricGradleTestRunner.class)
public class ChannelMessageApiClientTest {


    private ResLeftSideMenu sideMenu;

    @Before
    public void setUp() throws Exception {

        sideMenu = getSideMenu();
        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);
        System.setProperty("robolectric.logging", "stdout");
        ShadowLog.stream = System.out;

    }

    private ResLeftSideMenu getSideMenu() {
        ResLeftSideMenu infosForSideMenu = RequestApiManager.getInstance().getInfosForSideMenuByMainRest(279);

        return infosForSideMenu;
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

        ReqSendMessageV3 reqSendMessageV3 = new ReqSendMessageV3("HAHAHA. TEST", null);

        ResCommon resCommon = RequestApiManager.getInstance().sendPublicTopicMessageByChannelMessageApi(entity.id, sideMenu.team.id, reqSendMessageV3);

        assertThat(resCommon, is(notNullValue()));

    }

    @Test
    public void testGetPublicTopicMessages() throws Exception {

        ResLeftSideMenu.Channel channel = getChannel();

        ResMessages publicTopicMessages = null;
        try {
            publicTopicMessages = RequestApiManager.getInstance().getPublicTopicMessagesByChannelMessageApi(sideMenu.team.id, channel.id, 33391, 20);
        } catch (RetrofitError e) {
            fail(e.getResponse().getBody().toString());
        }

        assertThat(publicTopicMessages, is(notNullValue()));

        System.out.println(publicTopicMessages);


    }

    @Test
    public void testGetPublicTopicMessagesUpdated() throws Exception {

        ResLeftSideMenu.Channel channel = getChannel();

        ResUpdateMessages publicTopicMessages = null;
        try {
            publicTopicMessages = RequestApiManager.getInstance().getPublicTopicUpdatedMessagesByChannelMessageApi(sideMenu.team.id, channel.id, 33391);
        } catch (RetrofitError e) {
            fail(e.getResponse().getBody().toString());
        }

        assertThat(publicTopicMessages, is(notNullValue()));

        System.out.println(publicTopicMessages);

    }

    @Test
    public void testModifyPublicTopicMessage() throws Exception {

        ResLeftSideMenu.Channel channel = getChannel();

        ResUpdateMessages publicTopicMessages = RequestApiManager.getInstance().getPublicTopicUpdatedMessagesByChannelMessageApi(sideMenu.team.id, channel.id, 33391);

        ResMessages.Link myMessage = null;

        for (ResMessages.Link message : publicTopicMessages.updateInfo.messages) {
            if (message.message != null && message.message.writerId == sideMenu.user.id) {
                myMessage = message;
                break;
            }
        }

        ReqModifyMessage message = new ReqModifyMessage();
        message.teamId = 279;
        message.content = "zzzz" + new Timestamp(System.currentTimeMillis());
        ResCommon resCommon = null;
        try {
            resCommon = RequestApiManager.getInstance().modifyPublicTopicMessageByChannelMessageApi(message, channel.id, myMessage.messageId);
        } catch (RetrofitError e) {
            fail(e.getResponse().getBody().toString());
        }

        assertThat(resCommon, is(notNullValue()));


    }

    @Test
    public void testDeletePublicTopicMessage() throws Exception {

        ResLeftSideMenu.Channel channel = getChannel();

        ResUpdateMessages publicTopicMessages = RequestApiManager.getInstance().getPublicTopicUpdatedMessagesByChannelMessageApi(sideMenu.team.id, channel.id, 33391);

        ResMessages.Link myMessage = null;

        for (ResMessages.Link message : publicTopicMessages.updateInfo.messages) {
            if (message.message != null && message.message.writerId == sideMenu.user.id) {
                myMessage = message;
                break;
            }
        }

        ResCommon resCommon = null;
        try {
            resCommon = RequestApiManager.getInstance().deletePublicTopicMessageByChannelMessageApi(sideMenu.team.id, channel.id, myMessage.messageId);
            // TODO Fail 발생함...확인해봐야함
        } catch (RetrofitError e) {
            fail(e.getResponse().getBody().toString());
        }

        assertThat(resCommon, is(notNullValue()));

        // 다음 테스트를 위해 메세지 전송
        testSendPublicTopicMessage();


    }
}