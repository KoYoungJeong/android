package com.tosslab.jandi.app.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;

import java.util.Locale;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by Bill MinWook Heo on 15. 5. 20..
 */
@RunWith(RobolectricGradleTestRunner.class)
public class LanguageUtilTest {

    @Test
    public void TestGetLanguage() {

        {
            // 일본어
            Robolectric.application.getResources().getConfiguration().locale = Locale.JAPAN;
            String language = LanguageUtil.getLanguage(Robolectric.application);
            assertThat(language, is("ja"));
            //assertThat(language, is("jp"));
        }

        {
            // 타이완 테스트
            Robolectric.application.getResources().getConfiguration().locale = Locale.TRADITIONAL_CHINESE;
            String language = LanguageUtil.getLanguage(Robolectric.application);
            assertThat(language, is("zh-TW"));
        }

        {
            // 중국어
            Robolectric.application.getResources().getConfiguration().locale = Locale.SIMPLIFIED_CHINESE;
            String language = LanguageUtil.getLanguage(Robolectric.application);
            assertThat(language, is("zh-CN"));
        }

        {
            // 한국어
            Robolectric.application.getResources().getConfiguration().locale = Locale.KOREA;
            String language = LanguageUtil.getLanguage(Robolectric.application);
            assertThat(language, is("ko"));
        }
    }
}
