package com.tosslab.jandi.app.ui.message.detail.view;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import setup.BaseInitUtil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(AndroidJUnit4.class)
public class TopicDetailFragmentTest {

    @Rule
    public ActivityTestRule<BaseAppCompatActivity> rule = new ActivityTestRule<BaseAppCompatActivity>(BaseAppCompatActivity.class);
    private TopicDetailFragment fragment;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        BaseInitUtil.releaseDatabase();
    }

    @Before
    public void setUp() throws Exception {

        fragment = (TopicDetailFragment) TopicDetailFragment.createFragment(JandiApplication.getContext(), TeamInfoLoader.getInstance().getDefaultTopicId());

        BaseAppCompatActivity activity = rule.getActivity();
        activity.getSupportFragmentManager().beginTransaction()
                .add(fragment, null)
                .commit();
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }

    @Test
    public void testSetTopicAutoJoin() throws Throwable {
        rule.runOnUiThread(() -> fragment.setTopicAutoJoin(true, true, true, true, true));
        assertThat(fragment.switchAutoJoin.isChecked(), is(false));

        rule.runOnUiThread(() -> fragment.setTopicAutoJoin(false, true, true, false, true));
        assertThat(fragment.switchAutoJoin.isChecked(), is(true));

        rule.runOnUiThread(() -> fragment.setTopicAutoJoin(false, true, false, false, true));
        assertThat(fragment.switchAutoJoin.isChecked(), is(false));

        rule.runOnUiThread(() -> fragment.setTopicAutoJoin(true, true, false, false, true));
        assertThat(fragment.switchAutoJoin.isChecked(), is(true));

        rule.runOnUiThread(() -> fragment.setTopicAutoJoin(true, false, false, false, true));
        assertThat(fragment.switchAutoJoin.isChecked(), is(true));

        rule.runOnUiThread(() -> fragment.setTopicAutoJoin(false, false, false, false, true));
        assertThat(fragment.switchAutoJoin.isChecked(), is(false));

    }
}