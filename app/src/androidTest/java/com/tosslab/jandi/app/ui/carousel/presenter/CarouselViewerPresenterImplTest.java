package com.tosslab.jandi.app.ui.carousel.presenter;

import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.intent.matcher.IntentMatchers;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.file.FileApi;
import com.tosslab.jandi.app.network.client.teams.search.SearchApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.search.ReqSearch;
import com.tosslab.jandi.app.services.download.DownloadService;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.carousel.domain.CarouselFileInfo;
import com.tosslab.jandi.app.ui.carousel.model.CarouselViewerModel;
import com.tosslab.jandi.app.ui.carousel.module.CarouselViewerModule;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Component;
import rx.Observable;
import rx.observers.TestSubscriber;
import setup.BaseInitUtil;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class CarouselViewerPresenterImplTest {

    @Inject
    CarouselViewerPresenter presenter;

    @Inject
    CarouselViewerPresenter.View mockView;

    private long teamId;
    private long roomId;
    private long lastImageMessageId;
    private long firstImageMessageId;

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
        CarouselViewerPresenter.View viewMock = mock(CarouselViewerPresenter.View.class);
        DaggerCarouselViewerPresenterImplTest_CarouselViewerPresenterImplTestComponent.builder()
                .carouselViewerModule(new CarouselViewerModule(viewMock))
                .build()
                .inject(this);
        teamId = AccountRepository.getRepository().getSelectedTeamId();
        roomId = TeamInfoLoader.getInstance().getDefaultTopicId();
        lastImageMessageId = getLatestFileId();
        firstImageMessageId = getFirstFileId();
    }

    private long getLatestFileId() throws RetrofitException {
        ReqSearch.Builder builder = new ReqSearch.Builder().setType("file").setWriterId(-1).setRoomId(-1).setFileType("all").setPage(1).setKeyword("").setCount(1);
        return new SearchApi(RetrofitBuilder.getInstance()).getSearch(teamId, builder.build()).getRecords().get(0).getMessageId();
    }

    private long getFirstFileId() throws RetrofitException {
        ReqSearch.Builder builder = new ReqSearch.Builder().setType("file").setWriterId(-1).setRoomId(-1).setFileType("all").setPage(1).setKeyword("").setCount(1);
        return new SearchApi(RetrofitBuilder.getInstance()).getSearch(teamId, builder.build()).getRecords().get(0).getMessageId();    }

    @Test
    public void testOnInitImageFiles() throws Exception {

        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).initCarouselInfo(any());

        presenter.onInitImageFiles(roomId, lastImageMessageId);

        await().until(() -> finish[0]);

        verify(mockView).addFileInfos(anyList());
        verify(mockView).movePosition(anyInt());
        verify(mockView).initCarouselInfo(any());
    }

    @Test
    public void testOnBeforeImageFiles() throws Exception {

        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).addFileInfos(eq(0), anyList());

        presenter.setIsFirst(false);
        presenter.onBeforeImageFiles(roomId, lastImageMessageId, 1);

        await().until(() -> finish[0]);

        verify(mockView).addFileInfos(eq(0), anyList());
    }

    @Test
    public void testOnAfterImageFiles() throws Exception {
        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).addFileInfos(anyList());

        presenter.setIsLast(false);
        presenter.onAfterImageFiles(roomId, firstImageMessageId, 1);

        await().until(() -> finish[0]);

        verify(mockView).addFileInfos(any());
    }

    @Ignore
    @Test
    public void testOnFileDownload() throws Exception {

        Intents.init();
        CarouselViewerModel model = new CarouselViewerModel(() -> new FileApi(RetrofitBuilder.getInstance()));
        Observable<List<ResMessages.FileMessage>> imageFileListObservable =
                model.getImageFileListObservable(teamId, roomId, lastImageMessageId);
        TestSubscriber<List<ResMessages.FileMessage>> testSubscriber = new TestSubscriber<>();
        imageFileListObservable.subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();

        List<ResMessages.FileMessage> fileMessages = testSubscriber.getOnNextEvents().get(0);
        assertThat(fileMessages.size(), is(greaterThan(0)));

        List<CarouselFileInfo> imageFileConvert = model.getImageFileConvert(roomId, fileMessages);
        CarouselFileInfo fileInfo = imageFileConvert.get(0);
        presenter.onFileDownload(fileInfo);

        Intents.intending(IntentMatchers.hasComponent(DownloadService.class.getName()));
        Intents.intending(IntentMatchers.hasExtra("file_id", fileInfo.getFileMessageId()));
        Intents.intending(IntentMatchers.hasExtra("url", fileInfo.getFileLinkUrl()));
        Intents.intending(IntentMatchers.hasExtra("file_name", fileInfo.getFileName()));
        Intents.intending(IntentMatchers.hasExtra("ext", fileInfo.getExt()));
        Intents.intending(IntentMatchers.hasExtra("file_type", fileInfo.getFileType()));

        Intents.release();
    }

    @Component(modules = CarouselViewerModule.class)
    @Singleton
    public interface CarouselViewerPresenterImplTestComponent {
        void inject(CarouselViewerPresenterImplTest test);
    }
}