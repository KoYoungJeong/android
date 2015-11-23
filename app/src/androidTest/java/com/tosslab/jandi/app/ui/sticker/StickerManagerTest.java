package com.tosslab.jandi.app.ui.sticker;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
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
    public void testGetLocalePath() throws Exception {
        {
            String assetPath = "stickers/default/mozzi";
            String localePath = stickerManager.getLocalePath(assetPath);
            assertThat(localePath, is(equalTo("")));
        }

        {
            String assetPath = "stickers/default/day";
            String localePath = stickerManager.getLocalePath(assetPath);
            assertThat(localePath, is(equalTo("")));
        }

        {
            // Given
            updateTaiwan(Locale.TAIWAN);
            String assetPath = "stickers/default/day";

            // When
            String localePath = stickerManager.getLocalePath(assetPath);

            // Then
            assertThat(localePath, is(equalTo("/zh_tw")));
        }

    }

    private void updateTaiwan(Locale locale) {
        Resources resources = JandiApplication.getContext().getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
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
            updateTaiwan(Locale.TAIWAN);
            String stickerAssetPath = stickerManager.getStickerAssetPath(groupId, stickerId);
            assertThat(stickerAssetPath, is(endsWith("default/mozzi/100_1.png")));

        }

        {
            int groupId = 101;
            String stickerId = "1";
            updateTaiwan(Locale.ENGLISH);
            String stickerAssetPath = stickerManager.getStickerAssetPath(groupId, stickerId);
            assertThat(stickerAssetPath, is(endsWith("default/day/101_1.png")));
        }
        {
            int groupId = 101;
            String stickerId = "1";
            updateTaiwan(Locale.TAIWAN);
            String stickerAssetPath = stickerManager.getStickerAssetPath(groupId, stickerId);
            assertThat(stickerAssetPath, is(endsWith("default/day/zh_tw/101_1.png")));
        }

    }
}