package com.tosslab.jandi.app.ui.carousel.model;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.file.FileApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.carousel.domain.CarouselFileInfo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import setup.BaseInitUtil;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class CarouselViewerModelTest {

    private CarouselViewerModel model;
    private long teamId;
    private long roomId;
    private long lastImageMessageId;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData();
        model = CarouselViewerModel_.getInstance_(JandiApplication.getContext());
        teamId = AccountRepository.getRepository().getSelectedTeamId();
        roomId = EntityManager.getInstance().getDefaultTopicId();
        lastImageMessageId = getLatestFileId();

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
        return new FileApi().searchFile(reqSearchFile).firstIdOfReceivedList;
    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.clear();
    }

    @Test
    public void testSearchInitFileList() throws Exception {
        {
            List<ResMessages.FileMessage> fileMessages = model.searchInitFileList(teamId, roomId, lastImageMessageId);
            assertThat(fileMessages.size(), is(greaterThan(0)));
        }

        {
            try {
                model.searchInitFileList(teamId, roomId, -1);
                fail("성공할리가..");
            } catch (RetrofitException retrofitError) {
                System.out.println(retrofitError.getRawBody());
            }
            try {
                model.searchInitFileList(teamId, -1, lastImageMessageId);
                fail("성공 할 수 없음..");
            } catch (RetrofitException retrofitError) {
                System.out.println(retrofitError.getRawBody());
            }
            try {
                model.searchInitFileList(-1, roomId, lastImageMessageId);
                fail("성공 할 수 없음..");
            } catch (RetrofitException retrofitError) {
                System.out.println(retrofitError.getRawBody());
            }
        }
    }

    @Test
    public void testSearchBeforeFileList() throws Exception {
        {
            List<ResMessages.FileMessage> fileMessages = model.searchBeforeFileList(teamId, roomId, lastImageMessageId, 1);
            assertThat(fileMessages.size(), is(equalTo(1)));
        }

        {
            try {
                model.searchBeforeFileList(teamId, roomId, -1, 1);
                fail("성공할리가..");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                model.searchBeforeFileList(teamId, -1, lastImageMessageId, 1);
                fail("성공할리가..");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                model.searchBeforeFileList(-1, roomId, lastImageMessageId, 1);
                fail("성공할리가..");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        {
            List<ResMessages.FileMessage> fileMessages = model.searchBeforeFileList(teamId, roomId, lastImageMessageId, 0);
            assertThat(fileMessages.size(), is(greaterThan(0)));
        }
    }

    @Test
    public void testSearchAfterFileList() throws Exception {
        {
            List<ResMessages.FileMessage> fileMessages = model.searchAfterFileList(teamId, roomId, lastImageMessageId, 1);
            assertThat(fileMessages.size(), is(equalTo(0)));
        }

        {
            try {
                model.searchAfterFileList(teamId, roomId, -1, 1);
                fail("성공하면 안됨..ㅠㅠ");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                model.searchAfterFileList(teamId, -1, lastImageMessageId, 1);
                fail("성공하면 안됨..ㅠㅠ");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                model.searchAfterFileList(-1, roomId, lastImageMessageId, 1);
                fail("성공하면 안됨..ㅠㅠ");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testGetImageFileConvert() throws Exception {
        List<ResMessages.FileMessage> fileMessages = model.searchInitFileList(teamId, roomId, lastImageMessageId);
        List<CarouselFileInfo> imageFileConvert = model.getImageFileConvert(roomId, fileMessages);

        assertThat(imageFileConvert.size(), is(equalTo(fileMessages.size())));

        for (int idx = 0; idx < imageFileConvert.size(); idx++) {
            CarouselFileInfo carouselFileInfo = imageFileConvert.get(idx);
            ResMessages.FileMessage fileMessage = fileMessages.get(idx);

            assertThat(carouselFileInfo.getExt(), is(equalTo(fileMessage.content.ext)));
            assertThat(carouselFileInfo.getEntityId(), is(equalTo(roomId)));
            assertThat(carouselFileInfo.getFileLinkId(), is(equalTo(fileMessage.id)));
            assertThat(carouselFileInfo.getFileName(), is(equalTo(fileMessage.content.name)));
            assertThat(carouselFileInfo.getFileLinkUrl(), is(equalTo(fileMessage.content.fileUrl)));
            assertThat(carouselFileInfo.getFileType(), is(equalTo(fileMessage.content.type)));
            assertThat(carouselFileInfo.getFileWriter(), is(equalTo(EntityManager.getInstance().getEntityNameById(fileMessage.writerId))));
            assertThat(carouselFileInfo.getSize(), is(equalTo(fileMessage.content.size)));
        }
    }

    @Test
    public void testGetTeamId() throws Exception {
        long teamId = model.getTeamId();
        assertThat(teamId, is(equalTo(EntityManager.getInstance().getTeamId())));
    }

    @Test
    public void testFindLinkPosition() throws Exception {
        List<ResMessages.FileMessage> fileMessages = model.searchInitFileList(teamId, roomId, lastImageMessageId);
        List<CarouselFileInfo> imageFileConvert = model.getImageFileConvert(roomId, fileMessages);
        int linkPosition = model.findLinkPosition(imageFileConvert, lastImageMessageId);


        assertThat(linkPosition, is(equalTo(imageFileConvert.size() - 1)));

    }
}