package com.tosslab.jandi.app.ui.maintab.tabs.topic.views.create;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.MenuItem;

import com.tosslab.jandi.app.R;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(AndroidJUnit4.class)
public class TopicCreateActivityTest {

    @Rule
    public ActivityTestRule<TopicCreateActivity> rule = new ActivityTestRule<TopicCreateActivity>(TopicCreateActivity.class);
    private TopicCreateActivity activity;

    @Before
    public void setUp() throws Exception {
        activity = rule.getActivity();

    }

    @Test
    public void testOnHomeOptionClick() throws Throwable {

        rule.runOnUiThread(() -> {
            MenuItem mock = mock(MenuItem.class);
            doReturn(android.R.id.home).when(mock).getItemId();
            activity.onOptionsItemSelected(mock);
        });

        assertThat(activity.isFinishing(), is(true));

    }

    @Test
    public void testOnTitleTextChange() throws Throwable {
        // When
        rule.runOnUiThread(() -> activity.tvTitle.setText("ahahaha"));
        // Then
        assertThat(activity.menuCreatTopic.isEnabled(), is(true));
        assertThat(activity.tvTitleCount.getText().toString(), is(equalTo("7/60")));

        // When
        rule.runOnUiThread(() -> activity.tvTitle.setText(""));
        // Then
        assertThat(activity.menuCreatTopic.isEnabled(), is(false));

        assertThat(activity.tvTitleCount.getText().toString(), is(equalTo("0/60")));
    }

    @Test
    public void testOnDescriptionTextChange() throws Throwable {
        rule.runOnUiThread(() -> activity.tvTopicDescription.setText("hahaha"));
        assertThat(activity.tvDescriptionCount.getText().toString(), is(equalTo("6/300")));

        rule.runOnUiThread(() -> activity.tvTopicDescription.setText(""));
        assertThat(activity.tvDescriptionCount.getText().toString(), is(equalTo("0/300")));

    }


    @Test
    public void testOnAutojoinClick() throws Throwable {
        {
            // Given
            activity.lastAutoJoin = false;
            rule.runOnUiThread(() -> activity.switchAutojoin.setChecked(false));
            boolean checked = activity.switchAutojoin.isChecked();

            // When
            rule.runOnUiThread(() -> activity.setTopicType(true));
            rule.runOnUiThread(() -> activity.onAutojoinClick());

            // Then
            assertThat(activity.switchAutojoin.isChecked(), is(!checked));
            assertThat(activity.lastAutoJoin, is(!checked));
        }
        {
            // Given
            activity.lastAutoJoin = false;
            activity.isPublicTopic = false;
            rule.runOnUiThread(() -> activity.switchAutojoin.setChecked(false));
            boolean checked = activity.switchAutojoin.isChecked();
            // When
            rule.runOnUiThread(() -> activity.setTopicType(false));
            rule.runOnUiThread(() -> activity.onAutojoinClick());

            // Then
            assertThat(activity.switchAutojoin.isChecked(), is(false));
            assertThat(activity.lastAutoJoin, is(checked));
        }


        {
            // Given
            activity.lastAutoJoin = false;
            activity.isPublicTopic = true;
            rule.runOnUiThread(() -> activity.switchAutojoin.setChecked(false));
            rule.runOnUiThread(() -> activity.setTopicType(true));

            // When
            rule.runOnUiThread(() -> activity.onAutojoinClick());
            rule.runOnUiThread(() -> activity.setTopicType(false));

            // Then
            assertThat(activity.switchAutojoin.isChecked(), is(false));
            assertThat(activity.lastAutoJoin, is(true));

        }
    }

    @Test
    public void testShowProgressWheel() throws Throwable {
        rule.runOnUiThread(() -> activity.showProgressWheel());
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertThat(activity.progressWheel.isShowing(), is(true));

    }

    @Test
    public void testDismissProgressWheel() throws Throwable {
        rule.runOnUiThread(() -> activity.dismissProgressWheel());
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertThat(activity.progressWheel.isShowing(), is(false));
    }

    @Ignore
    @Test
    public void testCreateTopicSuccess() throws Throwable {
        rule.runOnUiThread(() -> activity.createTopicSuccess(1, 1, "a", true));
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertThat(activity.isFinishing(), is(true));
    }

    @Test
    public void testShowCheckNetworkDialog() throws Throwable {
        rule.runOnUiThread(() -> activity.showCheckNetworkDialog());
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        onView(withText(R.string.err_network))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        pressBack();
    }

}