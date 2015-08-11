package com.tosslab.jandi.app.ui.settings.viewmodel;

import android.content.Intent;

import com.tosslab.jandi.app.ui.intro.IntroActivity_;
import com.tosslab.jandi.app.ui.settings.SettingsActivity;
import com.tosslab.jandi.app.ui.settings.SettingsFragment;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.httpclient.FakeHttp;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class SettingFragmentViewModelTest {

    private SettingsActivity settingsActivity_;
    private SettingsFragment settingsFragment;
    private SettingFragmentViewModel settingFragmentViewModel;

    @Ignore
    @Before
    public void setUp() throws Exception {

        FakeHttp.getFakeHttpLayer().interceptHttpRequests(false);

        System.setProperty("robolectric.logging", "stdout");
        ShadowLog.stream = System.out;
    }

    @Test
    public void testChangeNotificationTarget() throws Exception {

        // cannot test

    }

    @Test
    public void testChangeNotificationTagerFailed() throws Exception {
        // cannot test

    }

    @Ignore
    @Test
    public void testReturnToLoginActivity() throws Exception {

        // When : return login Activity
//        settingFragmentViewModel.returnToLoginActivity();


        // Then : next Activity is IntroActivity
        ShadowActivity shadowPreferenceActivity = Shadows.shadowOf(settingsActivity_);
        Intent nextStartedActivity = shadowPreferenceActivity.getNextStartedActivity();

        assertThat(nextStartedActivity.getComponent().getClassName(), is(equalTo(IntroActivity_.class.getName())));

    }


}