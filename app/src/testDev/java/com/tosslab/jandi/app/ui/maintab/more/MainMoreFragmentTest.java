package com.tosslab.jandi.app.ui.maintab.more;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Locale;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by Bill MinWook Heo on 15. 5. 20..
 */
@RunWith(RobolectricGradleTestRunner.class)
public class MainMoreFragmentTest {

    private MainMoreFragment mainMoreFragment;

    @Before
    public void setUp() throws Exception {

        mainMoreFragment = MainMoreFragment_.builder().build();
        mainMoreFragment.mContext = RuntimeEnvironment.application;

    }

    @Test
    public void testSupportUrlEachLanguage() throws Exception {
        {
            //중국어
            RuntimeEnvironment.application.getResources().getConfiguration().locale = Locale.SIMPLIFIED_CHINESE;

            String supportUrlEachLanguage = mainMoreFragment.getSupportUrlEachLanguage();
            System.out.println(supportUrlEachLanguage);
            assertThat(supportUrlEachLanguage, is(mainMoreFragment.SUPPORT_URL_ZH_CH));
        }
        {
            //타이완
            RuntimeEnvironment.application.getResources().getConfiguration().locale = Locale.TRADITIONAL_CHINESE;

            String supportUrlEachLanguage = mainMoreFragment.getSupportUrlEachLanguage();
            System.out.println(supportUrlEachLanguage);
            assertThat(supportUrlEachLanguage, is(mainMoreFragment.SUPPORT_URL_ZH_TW));
        }
        {
            //일본어
            RuntimeEnvironment.application.getResources().getConfiguration().locale = Locale.JAPAN;

            String supportUrlEachLanguage = mainMoreFragment.getSupportUrlEachLanguage();
            System.out.println(supportUrlEachLanguage);
            //assertThat(supportUrlEachLanguage, is(mainMoreFragment.SUPPORT_URL_JA));
        }
        {
            //영어
            RuntimeEnvironment.application.getResources().getConfiguration().locale = Locale.ENGLISH;

            String supportUrlEachLanguage = mainMoreFragment.getSupportUrlEachLanguage();
            System.out.println(supportUrlEachLanguage);
            assertThat(supportUrlEachLanguage, is(mainMoreFragment.SUPPORT_URL_EN));
        }
        {
            //한국어
            RuntimeEnvironment.application.getResources().getConfiguration().locale = Locale.KOREA;

            String supportUrlEachLanguage = mainMoreFragment.getSupportUrlEachLanguage();
            System.out.println(supportUrlEachLanguage);
            assertThat(supportUrlEachLanguage, is(mainMoreFragment.SUPPORT_URL_KO));
        }
    }


}