package com.tosslab.jandi.app.ui.entities.disabled.view;

import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.intent.matcher.IntentMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.ui.entities.chats.model.ChatChooseModel_;
import com.tosslab.jandi.app.ui.entities.chats.view.ChatsChooseFragment;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import setup.BaseInitUtil;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;

@RunWith(AndroidJUnit4.class)
public class DisabledEntityChooseActivityTest {

    @Rule
    public ActivityTestRule<DisabledEntityChooseActivity_> rule = new ActivityTestRule<DisabledEntityChooseActivity_>(DisabledEntityChooseActivity_.class, false, false);
    private DisabledEntityChooseActivity activity;

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
    public void testSetDisabledMembers() throws Throwable {
        List<ChatChooseItem> disabledMembers = getEnabledUsers();
        rule.runOnUiThread(() -> activity.setDisabledMembers(disabledMembers));
        assertThat(activity.adapter.getCount(), is(equalTo(disabledMembers.size())));
    }

    @Test
    public void testOnMemberItemClick() throws Throwable {
        Intents.init();
        // Given
        List<ChatChooseItem> disabledMembers = getEnabledUsers();
        rule.runOnUiThread(() -> activity.setDisabledMembers(disabledMembers));

        // When
        rule.runOnUiThread(() -> activity.onMemberItemClick(0));

        //Then
        Intents.intending(IntentMatchers.hasExtra(ChatsChooseFragment.EXTRA_ENTITY_ID, eq(disabledMembers.get(0).getEntityId())));
        Intents.release();
    }

    private List<ChatChooseItem> getEnabledUsers() {
        return ChatChooseModel_.getInstance_(JandiApplication.getContext()).getUsers();
    }
}