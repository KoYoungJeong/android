package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.view.View;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.entities.disabled.view.DisabledEntityChooseActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.presenter.TeamMemberPresenter;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import rx.Observable;
import setup.BaseInitUtil;

import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@org.junit.runner.RunWith(android.support.test.runner.AndroidJUnit4.class)
public class TeamMemberFragmentTest {

    @Rule
    public IntentsTestRule<BaseAppCompatActivity> rule = new IntentsTestRule<>(BaseAppCompatActivity.class);
    private TeamMemberFragment fragment;


    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @Before
    public void setUp() throws Throwable {
        BaseAppCompatActivity activity = rule.getActivity();
        rule.runOnUiThread(() -> {
            fragment = new TeamMemberFragment();
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .commitNow();

        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }

    @Test
    public void moveDisabledMembers() throws Throwable {
        rule.runOnUiThread(() -> fragment.moveDisabledMembers());
        intending(hasComponent(DisabledEntityChooseActivity.class.getName()));
    }


    @Test
    public void moveDirectMessage() throws Throwable {
        long teamId = TeamInfoLoader.getInstance().getTeamId();
        long userId = TeamInfoLoader.getInstance().getJandiBot().getId();
        long chatId = TeamInfoLoader.getInstance().getChatId(userId);
        int lastLinkId = -1;

        rule.runOnUiThread(() -> fragment.moveDirectMessage(teamId, userId, chatId, lastLinkId));

        intending(hasComponent(MessageListV2Activity.class.getName()));
        intending(hasExtra("teamId", teamId));
        intending(hasExtra("entityType", JandiConstants.TYPE_DIRECT_MESSAGE));
        intending(hasExtra("entityId", userId));
        intending(hasExtra("roomId", chatId));
        intending(hasExtra("lastReadLinkId", lastLinkId));

        assertThat(rule.getActivity().isFinishing()).isTrue();
    }

    @Test
    public void prgoress_show_dismiss() throws Throwable {
        rule.runOnUiThread(() -> fragment.showPrgoress());
        assertThat(fragment.progressWheel.isShowing()).isTrue();

        rule.runOnUiThread(() -> fragment.dismissProgress());
        assertThat(fragment.progressWheel.isShowing()).isFalse();

    }

    @Test
    public void successToInvitation() throws Throwable {
        rule.runOnUiThread(() -> fragment.successToInvitation());

        assertThat(rule.getActivity().isFinishing()).isTrue();

    }

    @Test
    public void dismissEmptyView() throws Throwable {
        rule.runOnUiThread(() -> fragment.dismissEmptyView());

        assertThat(fragment.vgEmpty.getVisibility()).isEqualTo(View.GONE);

    }

    @Test
    public void setKeywordObservable() throws Exception {
        TeamMemberPresenter spy = getTeamMemberPresenter();

        String testValue = "123";
        fragment.setKeywordObservable(Observable.just(testValue));

        verify(spy).onSearchKeyword(eq(testValue));
    }

    @Test
    public void onAddToggledUser() throws Exception {
        TeamMemberPresenter spy = getTeamMemberPresenter();

        fragment.onAddToggledUser(new long[]{});

        verify(spy).addToggledUser(any());

    }

    @Test
    public void onAddAllUser() throws Exception {

        TeamMemberPresenter spy = getTeamMemberPresenter();

        fragment.onAddAllUser();

        verify(spy).addToggleOfAll();
    }

    @Test
    public void onUnselectAll() throws Exception {
        TeamMemberPresenter spy = getTeamMemberPresenter();

        fragment.onUnselectAll();

        verify(spy).clearToggle();

    }

    @Test
    public void onInvite() throws Exception {
        TeamMemberPresenter spy = getTeamMemberPresenter();

        fragment.onInvite();

        verify(spy).inviteToggle();

    }

    private TeamMemberPresenter getTeamMemberPresenter() {
        TeamMemberPresenter spy = mock(TeamMemberPresenter.class);
        fragment.presenter = spy;
        return spy;
    }
}