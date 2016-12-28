package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.view.View;

import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.adapter.DeptJobDataView;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.presenter.DeptJobPresenter;
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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@org.junit.runner.RunWith(android.support.test.runner.AndroidJUnit4.class)
public class DeptJobFragmentTest {
    @Rule
    public IntentsTestRule<BaseAppCompatActivity> rule = new IntentsTestRule<>(BaseAppCompatActivity.class, false, false);
    private DeptJobFragment fragment;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @Before
    public void setUp() throws Throwable {
        rule.launchActivity(null);
        BaseAppCompatActivity activity = rule.getActivity();
        fragment = new DeptJobFragment();
        rule.runOnUiThread(() -> activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, fragment)
                .commitNow());
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }

    @Test
    public void refreshDataView() throws Exception {
        fragment.deptJobDataView = mock(DeptJobDataView.class);
        fragment.refreshDataView();
        verify(fragment.deptJobDataView).refresh();
    }

    @Test
    public void moveDirectMessage() throws Throwable {
        long teamId = TeamInfoLoader.getInstance().getTeamId();
        long userId = TeamInfoLoader.getInstance().getJandiBot().getId();
        long roomId = TeamInfoLoader.getInstance().getChatId(userId);
        int lastLinkId = -1;
        rule.runOnUiThread(() -> fragment.moveDirectMessage(teamId, userId, roomId, lastLinkId));

        intending(hasComponent(MessageListV2Activity.class.getName()));
        intending(hasExtra("teamId", teamId));
        intending(hasExtra("entityId", userId));
        intending(hasExtra("roomId", roomId));
        intending(hasExtra("lastReadLinkId", lastLinkId));
    }

    @Test
    public void dismissEmptyView() throws Throwable {
        rule.runOnUiThread(() -> fragment.dismissEmptyView());

        assertThat(fragment.vgEmpty.getVisibility()).isEqualTo(View.GONE);
    }

    @Test
    public void showEmptyView() throws Throwable {
        rule.runOnUiThread(() -> fragment.showEmptyView("가"));

        assertThat(fragment.vgEmpty.getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(fragment.tvEmpty.getText().length()).isGreaterThanOrEqualTo(1);
    }

    @Test
    public void setKeywordObservable() throws Exception {
        fragment.deptJobPresenter = mock(DeptJobPresenter.class);
        fragment.setKeywordObservable(Observable.just("가"));
        verify(fragment.deptJobPresenter).onSearchKeyword(eq("가"));
    }


}