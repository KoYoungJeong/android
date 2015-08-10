package com.tosslab.jandi.app.network.client.messages;

import com.tosslab.jandi.app.local.database.JandiDatabaseOpenHelper;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqShareMessage;
import com.tosslab.jandi.app.network.models.ReqUnshareMessage;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;

import retrofit.RetrofitError;

import static junit.framework.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class MessagesApiClientTest {

    private ResLeftSideMenu sideMenu;

    @Before
    public void setUp() throws Exception {

        BaseInitUtil.initData(Robolectric.application);
        sideMenu = getSideMenu();

    }

    @After
    public void tearDown() throws Exception {
        JandiDatabaseOpenHelper.getInstance(Robolectric.application).getWritableDatabase().close();
    }

    private ResLeftSideMenu getSideMenu() {
        ResLeftSideMenu infosForSideMenu = RequestApiManager.getInstance().
                getInfosForSideMenuByMainRest(AccountRepository.getRepository().getAccountTeams().get(0).getTeamId());

        return infosForSideMenu;
    }


    private ResLeftSideMenu.Channel getDefaultChannel() {
        ResLeftSideMenu.Channel entity = null;
        for (ResLeftSideMenu.Entity entity1 : sideMenu.joinEntities) {
            if (entity1 instanceof ResLeftSideMenu.Channel && entity1.id == sideMenu.team.t_defaultChannelId) {
                ResLeftSideMenu.Channel channel = (ResLeftSideMenu.Channel) entity1;
                entity = channel;
                break;
            }
        }
        return entity;
    }

    private ResLeftSideMenu.Channel getOtherChannel() {
        ResLeftSideMenu.Channel entity = null;
        for (ResLeftSideMenu.Entity entity1 : sideMenu.joinEntities) {
            if (entity1 instanceof ResLeftSideMenu.Channel && (entity1.id != sideMenu.team.t_defaultChannelId)) {
                ResLeftSideMenu.Channel channel = (ResLeftSideMenu.Channel) entity1;
                entity = channel;
                break;
            }
        }
        return entity;
    }

    private ResMessages.FileMessage getMyFileMessage(ResLeftSideMenu.Channel defaultChannel) {
        ResMessages publicTopicMessages = RequestApiManager.getInstance().getPublicTopicMessagesByChannelMessageApi(sideMenu.team.id, defaultChannel.id, -1, 20);

        ResMessages.FileMessage fileMessage = null;
        for (ResMessages.Link message : publicTopicMessages.records) {
            if (message.message instanceof ResMessages.FileMessage && message.message.writerId == sideMenu.user.id) {
                fileMessage = (ResMessages.FileMessage) message.message;
                break;
            }
        }
        return fileMessage;
    }

    private ResMessages.CommentMessage getMyCommentMessage(ResFileDetail fileDetail) {
        ResMessages.CommentMessage textMessage = null;
        for (ResMessages.OriginalMessage messageDetail : fileDetail.messageDetails) {
            if (messageDetail instanceof ResMessages.CommentMessage) {
                ResMessages.CommentMessage tempTextMessage = (ResMessages.CommentMessage) messageDetail;
                if (tempTextMessage.writerId == sideMenu.user.id) {
                    textMessage = tempTextMessage;
                }
                break;
            }
        }
        return textMessage;
    }

    @Test
    public void testGetFileDetail() throws Exception {

        ResLeftSideMenu.Channel defaultChannel = getDefaultChannel();

        ResMessages.FileMessage fileMessage = getMyFileMessage(defaultChannel);

        if (fileMessage == null) {
            return;
        }

        ResFileDetail fileDetail = RequestApiManager.getInstance().getFileDetailByMessagesApiAuth(sideMenu.team.id, fileMessage.id);

        assertThat(fileDetail, is(notNullValue()));

        System.out.println(fileDetail);
    }


    @Test
    public void testShareMessage() throws Exception {
        ResLeftSideMenu.Channel defaultChannel = getDefaultChannel();

        ResMessages.FileMessage myFileMessage = getMyFileMessage(defaultChannel);

        if (myFileMessage == null) {
            return;
        }

        ReqShareMessage reqShareMessage = new ReqShareMessage();
        reqShareMessage.shareEntity = getOtherChannel().id;
        reqShareMessage.teamId = sideMenu.team.id;
        ResCommon resCommon = null;
        try {
            resCommon = RequestApiManager.getInstance().shareMessageByMessagesApiAuth(reqShareMessage, myFileMessage.id);
        } catch (RetrofitError e) {
            fail(e.getResponse().getBody().toString());
        }

        assertThat(resCommon, is(notNullValue()));

        ReqUnshareMessage reqUnshareMessage = new ReqUnshareMessage(sideMenu.team.id, reqShareMessage.shareEntity);
        try {
            resCommon = RequestApiManager.getInstance().unshareMessageByMessagesApiAuth(reqUnshareMessage, myFileMessage.id);
        } catch (RetrofitError e) {
            fail(e.getResponse().getBody().toString());
        }

        assertThat(resCommon, is(notNullValue()));

    }
}