package com.tosslab.jandi.app.ui.filedetail.model;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.utils.file.FileUtil;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsEqual;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.List;

import setup.BaseInitUtil;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

/**
 * Created by jsuch2362 on 15. 11. 18..
 */
@RunWith(AndroidJUnit4.class)
public class FileDetailModelTest {

    private FileDetailModel fileDetailModel;
    private ResMessages.FileMessage fileMessage;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData();
        fileDetailModel = FileDetailModel_.getInstance_(JandiApplication.getContext());
        fileMessage = getFileMessage();
    }

    @Test
    public void testGetUnsharedEntities() throws Exception {

        List<FormattedEntity> unsharedEntities = fileDetailModel.getUnsharedEntities();

        assertThat(unsharedEntities, is(notNullValue()));
        assertThat(unsharedEntities.size(), is(greaterThan(0)));

    }

    private ResMessages.FileMessage getFileMessage() {
        ReqSearchFile reqSearchFile = new ReqSearchFile();
        reqSearchFile.searchType = ReqSearchFile.SEARCH_TYPE_FILE;
        reqSearchFile.listCount = ReqSearchFile.MAX;

        reqSearchFile.fileType = ReqSearchFile.FILE_TYPE_IMAGE;
        reqSearchFile.writerId = "all";
        reqSearchFile.sharedEntityId = -1;

        reqSearchFile.startMessageId = -1;
        reqSearchFile.keyword = "";
        reqSearchFile.teamId = EntityManager.getInstance().getTeamId();
        ResSearchFile resSearchFile = RequestApiManager.getInstance().searchFileByMainRest(reqSearchFile);

        ResMessages.OriginalMessage originalMessage = resSearchFile.files.get(0);

        return ((ResMessages.FileMessage) originalMessage);
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
        ResMessages.FileMessage fileMessage1 = fileDetailModel.enableExternalLink(teamId, fileMessage.id);

        // Then
        assertThat(fileMessage1.content.externalShared, is(true));
        assertThat(fileMessage1.content.externalUrl, is(notNullValue()));
        assertThat(fileMessage1.content.externalCode, is(notNullValue()));

    }

    @Test
    public void testDisableExternalLink() throws Exception {

        // Given
        long teamId = fileDetailModel.getTeamId();
        fileDetailModel.enableExternalLink(teamId, this.fileMessage.id);

        // When
        ResMessages.FileMessage fileMessage = fileDetailModel.disableExternalLink(teamId, this.fileMessage.id);

        // Then
        assertThat(fileMessage.content.externalShared, is(false));
        assertThat(fileMessage.content.externalUrl, is(nullValue()));
        assertThat(fileMessage.content.externalCode, is(nullValue()));
    }

    @Test
    public void testUpdateExternalLink() throws Exception {

        // Given
        ResMessages.FileContent fileContent1 = createFileContent();

        String changedCode = "externalCode2";
        String changedUrl = "externalUrl2";
        boolean changedShared = false;

        // When
        fileDetailModel.updateExternalLink(fileContent1.fileUrl, changedShared, changedUrl, changedCode);

        ResMessages.FileMessage fileMessage = fileDetailModel.getFileMessage(this.fileMessage.id);
        ResMessages.FileContent fileContent = fileMessage.content;

        // Then
        assertThat(fileContent.externalCode, Is.is(IsEqual.equalTo(changedCode)));
        assertThat(fileContent.externalUrl, Is.is(IsEqual.equalTo(changedUrl)));
        assertThat(fileContent.externalShared, Is.is(changedShared));
    }

    @Test
    public void testDownloadFile() throws Exception {
        String fileUrl = fileDetailModel.getDownloadUrl(fileMessage.content.fileUrl);
        String downloadFilePath = fileDetailModel.getDownloadFilePath(fileMessage.content.title);

        final boolean[] finish = {false};
        fileDetailModel.downloadFile(fileUrl, downloadFilePath, (downloaded, total) -> {

        }, (e, result) -> {
            finish[0] = true;
        });

        await().until(() -> finish[0]);

        File file = new File(downloadFilePath);
        assertThat(file.exists(), is(true));
        assertThat(file.length(), is(equalTo(fileMessage.content.size)));
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

    private ResMessages.FileContent createFileContent() throws java.sql.SQLException {
        ResMessages.FileMessage fileMessage = getFileMessage();
        MessageRepository.getRepository().upsertFileMessage(fileMessage);
        return fileMessage.content;
    }
}