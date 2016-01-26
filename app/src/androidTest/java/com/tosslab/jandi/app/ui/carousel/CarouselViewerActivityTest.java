package com.tosslab.jandi.app.ui.carousel;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.intent.matcher.IntentMatchers;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.carousel.domain.CarouselFileInfo;
import com.tosslab.jandi.app.ui.carousel.model.CarouselViewerModel_;
import com.tosslab.jandi.app.ui.filedetail.FileDetailActivity_;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import setup.BaseInitUtil;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;

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

    private List<CarouselFileInfo> getCarousel() {
        CarouselViewerModel_ carouselViewerModel = CarouselViewerModel_.getInstance_(JandiApplication.getContext());
        List<ResMessages.FileMessage> fileMessages = carouselViewerModel
                .searchInitFileList(teamId, roomId, latestFileId);
        return carouselViewerModel.getImageFileConvert(roomId, fileMessages);
    }


    @After
    public void tearDown() throws Exception {
        BaseInitUtil.clear();
    }

    @Test
    public void testAddFileInfos() throws Throwable {
        int count = activity.carouselViewerAdapter.getCount();
        List<CarouselFileInfo> carousel = getCarousel();

        // When
        rule.runOnUiThread(() -> activity.addFileInfos(0, carousel));

        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        // Then
        int afaterCount = activity.carouselViewerAdapter.getCount();
        assertThat(afaterCount, is(greaterThan(count)));
        assertThat(activity.viewPager.getCurrentItem(), is(equalTo(carousel.size())));
    }

    @Test
    public void testSetInitFail() throws Throwable {

        rule.runOnUiThread(() -> activity.setInitFail());
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertThat(activity.isFinishing(), is(true));
    }

    @Test
    public void testMovePosition() throws Throwable {

        int movePosition = 0;
        rule.runOnUiThread(() -> activity.movePosition(movePosition));
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertThat(activity.viewPager.getCurrentItem(), is(equalTo(movePosition)));
    }

    @Test
    public void testSetActionbarTitle() throws Throwable {
        String fileName = "1111";
        String size = "1kb";
        String xls = "xls";
        rule.runOnUiThread(() -> activity.setActionbarTitle(fileName, size, xls));

        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        onView(withText(fileName))
                .check(matches(isDisplayed()));
        onView(withText(size + ", " + xls))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testSetFileWriterName() throws Throwable {
        String name = "writer_name";
        rule.runOnUiThread(() -> activity.setFileWriterName(name));

        assertThat(activity.tvFileWriterName.getText(), is(equalTo(name)));
    }

    @Test
    public void testSetFileCreateTime() throws Throwable {
        String date = "writer_name";
        rule.runOnUiThread(() -> activity.setFileCreateTime(date));

        onView(withText(date))
                .check(matches(isDisplayed()));
    }

    @Ignore
    @Test
    public void testMoveToFileDatail() throws Throwable {
        Intents.init();
        rule.runOnUiThread(() -> activity.moveToFileDatail());

        ViewMatchers.assertThat(activity.isFinishing(), is(true));

        Intents.intending(IntentMatchers.hasComponent(FileDetailActivity_.class.getName()));
        Intents.intending(IntentMatchers.hasExtra("roomId", any(Integer.class)));
        Intents.intending(IntentMatchers.hasExtra("fileId", any(Integer.class)));

        Intents.release();
    }

}