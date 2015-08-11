package com.tosslab.jandi.app.network.client.file;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResSearchFile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.JandiRobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.httpclient.FakeHttp;

import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(JandiRobolectricGradleTestRunner.class)
public class FileApiClientTest {

    private ResLeftSideMenu sideMenu;

    @Before
    public void setUp() throws Exception {


        BaseInitUtil.initData(RuntimeEnvironment.application);

        int teamId = AccountRepository.getRepository().getAccountTeams().get(0).getTeamId();
        AccountRepository.getRepository().updateSelectedTeamInfo(teamId);

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
        int teamId = AccountRepository.getRepository().getSelectedTeamInfo().getTeamId();
        ResLeftSideMenu infosForSideMenu = RequestApiManager.getInstance()
                .getInfosForSideMenuByMainRest(teamId);

        return infosForSideMenu;
    }

    private ResSearchFile getFileList() {
        ReqSearchFile reqSearchFile = new ReqSearchFile();
        reqSearchFile.searchType = ReqSearchFile.SEARCH_TYPE_FILE;
        reqSearchFile.listCount = ReqSearchFile.MAX;

        reqSearchFile.fileType = ReqSearchFile.FILE_TYPE_IMAGE;
        reqSearchFile.writerId = "all";
        reqSearchFile.sharedEntityId = sideMenu.team.t_defaultChannelId;

        reqSearchFile.startMessageId = -1;
        reqSearchFile.keyword = "";
        reqSearchFile.teamId = AccountRepository.getRepository().getSelectedTeamInfo().getTeamId();

        return RequestApiManager.getInstance().searchFileByMainRest(reqSearchFile);
    }

    private int getRoomId() {
        return RequestApiManager.getInstance().getPublicTopicMessagesByChannelMessageApi(sideMenu
                .team.id, sideMenu.team.t_defaultChannelId).entityId;
    }

    @Test
    public void testSearchInitImageFile() throws Exception {
        int messageId = getFileList().files.get(1).id;
        int roomId = getRoomId();

        List<ResMessages.FileMessage> fileMessages = RequestApiManager.getInstance()
                .searchInitImageFileByFileApi(sideMenu.team.id, roomId, messageId, 20);

        assertThat(fileMessages, is(notNullValue()));
        assertTrue(fileMessages.size() > 0);
        assertTrue(fileMessages.get(fileMessages.size() - 1).id > messageId);
    }

    @Test
    public void testSearchOldImageFile() throws Exception {
        int messageId = getFileList().files.get(1).id;
        int roomId = getRoomId();

        List<ResMessages.FileMessage> fileMessages = RequestApiManager.getInstance()
                .searchOldImageFileByFileApi(sideMenu.team.id, roomId, messageId, 20);

        assertThat(fileMessages, is(notNullValue()));
        assertTrue(fileMessages.size() > 0);
        assertTrue(fileMessages.get(fileMessages.size() - 1).id < messageId);

    }

    @Test
    public void testSearchNewImageFile() throws Exception {
        int messageId = getFileList().files.get(1).id;
        int roomId = getRoomId();

        List<ResMessages.FileMessage> fileMessages = RequestApiManager.getInstance()
                .searchNewImageFileByFileApi(sideMenu.team.id, roomId, messageId, 20);

        assertThat(fileMessages, is(notNullValue()));
        assertTrue(fileMessages.size() == 1);
        assertTrue(fileMessages.get(fileMessages.size() - 1).id > messageId);

    }

}