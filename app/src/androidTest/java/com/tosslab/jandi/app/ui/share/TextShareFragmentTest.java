package com.tosslab.jandi.app.ui.share;

import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.intent.matcher.IntentMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;
import android.view.View;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.intro.IntroActivity_;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity_;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;

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

    @Before
    public void setUp() throws Exception {
        textShareFragment = TextShareFragment_.builder()
                .text("text")
                .subject("subject")
                .build();
        rule.launchActivity(null);
        BaseAppCompatActivity activity = rule.getActivity();
        activity.getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, textShareFragment)
                .commit();

        await().until(() -> {
            return TextUtils.equals(textShareFragment.tvTeamName.getText().toString(), EntityManager.getInstance().getTeamName());
        });
    }

    @Test
    public void testFinishOnUiThread() throws Throwable {

        rule.runOnUiThread(textShareFragment::finishOnUiThread);

        assertThat(rule.getActivity().isFinishing(), is(true));
    }

    @Test
    public void testMoveIntro() throws Throwable {
        Intents.init();

        rule.runOnUiThread(textShareFragment::moveIntro);
        assertThat(rule.getActivity().isFinishing(), is(true));
        Intents.intending(IntentMatchers.hasComponent(IntroActivity_.class.getName()));

        Intents.release();
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
        long teamId = EntityManager.getInstance().getTeamId();
        long topicId = EntityManager.getInstance().getDefaultTopicId();
        rule.runOnUiThread(() -> textShareFragment.setMentionInfo(teamId, topicId, JandiConstants.TYPE_PUBLIC_TOPIC));

        assertThat(textShareFragment.mentionControlViewModel, is(notNullValue()));
    }

    @Test
    public void testShowProgressBar() throws Throwable {
        rule.runOnUiThread(textShareFragment::showProgressBar);
        assertThat(textShareFragment.downloadingProgressBar.getVisibility(), is(equalTo(View.VISIBLE)));
    }

    @Test
    public void testDismissProgressBar() throws Throwable {
        rule.runOnUiThread(textShareFragment::dismissProgressBar);
        assertThat(textShareFragment.downloadingProgressBar.getVisibility(), is(equalTo(View.GONE)));

    }

    @Ignore
    @Test
    public void testMoveEntity() throws Throwable {
        Intents.init();
        long teamId = EntityManager.getInstance().getTeamId();
        long topicId = EntityManager.getInstance().getDefaultTopicId();
        rule.runOnUiThread(() -> textShareFragment.moveEntity(teamId, topicId, JandiConstants.TYPE_PUBLIC_TOPIC));

        Intents.intended(IntentMatchers.hasComponent(MainTabActivity_.class.getName()));
        Intents.intended(IntentMatchers.hasComponent(MessageListV2Activity_.class.getName()));

        Intents.release();

    }
}