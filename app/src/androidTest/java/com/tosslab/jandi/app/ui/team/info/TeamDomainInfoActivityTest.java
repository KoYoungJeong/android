package com.tosslab.jandi.app.ui.team.info;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import setup.BaseInitUtil;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class TeamDomainInfoActivityTest {

    @Rule
    public ActivityTestRule<TeamDomainInfoActivity_> rule = new ActivityTestRule<TeamDomainInfoActivity_>(TeamDomainInfoActivity_.class, false, false);
    private TeamDomainInfoActivity activity;

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
    public void testGoHomeUpMenu() throws Throwable {
        rule.runOnUiThread(() -> activity.goHomeUpMenu());

        assertThat(activity.isFinishing(), is(true));
    }

    @Test
    public void testSuccessCreateTeam() throws Throwable {
        rule.runOnUiThread(() -> activity.successCreateTeam("hello"));
        assertThat(activity.isFinishing(), is(true));
    }

    @Test
    public void testShowProgressWheel() throws Throwable {
        rule.runOnUiThread(() -> activity.showProgressWheel());
        assertThat(activity.progressWheel.isShowing(), is(true));
    }

    @Test
    public void testDismissProgressWheel() throws Throwable {
        rule.runOnUiThread(() -> activity.dismissProgressWheel());
        assertThat(activity.progressWheel.isShowing(), is(false));
    }

    @Test
    public void testFinishView() throws Throwable {
        rule.runOnUiThread(() -> activity.finishView());
        assertThat(activity.isFinishing(), is(true));
    }
}