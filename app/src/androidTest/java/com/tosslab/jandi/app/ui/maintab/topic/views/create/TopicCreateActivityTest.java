package com.tosslab.jandi.app.ui.maintab.topic.views.create;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import com.tosslab.jandi.app.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class TopicCreateActivityTest {

    @Rule
    public ActivityTestRule<TopicCreateActivity_> rule = new ActivityTestRule<TopicCreateActivity_>(TopicCreateActivity_.class);
    private TopicCreateActivity activity;

    @Before
    public void setUp() throws Exception {
        activity = rule.getActivity();

    }

    @Test
    public void testOnHomeOptionClick() throws Exception {

        activity.onHomeOptionClick();

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
    public void testOnPublicTypeClick() throws Throwable {
        rule.runOnUiThread(() -> activity.onPublicTypeClick());

        assertThat(activity.publicCheckView.getVisibility(), is(equalTo(View.VISIBLE)));
        assertThat(activity.privateCheckView.getVisibility(), is(equalTo(View.GONE)));

        assertThat(activity.publicCheckView.isSelected(), is(true));
        assertThat(activity.privateCheckView.isSelected(), is(false));
    }

    @Test
    public void testOnPrivateTypeClick() throws Throwable {
        rule.runOnUiThread(() -> activity.onPrivateTypeClick());

        assertThat(activity.privateCheckView.getVisibility(), is(equalTo(View.VISIBLE)));
        assertThat(activity.publicCheckView.getVisibility(), is(equalTo(View.GONE)));

        assertThat(activity.privateCheckView.isSelected(), is(true));
        assertThat(activity.publicCheckView.isSelected(), is(false));

    }

    @Test
    public void testOnAutojoinClick() throws Throwable {
        {
            // Given
            activity.lastAutoJoin = false;
            rule.runOnUiThread(() -> activity.switchAutojoin.setChecked(false));
            boolean checked = activity.switchAutojoin.isChecked();

            // When
            rule.runOnUiThread(() -> activity.onPublicTypeClick());
            rule.runOnUiThread(() -> activity.onAutojoinClick());

            // Then
            assertThat(activity.switchAutojoin.isChecked(), is(!checked));
            assertThat(activity.lastAutoJoin, is(!checked));
        }
        {
            // Given
            activity.lastAutoJoin = false;
            rule.runOnUiThread(() -> activity.switchAutojoin.setChecked(false));
            boolean checked = activity.switchAutojoin.isChecked();
            // When
            rule.runOnUiThread(() -> activity.onPrivateTypeClick());
            rule.runOnUiThread(() -> activity.onAutojoinClick());

            // Then
            assertThat(activity.switchAutojoin.isChecked(), is(false));
            assertThat(activity.lastAutoJoin, is(checked));
        }


        {
            // Given
            activity.lastAutoJoin = false;
            rule.runOnUiThread(() -> activity.switchAutojoin.setChecked(false));
            rule.runOnUiThread(() -> activity.onPublicTypeClick());

            // When
            rule.runOnUiThread(() -> activity.onAutojoinClick());
            rule.runOnUiThread(() -> activity.onPrivateTypeClick());

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
    }

}