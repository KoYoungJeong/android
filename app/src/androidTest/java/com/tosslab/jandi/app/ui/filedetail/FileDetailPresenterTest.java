package com.tosslab.jandi.app.ui.filedetail;

import android.app.ProgressDialog;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.client.file.FileApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResSearchFile;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import setup.BaseInitUtil;

import static com.jayway.awaitility.Awaitility.await;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class FileDetailPresenterTest {

    private FileDetailPresenter fileDetailPresenter;
    private FileDetailPresenter.View mockView;
    private ResMessages.FileMessage fileMessage;

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


        fileDetailPresenter = FileDetailPresenter_.getInstance_(JandiApplication.getContext());
        mockView = mock(FileDetailPresenter.View.class);
        fileDetailPresenter.setView(mockView);

        fileMessage = getFileMessage();
        MessageRepository.getRepository().upsertFileMessage(fileMessage);
    }



    @Test
    public void testOnExportFile() throws Exception {
        // Given
        ProgressDialog mockProgressDialog = mock(ProgressDialog.class);
        doAnswer(invocationOnMock -> invocationOnMock).when(mockProgressDialog).setProgress(anyInt());
        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockProgressDialog).dismiss();

        // When
        fileDetailPresenter.onExportFile(fileMessage, mockProgressDialog);

        await().until(() -> finish[0]);

        // Then
        verify(mockView).startExportedFileViewerActivity(any(), anyString());
    }

    @Test
    public void testEnableExternalLink() throws Exception {
        reset(mockView);
        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).setExternalLinkToClipboard();

        fileDetailPresenter.onEnableExternalLink(fileMessage.id);

        await().until(() -> finish[0]);

        verify(mockView).showProgress();
        verify(mockView).setFileDetail(any(), anyList(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());
        verify(mockView).notifyDataSetChanged();
        verify(mockView).dismissProgress();
    }

    @Test
    public void testOnDisableExternalLink() throws Exception {
        // Given
        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).showDisableExternalLinkSuccessToast();


        // When
        fileDetailPresenter.onDisableExternalLink(fileMessage.id);

        await().until(() -> finish[0]);

        verify(mockView).showProgress();
        verify(mockView).setFileDetail(any(), anyList(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());
        verify(mockView).notifyDataSetChanged();
        verify(mockView).dismissProgress();
        verify(mockView).showDisableExternalLinkSuccessToast();
    }

    @Test
    public void testGetSharedTopicIds() throws Exception {
        // Given
        ResMessages.FileMessage fileMessage = new ResMessages.FileMessage();
        fileMessage.shareEntities = getSharedEntities();

        // When
//        fileDetailPresenter.onShareAction();
//        List<Long> sharedTopicIds = fileDetailPresenter.getSharedTopicIds(fileMessage);

//        FormattedEntity entity = EntityManager.getInstance().getEntityById(sharedTopicIds.get(0));
        // Then
//        assertThat(sharedTopicIds.size(), is(equalTo(1)));
//        assertThat(entity.isUser(), is(false));


    }

    private List<ResMessages.OriginalMessage.IntegerWrapper> getSharedEntities() {
        List<ResMessages.OriginalMessage.IntegerWrapper> integerWrappers = new ArrayList<>();

        FormattedEntity topic = EntityManager.getInstance().getJoinedChannels().get(0);
        FormattedEntity user = EntityManager.getInstance().getFormattedUsers().get(0);

        ResMessages.OriginalMessage.IntegerWrapper object = new ResMessages.OriginalMessage.IntegerWrapper();
        object.setShareEntity(topic.getId());
        integerWrappers.add(object);

        ResMessages.OriginalMessage.IntegerWrapper object1 = new ResMessages.OriginalMessage.IntegerWrapper();
        object1.setShareEntity(user.getId());
        integerWrappers.add(object1);
        return integerWrappers;
    }

    private ResMessages.FileMessage getFileMessage() throws RetrofitException {
        ReqSearchFile reqSearchFile = new ReqSearchFile();
        reqSearchFile.searchType = ReqSearchFile.SEARCH_TYPE_FILE;
        reqSearchFile.listCount = ReqSearchFile.MAX;

        reqSearchFile.fileType = ReqSearchFile.FILE_TYPE_IMAGE;
        reqSearchFile.writerId = "all";
        reqSearchFile.sharedEntityId = -1;

        reqSearchFile.startMessageId = -1;
        reqSearchFile.keyword = "";
        reqSearchFile.teamId = EntityManager.getInstance().getTeamId();
        ResSearchFile resSearchFile = new FileApi(RetrofitBuilder.newInstance()).searchFile(reqSearchFile);

        ResMessages.OriginalMessage originalMessage = resSearchFile.files.get(0);

        return ((ResMessages.FileMessage) originalMessage);
    }

}