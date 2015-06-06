package com.tosslab.jandi.app.utils;

import com.tosslab.jandi.app.JandiApplicationTest;
import com.tosslab.jandi.app.TestJandiApplication;
import com.tosslab.jandi.app.network.models.ResMessages;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by tonyjs on 15. 5. 31..
 */
@RunWith(RobolectricGradleTestRunner.class)
public class BitmapUtilTest {

    private ResMessages.FileContent content;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData(Robolectric.application);
        String original = "http://files.jandi.com/images/1234.jpg";
        content = new ResMessages.FileContent();
        content.fileUrl = original;
        ResMessages.ThumbnailUrls extraInfo = null;
        content.extraInfo = extraInfo;
    }

    @Test
    public void testGetThumbNailUrlOrOriginal() throws Exception {
        String thumbnailOrOriginal =
                BitmapUtil.getThumbnailUrlOrOriginal(content, BitmapUtil.Thumbnails.ORIGINAL);
        assertThat(content.fileUrl, is(equalTo(thumbnailOrOriginal)));
    }

    @Test
    public void testHasImageUrl() throws Exception {
        assertThat(BitmapUtil.hasImageUrl(content), is(equalTo(true)));
    }

    @Test
    public void testGetOptimizedImageUrl() throws Exception {
        String optimizedImageUrl =
                BitmapUtil.getOptimizedImageUrl(Robolectric.application, content);
        assertThat(optimizedImageUrl, is(equalTo(content.fileUrl)));
    }
}