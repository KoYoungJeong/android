package com.tosslab.jandi.app.ui.filedetail;

import android.app.ProgressDialog;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResSearchFile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import setup.BaseInitUtil;

import static com.jayway.awaitility.Awaitility.await;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class FileDetailPresenterTest {

    private FileDetailPresenter fileDetailPresenter;
    private FileDetailPresenter.View mockView;
    private ResMessages.FileMessage fileMessage;

    @Before
    public void setUp() throws Exception {

        BaseInitUtil.initData();

        fileDetailPresenter = FileDetailPresenter_.getInstance_(JandiApplication.getContext());
        mockView = mock(FileDetailPresenter.View.class);
        fileDetailPresenter.setView(mockView);

        fileMessage = getFileMessage();
        MessageRepository.getRepository().upsertFileMessage(fileMessage);
    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.clear();
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
        fileDetailPresenter.onExportFile(fileMessage.id, mockProgressDialog);

        await().until(() -> finish[0]);

        // Then
        verify(mockView).exportIntentFile(any(), eq(fileMessage.content.type));
    }

    @Test
    public void testOnCopyExternLink() throws Exception {
        {
            reset(mockView);
            final boolean[] finish = {false};
            doAnswer(invocationOnMock -> {
                finish[0] = true;
                return invocationOnMock;
            }).when(mockView).dismissProgress();

            fileDetailPresenter.onCopyExternLink(fileMessage.id, false);

            await().until(() -> finish[0]);

            verify(mockView).showProgress();
            verify(mockView).setExternalShared(eq(true));
            verify(mockView).copyToClipboard(anyString());
            verify(mockView).showToast(eq(JandiApplication.getContext().getResources().getString(R.string.jandi_success_copy_clipboard_external_link)));
            verify(mockView).dismissProgress();
        }

        {

            reset(mockView);
            fileDetailPresenter.onCopyExternLink(fileMessage.id, true);

            verify(mockView).copyToClipboard(anyString());
            verify(mockView).showToast(eq(JandiApplication.getContext().getResources().getString(R.string.jandi_success_copy_clipboard_external_link)));
        }

    }

    @Test
    public void testEnableExternalLink() throws Exception {
        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).dismissProgress();

        fileDetailPresenter.enableExternalLink(fileMessage.id);

        await().until(() -> finish[0]);

        verify(mockView).showProgress();
        verify(mockView).setExternalShared(eq(true));
        verify(mockView).copyToClipboard(anyString());
        verify(mockView).showToast(eq(JandiApplication.getContext().getResources().getString(R.string.jandi_success_copy_clipboard_external_link)));
        verify(mockView).dismissProgress();


    }

    @Test
    public void testOnDisableExternLink() throws Exception {
        // Given
        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).dismissProgress();

        fileDetailPresenter.fileDetailModel.enableExternalLink(fileMessage.teamId, fileMessage.id);

        // When
        fileDetailPresenter.onDisableExternLink(fileMessage.id);

        await().until(() -> finish[0]);

        verify(mockView).showProgress();
        verify(mockView).setExternalShared(eq(false));
        verify(mockView).showToast(eq(JandiApplication.getContext().getResources().getString(R.string.jandi_success_copy_clipboard_external_link)));
        verify(mockView).dismissProgress();
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

}