package com.tosslab.jandi.app.ui.entities.chats.view;

import android.app.Activity;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.intent.matcher.IntentMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.entities.chats.presenter.ChatChoosePresenter;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import setup.BaseInitUtil;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class ChatsChooseFragmentTest {

    @Rule
    public ActivityTestRule<BaseAppCompatActivity> rule = new ActivityTestRule<>(BaseAppCompatActivity.class, false, false);
    private BaseAppCompatActivity activity;
    private ChatsChooseFragment fragment;

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
        activity = rule.getActivity();
        fragment = new ChatsChooseFragment();
        activity.getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, fragment)
                .commit();
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        fragment.presenter = mock(ChatChoosePresenter.class);
    }


    @Test
    public void testMoveChatMessage() throws Throwable {
        Intents.init();

        rule.runOnUiThread(() -> fragment.moveChatMessage(EntityManager.getInstance().getTeamId(), getOtherMemberId()));
        Intents.intended(IntentMatchers.hasComponent(MessageListV2Activity_.class.getName()));
        assertThat(activity.isFinishing(), is(true));

        Intents.release();

    }

    @Test
    public void testOnDisabledMemberActivityResult() throws Throwable {
        Intent intent = new Intent();
        intent.putExtra(ChatsChooseFragment.EXTRA_ENTITY_ID, getOtherMemberId());
        rule.runOnUiThread(() -> fragment.onDisabledMemberActivityResult(Activity.RESULT_OK, intent));

        verify(fragment.presenter).onMoveChatMessage(eq(getOtherMemberId()));

    }

    private long getOtherMemberId() {
        return EntityManager.getInstance().getFormattedUsersWithoutMe().get(0).getId();
    }

    @Test
    public void testOnSearchTextChange() throws Throwable {
        String text = "a";

        fragment.onSearchTextChange(text);

        verify(fragment.presenter).onSearch(eq(text));

    }

    @Test
    public void testInvitationDialogExecution() throws Exception {
        fragment.invitationDialogExecution();
        verify(fragment.presenter).invite();
    }

}