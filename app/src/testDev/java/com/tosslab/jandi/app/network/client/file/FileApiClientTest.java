package com.tosslab.jandi.app.network.client.file;

import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.shadows.ShadowLog;

@RunWith(RobolectricGradleTestRunner.class)
public class FileApiClientTest {

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


    @Test
    public void testUploadFile() throws Exception {

    }

}