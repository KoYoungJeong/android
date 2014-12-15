package com.tosslab.jandi.app.network.client.messages.comments;

import com.tosslab.jandi.app.network.JandiRestClient;
import com.tosslab.jandi.app.network.JandiRestClient_;
import com.tosslab.jandi.app.network.spring.JandiV2HttpAuthentication;
import com.tosslab.jandi.app.network.client.messages.MessagesApiClient;
import com.tosslab.jandi.app.network.client.messages.MessagesApiClient_;
import com.tosslab.jandi.app.network.client.publictopic.messages.ChannelMessageApiClient;
import com.tosslab.jandi.app.network.client.publictopic.messages.ChannelMessageApiClient_;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqSendComment;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.shadows.ShadowLog;
import org.springframework.web.client.HttpStatusCodeException;

import java.sql.Timestamp;

import static junit.framework.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class CommentsApiClientTest {

    private JandiRestClient jandiRestClient_;
    private MessagesApiClient messagesApiClient;
    private CommentsApiClient commentsApiClient;
    private ResLeftSideMenu sideMenu;
    private ChannelMessageApiClient channelMessageApiClient;


    @Before
    public void setUp() throws Exception {

        jandiRestClient_ = new JandiRestClient_(Robolectric.application);
        messagesApiClient = new MessagesApiClient_(Robolectric.application);
        commentsApiClient = new CommentsApiClient_(Robolectric.application);
        channelMessageApiClient = new ChannelMessageApiClient_(Robolectric.application);

        ResAccessToken accessToken = getAccessToken();

        jandiRestClient_.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));
        messagesApiClient.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));
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
        ResMessages publicTopicMessages = channelMessageApiClient.getPublicTopicMessages(sideMenu.team.id, defaultChannel.id, -1, 20);

        ResMessages.FileMessage fileMessage = null;
        for (ResMessages.Link message : publicTopicMessages.messages) {
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
    public void testSendMessageComment() throws Exception {

        ResLeftSideMenu.Channel defaultChannel = getDefaultChannel();

        ResMessages.FileMessage myFileMessage = getMyFileMessage(defaultChannel);

        ReqSendComment reqSendComment = new ReqSendComment();
        reqSendComment.teamId = sideMenu.team.id;
        reqSendComment.comment = "create_" + new Timestamp(System.currentTimeMillis());
        try {
            ResCommon resCommon = commentsApiClient.sendMessageComment(reqSendComment, myFileMessage.id);
        } catch (HttpStatusCodeException e) {
            fail(e.getResponseBodyAsString());
        }

    }

    @Test
    public void testModifyMessageComment() throws Exception {
        ResLeftSideMenu.Channel defaultChannel = getDefaultChannel();

        ResMessages.FileMessage myFileMessage = getMyFileMessage(defaultChannel);

        ResFileDetail fileDetail = messagesApiClient.getFileDetail(sideMenu.team.id, myFileMessage.id);

        ResMessages.CommentMessage textMessage = getMyCommentMessage(fileDetail);

        ReqSendComment reqSendComment = new ReqSendComment();
        reqSendComment.teamId = sideMenu.team.id;
        reqSendComment.comment = "mod_" + new Timestamp(System.currentTimeMillis());
        ResCommon resCommon = null;
        try {
            resCommon = commentsApiClient.modifyMessageComment(reqSendComment, textMessage.feedbackId, textMessage.id);
        } catch (HttpStatusCodeException e) {
            fail(e.getResponseBodyAsString());
        }

        assertThat(resCommon, is(notNullValue()));

    }

    @Test
    public void testDeleteMessageComment() throws Exception {
        ResLeftSideMenu.Channel defaultChannel = getDefaultChannel();

        ResMessages.FileMessage myFileMessage = getMyFileMessage(defaultChannel);

        ResFileDetail fileDetail = messagesApiClient.getFileDetail(sideMenu.team.id, myFileMessage.id);

        ResMessages.CommentMessage textMessage = getMyCommentMessage(fileDetail);

        commentsApiClient.deleteMessageComment(sideMenu.team.id, textMessage.feedbackId, textMessage.id);
    }

}