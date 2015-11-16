package com.tosslab.jandi.app.ui.login;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Created by jsuch2362 on 15. 11. 10..
 */
@RunWith(AndroidJUnit4.class)
public class IntroMainActivityTest {

    @Rule
    public ActivityTestRule<IntroMainActivity_> rule = new ActivityTestRule<IntroMainActivity_>(IntroMainActivity_.class);
    private IntroMainActivity activity;

    @Before
    public void setUp() throws Exception {
        activity = rule.getActivity();
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }

    @Test
    public void testBtnAction() throws Throwable {

        {
            rule.runOnUiThread(() -> activity.btnAction(0));
            assertThat(activity.buttonTutorialFirst.isSelected(), is(true));
            assertThat(activity.buttonTutorialSecond.isSelected(), is(false));
            assertThat(activity.buttonTutorialThird.isSelected(), is(false));
            assertThat(activity.buttonTutorialLast.isSelected(), is(false));
        }

        {
            rule.runOnUiThread(() -> activity.btnAction(1));
            assertThat(activity.buttonTutorialFirst.isSelected(), is(false));
            assertThat(activity.buttonTutorialSecond.isSelected(), is(true));
            assertThat(activity.buttonTutorialThird.isSelected(), is(false));
            assertThat(activity.buttonTutorialLast.isSelected(), is(false));
        }

        {
            rule.runOnUiThread(() -> activity.btnAction(2));
            assertThat(activity.buttonTutorialFirst.isSelected(), is(false));
            assertThat(activity.buttonTutorialSecond.isSelected(), is(false));
            assertThat(activity.buttonTutorialThird.isSelected(), is(true));
            assertThat(activity.buttonTutorialLast.isSelected(), is(false));
        }

        {
            rule.runOnUiThread(() -> activity.btnAction(3));
            assertThat(activity.buttonTutorialFirst.isSelected(), is(false));
            assertThat(activity.buttonTutorialSecond.isSelected(), is(false));
            assertThat(activity.buttonTutorialThird.isSelected(), is(false));
            assertThat(activity.buttonTutorialLast.isSelected(), is(true));
        }
    }
}