package com.tosslab.jandi.app.ui.filedetail.model;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.teams.search.SearchApi;
import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.search.ReqSearch;
import com.tosslab.jandi.app.network.models.search.ResSearch;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.utils.file.FileUtil;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import javax.inject.Inject;

import dagger.Component;
import setup.BaseInitUtil;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

/**
 * Created by jsuch2362 on 15. 11. 18..
 */
@RunWith(AndroidJUnit4.class)
public class FileDetailModelTest {

    @Inject
    FileDetailModel fileDetailModel;
    private ResSearch.SearchRecord fileMessage;

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
        DaggerFileDetailModelTest_TestComponent.builder()
                .build().inject(this);
        fileMessage = getFileMessage();
    }

    private ResSearch.SearchRecord getFileMessage() throws RetrofitException {
        ReqSearch.Builder builder = new ReqSearch.Builder().setType("file").setWriterId(-1).setRoomId(-1).setFileType("all").setPage(1).setKeyword("").setCount(1);
        return new SearchApi(RetrofitBuilder.getInstance()).getSearch(TeamInfoLoader.getInstance().getTeamId(), builder.build()).getRecords().get(0);
    }

    @Test
    public void testGetTeamId() throws Exception {
        long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
        long teamId = fileDetailModel.getTeamId();

        assertThat(teamId, is(equalTo(selectedTeamId)));

    }

    @Test
    public void testEnableExternalLink() throws Exception {

        // Given
        long teamId = fileDetailModel.getTeamId();
        // When
        ResMessages.FileMessage fileMessage1 = fileDetailModel.enableExternalLink(teamId, fileMessage.getMessageId());

        // Then
        assertThat(fileMessage1.content.externalShared, is(true));
        assertThat(fileMessage1.content.externalUrl, is(notNullValue()));
        assertThat(fileMessage1.content.externalCode, is(notNullValue()));

    }

    @Test
    public void testDisableExternalLink() throws Exception {

        // Given
        long teamId = fileDetailModel.getTeamId();
        fileDetailModel.enableExternalLink(teamId, this.fileMessage.getMessageId());

        // When
        ResMessages.FileMessage fileMessage = fileDetailModel.disableExternalLink(teamId, this.fileMessage.getMessageId());

        // Then
        assertThat(fileMessage.content.externalShared, is(false));
        assertThat(fileMessage.content.externalUrl, is(nullValue()));
        assertThat(fileMessage.content.externalCode, is(nullValue()));
    }

    @Test
    public void testDownloadFile() throws Exception {
        String fileUrl = fileDetailModel.getDownloadUrl(fileMessage.getFile().getFileUrl());
        String downloadFilePath = fileDetailModel.getDownloadFilePath(fileMessage.getFile().getTitle());

        final boolean[] finish = {false};
        fileDetailModel.downloadFile(fileUrl, downloadFilePath, callback2 -> callback2.subscribe(it -> {}, t -> {}, () -> finish[0] = true));

        await().until(() -> finish[0]);

        File file = new File(downloadFilePath);
        assertThat(file.exists(), is(true));
        assertThat(file.length(), is(equalTo(fileMessage.getFile().getSize())));
    }

    @Test
    public void testGetDownloadFilePath() throws Exception {
        String title = "hello";
        String downloadFilePath = fileDetailModel.getDownloadFilePath(title);
        assertThat(downloadFilePath, is(equalTo(FileUtil.getDownloadPath() + "/hello")));
    }

    @Test
    public void testGetDownloadUrl() throws Exception {
        {
            String fileUrl = "http://test";
            String downloadUrl = fileDetailModel.getDownloadUrl(fileUrl);
            assertThat(downloadUrl, is(equalTo(fileUrl + "/download")));
        }

        {
            String fileUrl = "http://test/d/download";
            String downloadUrl = fileDetailModel.getDownloadUrl(fileUrl);
            assertThat(downloadUrl, is(equalTo(fileUrl)));
        }

    }

    @Component(modules = {ApiClientModule.class})
    public interface TestComponent {
        void inject(FileDetailModelTest test);
    }

}