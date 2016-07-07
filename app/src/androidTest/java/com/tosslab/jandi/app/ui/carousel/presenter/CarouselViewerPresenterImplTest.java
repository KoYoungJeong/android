package com.tosslab.jandi.app.ui.carousel.presenter;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.file.FileApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import setup.BaseInitUtil;

import static com.jayway.awaitility.Awaitility.await;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class CarouselViewerPresenterImplTest {

    private CarouselViewerPresenter presenter;
    private CarouselViewerPresenter.View mockView;
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

        presenter = CarouselViewerPresenterImpl_.getInstance_(JandiApplication.getContext());
        mockView = mock(CarouselViewerPresenter.View.class);

        teamId = AccountRepository.getRepository().getSelectedTeamId();
        roomId = TeamInfoLoader.getInstance().getDefaultTopicId();
        lastImageMessageId = getLatestFileId();
        presenter.setView(mockView);
        presenter.setRoomId(roomId);
        presenter.setFileId(lastImageMessageId);
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

    @Test
    public void testOnInitImageFiles() throws Exception {

        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).setFileCreateTime(anyString());

        presenter.onInitImageFiles();

        await().until(() -> finish[0]);

        verify(mockView).addFileInfos(anyList());
        verify(mockView).movePosition(anyInt());
        verify(mockView).setActionbarTitle(anyString(), anyString(), anyString());
        verify(mockView).setFileWriterName(anyString());
        verify(mockView).setFileCreateTime(anyString());
    }

    @Test
    public void testOnBeforeImageFiles() throws Exception {

        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).addFileInfos(eq(0), anyList());

        presenter.onBeforeImageFiles(lastImageMessageId, 1);

        await().until(() -> finish[0]);

        verify(mockView).addFileInfos(eq(0), anyList());
    }

//    @Ignore
//    @Test
//    public void testOnAfterImageFiles() throws Exception {
//        final boolean[] finish = {false};
//        doAnswer(new Answer() {
//            @Override
//            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
//                finish[0] = true;
//                return invocationOnMock;
//            }
//        }).when(mockView).addFileInfos(any());
//
//        presenter.onAfterImageFiles(lastImageMessageId, 1);
//
//        await().until(new Callable<Boolean>() {
//            @Override
//            public Boolean call() throws Exception {
//                return finish[0];
//            }
//        });
//
//        verify(mockView).addFileInfos(any());
//    }

//    @Ignore
//    @Test
//    public void testOnFileDownload() throws Exception {
//
//        Intents.init();
//        CarouselViewerModel model = CarouselViewerModel_.getInstance_(JandiApplication.getContext());
//        List<ResMessages.FileMessage> fileMessages = model.searchInitFileList(teamId, roomId, lastImageMessageId);
//        List<CarouselFileInfo> imageFileConvert = model.getImageFileConvert(roomId, fileMessages);
//        CarouselFileInfo fileInfo = imageFileConvert.get(0);
//        presenter.onFileDownload(fileInfo);
//
//        Intents.intending(IntentMatchers.hasComponent(DownloadService.class.getName()));
//        Intents.intending(IntentMatchers.hasExtra("file_id", fileInfo.getFileLinkId()));
//        Intents.intending(IntentMatchers.hasExtra("url", fileInfo.getFileLinkUrl()));
//        Intents.intending(IntentMatchers.hasExtra("file_name", fileInfo.getFileName()));
//        Intents.intending(IntentMatchers.hasExtra("ext", fileInfo.getExt()));
//        Intents.intending(IntentMatchers.hasExtra("file_type", fileInfo.getFileType()));
//
//        Intents.release();
//    }

}