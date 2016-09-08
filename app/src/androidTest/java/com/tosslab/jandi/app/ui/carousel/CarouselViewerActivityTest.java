package com.tosslab.jandi.app.ui.carousel;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.TopicRepository;
import com.tosslab.jandi.app.network.client.file.FileApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.carousel.domain.CarouselFileInfo;
import com.tosslab.jandi.app.ui.carousel.model.CarouselViewerModel;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;
import setup.BaseInitUtil;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;

@RunWith(AndroidJUnit4.class)
public class CarouselViewerActivityTest {

    @Rule
    public IntentsTestRule<CarouselViewerActivity> rule = new IntentsTestRule<>(CarouselViewerActivity.class, false, false);
    private long latestFileId;
    private long teamId;
    private long roomId;
    private CarouselViewerActivity activity;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        BaseInitUtil.releaseDatabase();
    }

    @Before
    public void setUp() throws Exception {

        teamId = AccountRepository.getRepository().getSelectedTeamId();
        roomId = TopicRepository.getInstance().getDefaultTopic(teamId).getId();
        latestFileId = getLatestFileId();

        Intent startIntent = new Intent();
        startIntent.putExtra("roomId", roomId);
        startIntent.putExtra("startMessageId", latestFileId);
        startIntent.putExtra("mode", CarouselViewerActivity.CAROUSEL_MODE);
        rule.launchActivity(startIntent);
        activity = rule.getActivity();

        await().until(() -> activity.viewPager.getChildCount() > 0);

    }

    private int getLatestFileId() throws RetrofitException {
        ReqSearchFile reqSearchFile = new ReqSearchFile();
        reqSearchFile.searchType = ReqSearchFile.SEARCH_TYPE_FILE;
        reqSearchFile.fileType = "image";
        reqSearchFile.writerId = "all";
        reqSearchFile.keyword = "";
        reqSearchFile.listCount = 1;
        reqSearchFile.sharedEntityId = roomId;
        reqSearchFile.startMessageId = -1;
        reqSearchFile.teamId = teamId;
        return new FileApi(RetrofitBuilder.getInstance()).searchFile(reqSearchFile).firstIdOfReceivedList;
    }

    private List<CarouselFileInfo> getCarousel() throws RetrofitException {
        CarouselViewerModel model = new CarouselViewerModel(() -> new FileApi(RetrofitBuilder.getInstance()));
        Observable<List<ResMessages.FileMessage>> imageFileListObservable =
                model.getImageFileListObservable(teamId, roomId, getLatestFileId());
        TestSubscriber<List<ResMessages.FileMessage>> testSubscriber = new TestSubscriber<>();
        imageFileListObservable.subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();

        List<ResMessages.FileMessage> fileMessages = testSubscriber.getOnNextEvents().get(0);
        Assert.assertThat(fileMessages.size(), is(greaterThan(0)));

        List<CarouselFileInfo> imageFileConvert = model.getImageFileConvert(roomId, fileMessages);
        return imageFileConvert;
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

//    @Ignore
//    @Test
//    public void testSetInitFail() throws Throwable {
//
//        rule.runOnUiThread(() -> activity.setInitFail());
//        assertThat(activity.isFinishing(), is(true));
//    }

    @Test
    public void testMovePosition() throws Throwable {

        int movePosition = 0;
        rule.runOnUiThread(() -> activity.movePosition(movePosition));
        assertThat(activity.viewPager.getCurrentItem(), is(equalTo(movePosition)));
    }

    @Test
    public void testSetActionbarTitle() throws Throwable {
        String fileName = "1111";
        rule.runOnUiThread(() -> activity.setFileTitle(fileName));


        onView(withText(fileName))
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

//    @Ignore
//    @Test
//    public void testMoveToFileDatail() throws Throwable {
//        rule.runOnUiThread(() -> activity.moveToFileDatail());
//
//        assertThat(activity.isFinishing(), is(true));
//
//        Intents.intending(IntentMatchers.hasComponent(FileDetailActivity_.class.getName()));
//        Intents.intending(IntentMatchers.hasExtra("roomId", eq(roomId)));
//        Intents.intending(IntentMatchers.hasExtra("fileId", eq(latestFileId)));
//    }

}