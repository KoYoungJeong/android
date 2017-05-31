package com.tosslab.jandi.app.ui.carousel.model;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.file.FileApi;
import com.tosslab.jandi.app.network.client.teams.search.SearchApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.search.ReqSearch;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.carousel.domain.CarouselFileInfo;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;
import setup.BaseInitUtil;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class CarouselViewerModelTest {

    private CarouselViewerModel model;
    private long teamId;
    private long roomId;
    private long lastImageMessageId;

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
        model = new CarouselViewerModel(() -> new FileApi(InnerApiRetrofitBuilder.getInstance()));
        teamId = AccountRepository.getRepository().getSelectedTeamId();
        roomId = TeamInfoLoader.getInstance().getDefaultTopicId();
        lastImageMessageId = getLatestFileId();

    }

    private long getLatestFileId() throws RetrofitException {
        ReqSearch.Builder builder = new ReqSearch.Builder().setType("file").setWriterId(-1).setRoomId(-1).setFileType("all").setPage(1).setKeyword("").setCount(1);
        return new SearchApi(InnerApiRetrofitBuilder.getInstance()).getSearch(teamId, builder.build()).getRecords().get(0).getMessageId();
    }

    @Test
    public void testSearchInitFileList() throws Exception {
        {
            Observable<List<ResMessages.FileMessage>> imageFileListObservable =
                    model.getImageFileListObservable(teamId, roomId, lastImageMessageId);
            TestSubscriber<List<ResMessages.FileMessage>> testSubscriber = new TestSubscriber<>();
            imageFileListObservable.subscribe(testSubscriber);

            testSubscriber.assertNoErrors();
            testSubscriber.assertCompleted();

            List<ResMessages.FileMessage> fileMessages = testSubscriber.getOnNextEvents().get(0);
            assertThat(fileMessages.size(), is(greaterThan(0)));
        }

        {
            Observable<List<ResMessages.FileMessage>> imageFileListObservable =
                    model.getImageFileListObservable(teamId, roomId, -1);
            TestSubscriber<List<ResMessages.FileMessage>> testSubscriber = new TestSubscriber<>();
            imageFileListObservable.subscribe(testSubscriber);

            testSubscriber.assertError(testSubscriber.getOnErrorEvents().get(0));
        }

        {
            Observable<List<ResMessages.FileMessage>> imageFileListObservable =
                    model.getImageFileListObservable(teamId, -1, lastImageMessageId);
            TestSubscriber<List<ResMessages.FileMessage>> testSubscriber = new TestSubscriber<>();
            imageFileListObservable.subscribe(testSubscriber);

            testSubscriber.assertError(testSubscriber.getOnErrorEvents().get(0));
        }

        {
            Observable<List<ResMessages.FileMessage>> imageFileListObservable =
                    model.getImageFileListObservable(-1, roomId, lastImageMessageId);
            TestSubscriber<List<ResMessages.FileMessage>> testSubscriber = new TestSubscriber<>();
            imageFileListObservable.subscribe(testSubscriber);

            testSubscriber.assertError(testSubscriber.getOnErrorEvents().get(0));
        }
    }

    @Test
    public void testSearchBeforeFileList() throws Exception {
        {
            Observable<List<ResMessages.FileMessage>> imageFileListObservable =
                    model.getBeforeImageFileListObservable(teamId, roomId, lastImageMessageId, 1);

            TestSubscriber<List<ResMessages.FileMessage>> testSubscriber = new TestSubscriber<>();
            imageFileListObservable.subscribe(testSubscriber);

            testSubscriber.assertNoErrors();
            testSubscriber.assertCompleted();

            List<ResMessages.FileMessage> fileMessages = testSubscriber.getOnNextEvents().get(0);
            assertThat(fileMessages.size(), is(greaterThan(0)));
        }

        {
            Observable<List<ResMessages.FileMessage>> imageFileListObservable =
                    model.getBeforeImageFileListObservable(teamId, roomId, -1, 1);

            TestSubscriber<List<ResMessages.FileMessage>> testSubscriber = new TestSubscriber<>();
            imageFileListObservable.subscribe(testSubscriber);

            testSubscriber.assertError(testSubscriber.getOnErrorEvents().get(0));
        }

        {
            Observable<List<ResMessages.FileMessage>> imageFileListObservable =
                    model.getBeforeImageFileListObservable(teamId, -1, lastImageMessageId, 1);

            TestSubscriber<List<ResMessages.FileMessage>> testSubscriber = new TestSubscriber<>();
            imageFileListObservable.subscribe(testSubscriber);

            testSubscriber.assertError(testSubscriber.getOnErrorEvents().get(0));
        }

        {
            Observable<List<ResMessages.FileMessage>> imageFileListObservable =
                    model.getBeforeImageFileListObservable(-1, roomId, lastImageMessageId, 1);

            TestSubscriber<List<ResMessages.FileMessage>> testSubscriber = new TestSubscriber<>();
            imageFileListObservable.subscribe(testSubscriber);

            testSubscriber.assertError(testSubscriber.getOnErrorEvents().get(0));
        }

        {
            Observable<List<ResMessages.FileMessage>> imageFileListObservable =
                    model.getBeforeImageFileListObservable(teamId, roomId, lastImageMessageId, 0);

            TestSubscriber<List<ResMessages.FileMessage>> testSubscriber = new TestSubscriber<>();
            imageFileListObservable.subscribe(testSubscriber);

            testSubscriber.assertNoErrors();
            testSubscriber.assertCompleted();

            List<ResMessages.FileMessage> fileMessages = testSubscriber.getOnNextEvents().get(0);
            assertThat(fileMessages.size(), is(greaterThan(0)));
        }
    }

    @Test
    public void testSearchAfterFileList() throws Exception {
        {
            Observable<List<ResMessages.FileMessage>> imageFileListObservable =
                    model.getAfterImageFileListObservable(teamId, roomId, lastImageMessageId, 1);

            TestSubscriber<List<ResMessages.FileMessage>> testSubscriber = new TestSubscriber<>();
            imageFileListObservable.subscribe(testSubscriber);

            testSubscriber.assertNoErrors();
            testSubscriber.assertCompleted();

            List<ResMessages.FileMessage> fileMessages = testSubscriber.getOnNextEvents().get(0);
            assertThat(fileMessages.size(), is(equalTo(0)));
        }

        {
            Observable<List<ResMessages.FileMessage>> imageFileListObservable =
                    model.getAfterImageFileListObservable(teamId, roomId, -1, 1);

            TestSubscriber<List<ResMessages.FileMessage>> testSubscriber = new TestSubscriber<>();
            imageFileListObservable.subscribe(testSubscriber);

            testSubscriber.assertError(testSubscriber.getOnErrorEvents().get(0));
        }

        {
            Observable<List<ResMessages.FileMessage>> imageFileListObservable =
                    model.getAfterImageFileListObservable(teamId, -1, lastImageMessageId, 1);

            TestSubscriber<List<ResMessages.FileMessage>> testSubscriber = new TestSubscriber<>();
            imageFileListObservable.subscribe(testSubscriber);

            testSubscriber.assertError(testSubscriber.getOnErrorEvents().get(0));
        }

        {
            Observable<List<ResMessages.FileMessage>> imageFileListObservable =
                    model.getAfterImageFileListObservable(-1, roomId, lastImageMessageId, 1);

            TestSubscriber<List<ResMessages.FileMessage>> testSubscriber = new TestSubscriber<>();
            imageFileListObservable.subscribe(testSubscriber);

            testSubscriber.assertError(testSubscriber.getOnErrorEvents().get(0));
        }
    }

    @Test
    public void testGetImageFileConvert() throws Exception {
        Observable<List<ResMessages.FileMessage>> imageFileListObservable =
                model.getImageFileListObservable(teamId, roomId, lastImageMessageId);
        TestSubscriber<List<ResMessages.FileMessage>> testSubscriber = new TestSubscriber<>();
        imageFileListObservable.subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();

        List<ResMessages.FileMessage> fileMessages = testSubscriber.getOnNextEvents().get(0);
        assertThat(fileMessages.size(), is(greaterThan(0)));

        List<CarouselFileInfo> imageFileConvert = model.getImageFileConvert(roomId, fileMessages);

        assertThat(imageFileConvert.size(), is(equalTo(fileMessages.size())));

        for (int idx = 0; idx < imageFileConvert.size(); idx++) {
            CarouselFileInfo carouselFileInfo = imageFileConvert.get(idx);
            ResMessages.FileMessage fileMessage = fileMessages.get(idx);

            assertThat(carouselFileInfo.getExt(), is(equalTo(fileMessage.content.ext)));
            assertThat(carouselFileInfo.getEntityId(), is(equalTo(roomId)));
            assertThat(carouselFileInfo.getFileMessageId(), is(equalTo(fileMessage.id)));
            assertThat(carouselFileInfo.getFileName(), is(equalTo(fileMessage.content.title)));
            assertThat(carouselFileInfo.getFileLinkUrl(), is(equalTo(fileMessage.content.fileUrl)));
            assertThat(carouselFileInfo.getFileType(), is(equalTo(fileMessage.content.type)));
            assertThat(carouselFileInfo.getFileWriterName(), is(equalTo(TeamInfoLoader.getInstance().getMemberName(fileMessage.writerId))));
            assertThat(carouselFileInfo.getSize(), is(equalTo(fileMessage.content.size)));
        }
    }

    @Test
    public void testGetTeamId() throws Exception {
        long teamId = model.getTeamId();
        assertThat(teamId, is(equalTo(TeamInfoLoader.getInstance().getTeamId())));
    }

    @Test
    public void testFindLinkPosition() throws Exception {
        Observable<List<ResMessages.FileMessage>> imageFileListObservable =
                model.getImageFileListObservable(teamId, roomId, lastImageMessageId);
        TestSubscriber<List<ResMessages.FileMessage>> testSubscriber = new TestSubscriber<>();
        imageFileListObservable.subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();

        List<ResMessages.FileMessage> fileMessages = testSubscriber.getOnNextEvents().get(0);
        assertThat(fileMessages.size(), is(greaterThan(0)));

        List<CarouselFileInfo> imageFileConvert = model.getImageFileConvert(roomId, fileMessages);
        int linkPosition = model.findLinkPosition(imageFileConvert, lastImageMessageId);

        assertThat(linkPosition, is(equalTo(imageFileConvert.size() - 1)));

    }
}