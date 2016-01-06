package com.tosslab.jandi.app.ui.maintab.more.view;

import android.content.Intent;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.intent.matcher.IntentMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.text.SpannableStringBuilder;
import android.view.View;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.InvitationDisableCheckEvent;
import com.tosslab.jandi.app.ui.account.AccountHomeActivity_;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.members.MembersListActivity;
import com.tosslab.jandi.app.ui.members.MembersListActivity_;
import com.tosslab.jandi.app.ui.profile.modify.view.ModifyProfileActivity_;
import com.tosslab.jandi.app.ui.settings.SettingsActivity_;
import com.tosslab.jandi.app.ui.starmention.StarMentionListActivity;
import com.tosslab.jandi.app.ui.starmention.StarMentionListActivity_;
import com.tosslab.jandi.app.ui.web.InternalWebActivity_;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.greenrobot.event.EventBus;
import setup.BaseInitUtil;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

@RunWith(AndroidJUnit4.class)
public class MainMoreFragmentTest {

    @Rule
    public ActivityTestRule<BaseAppCompatActivity> rule = new ActivityTestRule<BaseAppCompatActivity>(BaseAppCompatActivity.class, false, false);
    private BaseAppCompatActivity activity;
    private MainMoreFragment fragment;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData();
        rule.launchActivity(null);
        activity = rule.getActivity();

        fragment = MainMoreFragment_.builder().build();
        try {
            rule.runOnUiThread(() -> activity.getSupportFragmentManager().beginTransaction().add(android.R.id.content, fragment).commit());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.clear();
    }

    @Test
    public void testSetVersionButtonVisibility() throws Throwable {
        rule.runOnUiThread(() -> fragment.setVersionButtonVisibility(View.VISIBLE));
        assertThat(fragment.btnUpdateVersion.getVisibility(), is(equalTo(View.VISIBLE)));

        rule.runOnUiThread(() -> fragment.setVersionButtonVisibility(View.GONE));
        assertThat(fragment.btnUpdateVersion.getVisibility(), is(equalTo(View.GONE)));

    }

    @Test
    public void testMoveToProfileActivity() throws Throwable {
        Intents.init();
        rule.runOnUiThread(fragment::moveToProfileActivity);

        Intents.intending(IntentMatchers.hasComponent(ModifyProfileActivity_.class.getName()));
        Intents.release();
    }

    @Test
    public void testMoveToTeamMemberActivity() throws Throwable {

        Intents.init();
        rule.runOnUiThread(fragment::moveToTeamMemberActivity);

        Intents.intending(IntentMatchers.hasComponent(MembersListActivity_.class.getName()));
        Intents.intending(IntentMatchers.hasExtra("type", MembersListActivity.TYPE_MEMBERS_LIST_TEAM));

        Intents.release();
    }

    @Test
    public void testOnInvitationDisableCheck() throws Exception {
        final boolean[] received = {false};
        Object subscriber = new Object() {
            public void onEvent(InvitationDisableCheckEvent event) {
                received[0] = true;
            }
        };
        EventBus.getDefault().register(subscriber);
        fragment.onInvitationDisableCheck();

        assertThat(received[0], is(true));

        EventBus.getDefault().unregister(subscriber);
    }

    @Test
    public void testMoveToAccountActivity() throws Throwable {
        Intents.init();
        rule.runOnUiThread(fragment::moveToAccountActivity);

        Intents.intending(IntentMatchers.hasComponent(AccountHomeActivity_.class.getName()));

        Intents.release();
    }

    @Test
    public void testMoveToSettingActivity() throws Throwable {
        Intents.init();
        rule.runOnUiThread(fragment::moveToSettingActivity);
        Intents.intending(IntentMatchers.hasComponent(SettingsActivity_.class.getName()));
        Intents.release();
    }

    @Test
    public void testLaunchHelpPageOnBrowser() throws Throwable {
        Intents.init();
        rule.runOnUiThread(fragment::launchHelpPageOnBrowser);
        Intents.intending(IntentMatchers.hasComponent(InternalWebActivity_.class.getName()));
        Intents.release();

    }

    @Test
    public void testSetLatestVersion() throws Throwable {
        int latestVersionCode = 19;
        rule.runOnUiThread(() -> fragment.setLatestVersion(latestVersionCode));
        int tag = (int) fragment.btnUpdateVersion.getTag();

        assertThat(tag, is(equalTo(latestVersionCode)));

    }

    @Test
    public void testShowBugReportDialog() throws Throwable {
        SpannableStringBuilder test = new SpannableStringBuilder("test");
        rule.runOnUiThread(() -> fragment.showBugReportDialog(test));

        onView(withText("test"))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @Test
    public void testLaunchHelpPageOnMentioned() throws Throwable {
        Intents.init();
        rule.runOnUiThread(fragment::launchHelpPageOnMentioned);

        Intents.intending(IntentMatchers.hasComponent(StarMentionListActivity_.class.getName()));
        Intents.intending(IntentMatchers.hasExtra("type", StarMentionListActivity.TYPE_MENTION_LIST));
        Intents.release();
    }

    @Test
    public void testLaunchHelpPageOnStarred() throws Throwable {
        Intents.init();
        rule.runOnUiThread(fragment::launchHelpPageOnStarred);
        Intents.intending(IntentMatchers.hasComponent(StarMentionListActivity_.class.getName()));
        Intents.intending(IntentMatchers.hasExtra("type", StarMentionListActivity.TYPE_STAR_LIST));
        Intents.release();
    }

    @Test
    public void testOnClickUserInfoReport() throws Throwable {
        rule.runOnUiThread(fragment::onClickUserInfoReport);
        rule.runOnUiThread(fragment::onClickUserInfoReport);
        rule.runOnUiThread(fragment::onClickUserInfoReport);
        rule.runOnUiThread(fragment::onClickUserInfoReport);
        rule.runOnUiThread(fragment::onClickUserInfoReport);

        onView(withText(R.string.jandi_close))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @Test
    public void testOnClickUpdateVersion() throws Throwable {
        Intents.init();
        rule.runOnUiThread(fragment::onClickUpdateVersion);

        Intents.intending(IntentMatchers.hasAction(Intent.ACTION_VIEW));
        Intents.intending(IntentMatchers.hasData("market://details?id=" + JandiApplication.getContext().getPackageName()));
        Intents.release();
    }

    @Test
    public void testSetJandiVersion() throws Throwable {
        String version = "version....";
        rule.runOnUiThread(() -> fragment.setJandiVersion(version));

        assertThat(fragment.textViewJandiVersion.getText(), is(equalTo(String.format("Version%s", version))));
    }

    @Test
    public void testSetOtherTeamBadgeCount() throws Throwable {
        int badgeCount = 10;
        rule.runOnUiThread(() -> fragment.setOtherTeamBadgeCount(badgeCount));

        onView(withText("10"))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testSetMemberTextWithCount() throws Throwable {
        String memberText = "hello";
        rule.runOnUiThread(() -> fragment.setMemberTextWithCount(memberText));
        onView(withText(memberText))
                .check(matches(isDisplayed()));
    }
}