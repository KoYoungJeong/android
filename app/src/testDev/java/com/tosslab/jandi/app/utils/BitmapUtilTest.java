package com.tosslab.jandi.app.utils;

import com.tosslab.jandi.app.network.models.ResMessages;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.JandiRobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by tonyjs on 15. 5. 31..
 */
@RunWith(JandiRobolectricGradleTestRunner.class)
public class BitmapUtilTest {

    private ResMessages.FileContent content;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData(RuntimeEnvironment.application);
        String original = "http://files.jandi.com/images/1234.jpg";
        content = new ResMessages.FileContent();
        content.fileUrl = original;
        ResMessages.ThumbnailUrls extraInfo = null;
        content.extraInfo = extraInfo;
    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.releaseDatabase();

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
                BitmapUtil.getOptimizedImageUrl(content);
        assertThat(optimizedImageUrl, is(equalTo(content.fileUrl)));
    }
}