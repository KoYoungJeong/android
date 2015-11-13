package com.tosslab.jandi.app.ui.account;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import setup.BaseIniUtil;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class AccountHomeActivityTest {

    @Rule
    public ActivityTestRule<AccountHomeActivity_> rule = new ActivityTestRule<>(AccountHomeActivity_.class, true, false);
    private AccountHomeActivity activity;

    @Before
    public void setUp() throws Exception {
        BaseIniUtil.initData();
        rule.launchActivity(null);

        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        activity = rule.getActivity();
    }

    @After
    public void tearDown() throws Exception {
        BaseIniUtil.clear();

    }

    @Test
    public void testOnHelpOptionSelect() throws Exception {
        activity.onHelpOptionSelect();

        onView(withText(R.string.jandi_account_home_help))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }
}