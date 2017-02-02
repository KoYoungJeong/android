package com.tosslab.jandi.app.ui.profile.modify.property.namestatus.view;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;

import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.profile.modify.property.namestatus.presenter.NameStatusPresenter;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import setup.BaseInitUtil;

import static org.assertj.android.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@org.junit.runner.RunWith(android.support.test.runner.AndroidJUnit4.class)
public class StatusChangeFragmentTest {

    @Rule
    public IntentsTestRule<NameStatusActivity> rule = new IntentsTestRule<NameStatusActivity>(NameStatusActivity.class, false, false);
    private StatusChangeFragment fragment;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @Before
    public void setUp() throws Exception {

        rule.launchActivity(Henson.with(InstrumentationRegistry.getTargetContext())
                .gotoNameStatusActivity()
                .type(NameStatusActivity.EXTRA_TYPE_STATUS)
                .build());

        NameStatusActivity activity = rule.getActivity();
        fragment = (StatusChangeFragment) activity.getSupportFragmentManager()
                .findFragmentByTag(StatusChangeFragment.class.getName());
        fragment.presenter = mock(NameStatusPresenter.class);
    }

    @Test
    public void updateStatus() throws Throwable {
        rule.runOnUiThread(() -> fragment.updateStatus());

        verify(fragment.presenter).updateStatus(eq(fragment.etStatus.getText().toString()), eq(-1L));
    }
    @Test
    public void onNameTextChanged() throws Throwable {
        rule.runOnUiThread(() -> fragment.onStatusTextChanged("as"));

        verify(fragment.presenter).onTextChange(eq("as"));
    }

    @Test
    public void setTextCount() throws Throwable {
        rule.runOnUiThread(() -> fragment.setTextCount(10));
        assertThat(fragment.tvCount).hasTextString("10/60");
    }

    @Test
    public void successUpdate() throws Throwable {
        rule.runOnUiThread(() -> fragment.successUpdate());
        assertThat(fragment.getActivity()).isFinishing();
    }

    @Test
    public void setUser() throws Throwable {
        User user = TeamInfoLoader.getInstance().getUser(TeamInfoLoader.getInstance().getMyId());
        rule.runOnUiThread(() -> fragment.setContent(user.getStatusMessage()));

        assertThat(fragment.etStatus).hasTextString(user.getStatusMessage());
    }


}