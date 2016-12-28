package com.tosslab.jandi.app.ui.share.text;

import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.intent.matcher.IntentMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import setup.BaseInitUtil;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

@RunWith(AndroidJUnit4.class)
public class TextShareFragmentTest {

    @Rule
    public ActivityTestRule<BaseAppCompatActivity> rule = new ActivityTestRule<>(BaseAppCompatActivity.class, false, false);
    private TextShareFragment textShareFragment;

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
        rule.launchActivity(null);
        BaseAppCompatActivity activity = rule.getActivity();
        textShareFragment = TextShareFragment.create(activity, "subject", "text");
        activity.getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, textShareFragment)
                .commit();

        await().until(() -> {
            return TextUtils.equals(textShareFragment.tvTeamName.getText().toString(), TeamInfoLoader.getInstance().getTeamName());
        });
    }

    @Test
    public void testFinishOnUiThread() throws Throwable {

        rule.runOnUiThread(textShareFragment::finishOnUiThread);

        assertThat(rule.getActivity().isFinishing(), is(true));
    }

    @Test
    public void testSetTeamName() throws Throwable {
        String teamName = "hello";
        rule.runOnUiThread(() -> textShareFragment.setTeamName(teamName));
        assertThat(textShareFragment.tvTeamName.getText().toString(), is(equalTo(teamName)));
    }

    @Test
    public void testSetRoomName() throws Throwable {
        String teamName = "hello";
        rule.runOnUiThread(() -> textShareFragment.setRoomName(teamName));
        assertThat(textShareFragment.tvRoomName.getText().toString(), is(equalTo(teamName)));
    }

    @Test
    public void testSetMentionInfo() throws Throwable {
        long teamId = TeamInfoLoader.getInstance().getTeamId();
        long topicId = TeamInfoLoader.getInstance().getDefaultTopicId();
        rule.runOnUiThread(() -> textShareFragment.setMentionInfo(teamId, topicId, JandiConstants.TYPE_PUBLIC_TOPIC));

        assertThat(textShareFragment.mentionControlViewModel, is(notNullValue()));
    }

    @Test
    public void testShowProgressBar() throws Throwable {
        rule.runOnUiThread(textShareFragment::showProgressBar);
        assertThat(textShareFragment.progressWheel.isShowing(), is(true));
    }

    @Test
    public void testDismissProgressBar() throws Throwable {
        rule.runOnUiThread(textShareFragment::dismissProgressBar);
        assertThat(textShareFragment.progressWheel.isShowing(), is(false));

    }

    @Ignore
    @Test
    public void testMoveEntity() throws Throwable {
        Intents.init();
        long teamId = TeamInfoLoader.getInstance().getTeamId();
        long topicId = TeamInfoLoader.getInstance().getDefaultTopicId();
        rule.runOnUiThread(() -> textShareFragment.moveEntity(teamId, topicId, topicId, JandiConstants.TYPE_PUBLIC_TOPIC));

        Intents.intended(IntentMatchers.hasComponent(MainTabActivity.class.getName()));
        Intents.intended(IntentMatchers.hasComponent(MessageListV2Activity.class.getName()));

        Intents.release();

    }
}