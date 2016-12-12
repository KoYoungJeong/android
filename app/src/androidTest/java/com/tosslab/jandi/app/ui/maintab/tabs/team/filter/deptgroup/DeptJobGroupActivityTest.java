package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.deptgroup;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.view.View;

import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.DeptJobFragment;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.deptgroup.presenter.DeptJobGroupPresenter;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter.TeamMemberDataView;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import setup.BaseInitUtil;

import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static com.tosslab.jandi.app.ui.maintab.tabs.team.filter.deptgroup.DeptJobGroupActivity.EXTRA_RESULT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@org.junit.runner.RunWith(android.support.test.runner.AndroidJUnit4.class)
public class DeptJobGroupActivityTest {

    @Rule
    public IntentsTestRule<DeptJobGroupActivity> rule = new IntentsTestRule<>(DeptJobGroupActivity.class, false, false);
    private DeptJobGroupActivity activity;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @Before
    public void setUp() throws Exception {
        rule.launchActivity(Henson.with(JandiApplication.getContext())
                .gotoDeptJobGroupActivity()
                .type(DeptJobFragment.EXTRA_TYPE_DEPT)
                .keyword("")
                .build());

        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        activity = rule.getActivity();
    }

    @Test
    public void refreshDataView() throws Exception {
        activity.teamMemberDataView = mock(TeamMemberDataView.class);
        activity.refreshDataView();

        verify(activity.teamMemberDataView).refresh();
    }

    @Test
    public void pickUser_pick() throws Throwable {
        long id = TeamInfoLoader.getInstance().getJandiBot().getId();
        activity.pickMode = true;
        rule.runOnUiThread(() -> activity.pickUser(id));

        intending(hasExtra(EXTRA_RESULT, id));

        assertThat(activity.isFinishing()).isTrue();
    }

    @Test
    public void pickUser_profile() throws Throwable {
        rule.runOnUiThread(() -> activity.pickUser(TeamInfoLoader.getInstance().getJandiBot().getId()));

        intending(hasComponent(MemberProfileActivity.class.getName()));
        intending(hasExtra("memberId", TeamInfoLoader.getInstance().getJandiBot().getId()));
    }

    @Test
    public void updateToggledUser() throws Throwable {
        rule.runOnUiThread(() -> activity.updateToggledUser(1));
        assertThat(activity.vgToggled.getVisibility()).isEqualTo(View.VISIBLE);

        rule.runOnUiThread(() -> activity.updateToggledUser(0));
        assertThat(activity.vgToggled.getVisibility()).isEqualTo(View.GONE);
    }

    @Test
    public void comeWithResult() throws Exception {
        long[] toggledUser = {TeamInfoLoader.getInstance().getJandiBot().getId()};
        activity.comeWithResult(toggledUser);

        intending(hasExtra(EXTRA_RESULT, toggledUser));

        assertThat(activity.isFinishing()).isTrue();
    }

    @Test
    public void onUnselectClick() throws Exception {
        activity.presenter = mock(DeptJobGroupPresenter.class);

        activity.onUnselectClick();

        verify(activity.presenter).onUnselectClick();
    }

    @Test
    public void onAddClick() throws Exception {
        activity.presenter = mock(DeptJobGroupPresenter.class);

        activity.onAddClick();

        verify(activity.presenter).onAddClick();
    }


}