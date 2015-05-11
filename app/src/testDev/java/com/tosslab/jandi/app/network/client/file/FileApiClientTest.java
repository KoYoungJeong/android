package com.tosslab.jandi.app.network.client.file;

import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.client.JandiRestClient_;
import com.tosslab.jandi.app.network.client.publictopic.messages.ChannelMessageApiClient;
import com.tosslab.jandi.app.network.client.publictopic.messages.ChannelMessageApiClient_;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.spring.JandiV2HttpAuthentication;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.shadows.ShadowLog;
import org.springframework.web.client.HttpStatusCodeException;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(RobolectricGradleTestRunner.class)
public class FileApiClientTest {


    private JandiRestClient jandiRestClient_;
    private FileApiClient fileApiClient;
    private ResLeftSideMenu sideMenu;
    private ChannelMessageApiClient channelMessageApiClient;


    @Before
    public void setUp() throws Exception {

        jandiRestClient_ = new JandiRestClient_(Robolectric.application);
        fileApiClient = new FileApiClient_(Robolectric.application);
        channelMessageApiClient = new ChannelMessageApiClient_(Robolectric.application);

        ResAccessToken accessToken = getAccessToken();

        jandiRestClient_.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));
        fileApiClient.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));
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

        ResAccessToken accessToken = jandiRestClient_.getAccessToken(ReqAccessToken.createPasswordReqToken("jihoonk@tosslab.com", "1234"));
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

    private ResMessages.FileMessage getMyFileMessage(ResLeftSideMenu.Channel defaultChannel) {
        ResMessages publicTopicMessages = channelMessageApiClient.getPublicTopicMessages(sideMenu.team.id, defaultChannel.id, -1, requestCount);

        ResMessages.FileMessage fileMessage = null;
        for (ResMessages.Link message : publicTopicMessages.records) {
            if (message.message instanceof ResMessages.FileMessage && message.message.writerId == sideMenu.user.id) {
                fileMessage = (ResMessages.FileMessage) message.message;
                break;
            }
        }
        return fileMessage;
    }


    @Test
    public void testUploadFile() throws Exception {

    }

    @Ignore
    @Test
    public void testDeleteFile() throws Exception {

        ResLeftSideMenu.Channel defaultChannel = getDefaultChannel();

        ResMessages.FileMessage myFileMessage = getMyFileMessage(defaultChannel);

        ResCommon resCommon = null;
        try {
            resCommon = fileApiClient.deleteFile(sideMenu.team.id, myFileMessage.id);
        } catch (HttpStatusCodeException e) {
            fail(e.getResponseBodyAsString());
        }

        assertThat(resCommon, is(notNullValue()));


    }
}