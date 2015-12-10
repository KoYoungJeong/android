package com.tosslab.jandi.app.ui.carousel;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.carousel.model.CarouselViewerModel_;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import setup.BaseInitUtil;

@RunWith(AndroidJUnit4.class)
public class CarouselViewerActivityTest {

    @Rule
    public ActivityTestRule<CarouselViewerActivity_> rule = new ActivityTestRule<>(CarouselViewerActivity_.class, false, false);
    private int latestFileId;
    private int teamId;
    private int roomId;
    private CarouselViewerActivity activity;

    @Before
    public void setUp() throws Exception {

        BaseInitUtil.initData();

        teamId = AccountRepository.getRepository().getSelectedTeamId();
        roomId = EntityManager.getInstance().getDefaultTopicId();
        latestFileId = getLatestFileId();

        Intent startIntent = new Intent();
        startIntent.putExtra("roomId", roomId);
        startIntent.putExtra("startLinkId", latestFileId);
        rule.launchActivity(startIntent);
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        activity = rule.getActivity();
    }

    private int getLatestFileId() {
        ReqSearchFile reqSearchFile = new ReqSearchFile();
        reqSearchFile.searchType = ReqSearchFile.SEARCH_TYPE_FILE;
        reqSearchFile.fileType = "image";
        reqSearchFile.writerId = "all";
        reqSearchFile.keyword = "";
        reqSearchFile.listCount = 1;
        reqSearchFile.sharedEntityId = roomId;
        reqSearchFile.startMessageId = -1;
        reqSearchFile.teamId = teamId;
        return RequestApiManager.getInstance().searchFileByMainRest(reqSearchFile).firstIdOfReceivedList;
    }

    private void getCarousel() {
        List<ResMessages.FileMessage> fileMessages = CarouselViewerModel_.getInstance_(JandiApplication.getContext())
                .searchInitFileList(teamId, roomId, latestFileId);
    }


    @After
    public void tearDown() throws Exception {
        BaseInitUtil.clear();
    }

    @Test
    public void testAddFileInfos() throws Exception {

        activity.addFileInfos(0, new ArrayList<>());
    }

    @Test
    public void testSetInitFail() throws Exception {

    }

    @Test
    public void testMovePosition() throws Exception {

    }

    @Test
    public void testSetActionbarTitle() throws Exception {

    }

    @Test
    public void testSetFileWriterName() throws Exception {

    }

    @Test
    public void testSetFileCreateTime() throws Exception {

    }

    @Test
    public void testMoveToFileDatail() throws Exception {

    }

    @Test
    public void testOnRequestPermissionsResult() throws Exception {

    }

    @Test
    public void testShowFailToast() throws Exception {

    }

    @Test
    public void testOnMenuOpened() throws Exception {

    }

    @Test
    public void testOnSwipeExit() throws Exception {

    }
}