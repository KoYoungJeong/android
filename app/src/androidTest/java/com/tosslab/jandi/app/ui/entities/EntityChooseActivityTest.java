package com.tosslab.jandi.app.ui.entities;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import setup.BaseInitUtil;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.Is.is;

@RunWith(AndroidJUnit4.class)
public class EntityChooseActivityTest {

    @Rule
    public ActivityTestRule<EntityChooseActivity_> rule = new ActivityTestRule<>(EntityChooseActivity_.class, false, false);
    private EntityChooseActivity_ activity;

    @Before
    public void setUp() throws Exception {

        BaseInitUtil.initData();
        rule.launchActivity(null);
        activity = rule.getActivity();

    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.clear();
    }

    @Test
    public void testInitActionBarTitle() throws Exception {
        onView(withText(R.string.jandi_team_member))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testOnHomeOptionClick() throws Throwable {
        rule.runOnUiThread(() -> activity.onHomeOptionClick());
        assertThat(activity.isFinishing(), is(true));
    }
}