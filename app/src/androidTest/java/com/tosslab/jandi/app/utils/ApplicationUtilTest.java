package com.tosslab.jandi.app.utils;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class ApplicationUtilTest {

    private static final String[] HTTP_URLS = {
            "http://www.jandi.com",
            "Http://www.jandi.com",
            "hTtp://www.jandi.com",
            "htTp://www.jandi.com",
            "httP://www.jandi.com",
            "HTTP://www.jandi.com",
            "www.jandi.com"
    };
    private static final String[] FTP_URLS = {
            "ftp://www.jandi.com",
            "Ftp://www.jandi.com",
            "fTp://www.jandi.com",
            "ftP://www.jandi.com",
            "FTP://www.jandi.com",
    };

    @Test
    public void testGetAvailableUrl_HTTP() throws Exception {
        for (String url : HTTP_URLS) {
            String availableUrl = ApplicationUtil.getAvailableUrl(url);
            assertThat(HTTP_URLS[0], is(equalTo(availableUrl)));
        }
    }

    @Test
    public void testGetAvailableUrl_FTP() throws Exception {

        for (String url : FTP_URLS) {
            String availableUrl = ApplicationUtil.getAvailableUrl(url);
            assertThat(FTP_URLS[0], is(equalTo(availableUrl)));
        }
    }
}