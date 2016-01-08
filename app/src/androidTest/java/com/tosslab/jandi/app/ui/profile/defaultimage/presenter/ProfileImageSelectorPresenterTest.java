package com.tosslab.jandi.app.ui.profile.defaultimage.presenter;

import android.net.Uri;

import com.tosslab.jandi.app.utils.file.FileUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by tee on 16. 1. 8..
 */
public class ProfileImageSelectorPresenterTest {

    ProfileImageSelectorPresenter profileImageSelectorPresenter;

    @Before
    public void setUp() throws Exception {
        profileImageSelectorPresenter = new ProfileImageSelectorPresenter();
    }

    @Test
    public void testMakeCustomProfileImageFile() {
        try {
            File directory = new File(FileUtil.getDownloadPath());
            File tempFile = File.createTempFile("temp", ".png", directory);
            String Url = "http://files.jandi.io/files-resource/characters/character_02.png";
            profileImageSelectorPresenter.makeCustomProfileImageFile(Uri.fromFile(tempFile), Url, 0xff5EA879);
            if (tempFile != null) {
                Assert.assertTrue(tempFile.exists());
                tempFile.delete();
            } else {
                Assert.assertFalse(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}