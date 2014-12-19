package com.tosslab.jandi.app.ui.login.login.model;

import com.tosslab.jandi.app.ui.login.IntroMainActivity;
import com.tosslab.jandi.app.ui.login.IntroMainActivity_;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.shadows.ShadowLog;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class IntroLoginModelTest {


    private IntroMainActivity introMainActivity;
    private IntroLoginModel introLoginModel;

    @Before
    public void setUp() throws Exception {
        introMainActivity = Robolectric.buildActivity(IntroMainActivity_.class).get();
        introLoginModel = IntroLoginModel_.getInstance_(introMainActivity);

        // Real Connect Dev Server
        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);

        System.setProperty("robolectric.logging", "stdout");
        ShadowLog.stream = System.out;
    }


    @Test
    public void testIsValidEmailFormat() throws Exception {

        {
            boolean validEmailFormat = introLoginModel.isValidEmailFormat("steve@tosslab.com");

            assertThat(validEmailFormat, is(equalTo(true)));
        }

        {

            boolean validEmailFormat = introLoginModel.isValidEmailFormat("123@a.com");
            assertThat(validEmailFormat, is(equalTo(true)));
        }

        {
            boolean validEmailFormat = introLoginModel.isValidEmailFormat("steve@.com");

            assertThat(validEmailFormat, is(equalTo(false)));
        }

        {
            boolean validEmailFormat = introLoginModel.isValidEmailFormat("steve@tosslab.");

            assertThat(validEmailFormat, is(equalTo(false)));
        }

        {
            boolean validEmailFormat = introLoginModel.isValidEmailFormat("steve@tosslab");

            assertThat(validEmailFormat, is(equalTo(false)));
        }

        {
            boolean validEmailFormat = introLoginModel.isValidEmailFormat("@a.com");

            assertThat(validEmailFormat, is(equalTo(false)));
        }

    }
}