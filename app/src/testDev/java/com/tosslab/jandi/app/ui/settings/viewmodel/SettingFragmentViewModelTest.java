package com.tosslab.jandi.app.ui.settings.viewmodel;

import com.tosslab.jandi.app.ui.settings.SettingsActivity;
import com.tosslab.jandi.app.ui.settings.SettingsFragment;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.JandiRobolectricGradleTestRunner;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.httpclient.FakeHttp;

@RunWith(JandiRobolectricGradleTestRunner.class)
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
//        ShadowActivity shadowPreferenceActivity = Shadows.shadowOf(settingsActivity_);
//        Intent nextStartedActivity = shadowPreferenceActivity.getNextStartedActivity();
//
//        assertThat(nextStartedActivity.getComponent().getClassName(), is(equalTo(IntroActivity_.class.getName())));

    }


}