package com.tosslab.jandi.app.ui.message.detail.view;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;

import org.junit.Before;
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

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData();

        EntityManager manager = EntityManager.getInstance();
        fragment = TopicDetailFragment_.builder()
                .entityId(manager.getDefaultTopicId())
                .teamId(manager.getTeamId())
                .build();

        BaseAppCompatActivity activity = rule.getActivity();
        activity.getSupportFragmentManager().beginTransaction()
                .add(fragment, null)
                .commit();
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }

    @Test
    public void testSetTopicAutoJoin() throws Throwable {
        rule.runOnUiThread(() -> fragment.setTopicAutoJoin(true, true, true, true));
        assertThat(fragment.switchAutoJoin.isEnabled(), is(false));
        assertThat(fragment.switchAutoJoin.isChecked(), is(false));

        rule.runOnUiThread(() -> fragment.setTopicAutoJoin(false, true, true, false));
        assertThat(fragment.switchAutoJoin.isEnabled(), is(false));
        assertThat(fragment.switchAutoJoin.isChecked(), is(true));

        rule.runOnUiThread(() -> fragment.setTopicAutoJoin(false, true, false, false));
        assertThat(fragment.switchAutoJoin.isEnabled(), is(true));
        assertThat(fragment.switchAutoJoin.isChecked(), is(false));

        rule.runOnUiThread(() -> fragment.setTopicAutoJoin(true, true, false, false));
        assertThat(fragment.switchAutoJoin.isEnabled(), is(true));
        assertThat(fragment.switchAutoJoin.isChecked(), is(true));

        rule.runOnUiThread(() -> fragment.setTopicAutoJoin(true, false, false, false));
        assertThat(fragment.switchAutoJoin.isEnabled(), is(false));
        assertThat(fragment.switchAutoJoin.isChecked(), is(true));

        rule.runOnUiThread(() -> fragment.setTopicAutoJoin(false, false, false, false));
        assertThat(fragment.switchAutoJoin.isEnabled(), is(false));
        assertThat(fragment.switchAutoJoin.isChecked(), is(false));

    }
}