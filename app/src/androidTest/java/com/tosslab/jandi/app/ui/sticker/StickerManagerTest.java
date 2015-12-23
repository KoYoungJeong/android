package com.tosslab.jandi.app.ui.sticker;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class StickerManagerTest {

    private StickerManager stickerManager;

    @Before
    public void setUp() throws Exception {
        stickerManager = StickerManager.getInstance();
    }

    @Test
    public void testGetStickerAssetPath() throws Exception {
        {
            int groupId = 100;
            String stickerId = "1";
            String stickerAssetPath = stickerManager.getStickerAssetPath(groupId, stickerId);
            assertThat(stickerAssetPath, is(endsWith("default/mozzi/100_1.png")));
        }

        {
            int groupId = 100;
            String stickerId = "1";
            String stickerAssetPath = stickerManager.getStickerAssetPath(groupId, stickerId);
            assertThat(stickerAssetPath, is(endsWith("default/mozzi/100_1.png")));

        }

        {
            int groupId = 101;
            String stickerId = "1";
            String stickerAssetPath = stickerManager.getStickerAssetPath(groupId, stickerId);
            assertThat(stickerAssetPath, is(endsWith("default/day/101_1.png")));
        }
        {
            int groupId = 102;
            String stickerId = "1";
            String stickerAssetPath = stickerManager.getStickerAssetPath(groupId, stickerId);
            assertThat(stickerAssetPath, is(endsWith("default/day/zh_tw/102_1.png")));
        }

    }
}