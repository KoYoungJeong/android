package com.tosslab.jandi.app.ui.profile.defaultimage.presenter;

import android.net.Uri;

import com.jayway.awaitility.Awaitility;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.utils.file.FileUtil;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import setup.BaseInitUtil;

import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by tee on 16. 1. 8..
 */
public class ProfileImageSelectorPresenterTest {

    private ProfileImageSelectorPresenter profileImageSelectorPresenter;
    private ProfileImageSelectorPresenter.View mockView;

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
        profileImageSelectorPresenter = ProfileImageSelectorPresenter_
                .getInstance_(JandiApplication.getContext());
        mockView = mock(ProfileImageSelectorPresenter.View.class);
        profileImageSelectorPresenter.setView(mockView);

    }

    @Test
    public void testInitList() {
        //given
        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).showInitialImage();

        //when
        profileImageSelectorPresenter.initLists();
        Awaitility.await().until(() -> finish[0]);

        //then
        verify(mockView).showColorList(anyList());
        verify(mockView).showCharacterList(anyList());
        verify(mockView).showInitialImage();
    }

    @Test
    public void testMakeCustomProfileImageFile() {
        //given
        File tempFile = null;
        try {
            File directory = new File(FileUtil.getDownloadPath());
            tempFile = File.createTempFile("temp", ".png", directory);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String Url = "http://files.jandi.io/files-resource/characters/character_02.png";

        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).finishProgress();

        //when
        profileImageSelectorPresenter.makeCustomProfileImageFile(Uri.fromFile(tempFile), Url, 0xff5EA879);
        Awaitility.await().until(() -> finish[0]);

        //then
        if (tempFile != null) {
            Assert.assertTrue(tempFile.exists());
            tempFile.delete();
        } else {
            Assert.assertFalse(true);
        }
    }

}