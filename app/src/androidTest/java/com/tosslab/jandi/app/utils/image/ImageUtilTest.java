package com.tosslab.jandi.app.utils.image;

import com.tosslab.jandi.app.utils.file.FileUtil;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * Created by tee on 16. 1. 5..
 */
public class ImageUtilTest {

    @Test
    public void testGetCustomProfileImageFile() {
        String Url = "http://files.jandi.io/files-resource/characters/character_02.png";
        ImageUtil.getCustomProfileImageFile(Url, 0xff5EA879);
        File profileImageFile = new File(FileUtil.getTempDownloadPath() + "/" + "tempProfile.png");
        if (profileImageFile != null) {
            Assert.assertTrue(profileImageFile.exists());
            profileImageFile.delete();
        } else {
            Assert.assertFalse(true);
        }
    }

}