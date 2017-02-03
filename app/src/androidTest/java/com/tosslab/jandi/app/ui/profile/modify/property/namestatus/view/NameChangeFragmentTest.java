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

import static org.assertj.android.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@org.junit.runner.RunWith(android.support.test.runner.AndroidJUnit4.class)
public class NameChangeFragmentTest {

    @Rule
    public IntentsTestRule<NameStatusActivity> rule = new IntentsTestRule<NameStatusActivity>(NameStatusActivity.class, false, false);
    private NameChangeFragment fragment;

    @Before
    public void setUp() throws Exception {
        rule.launchActivity(Henson.with(InstrumentationRegistry.getTargetContext())
                .gotoNameStatusActivity()
                .type(NameStatusActivity.EXTRA_TYPE_NAME_FOR_TEAM_PROFILE)
                .build());

        NameStatusActivity activity = rule.getActivity();
        fragment = ((NameChangeFragment) activity.getSupportFragmentManager().findFragmentByTag(NameChangeFragment.class.getName()));
        fragment.presenter = mock(NameStatusPresenter.class);
    }

    @Test
    public void updateName() throws Throwable {
        rule.runOnUiThread(() -> fragment.updateName());
        verify(fragment.presenter).updateName(eq(fragment.etName.getText().toString()), eq(-1L));
    }

    @Test
    public void onNameTextChanged() throws Throwable {
        String text = "as";
        rule.runOnUiThread(() -> fragment.onNameTextChanged(text));

        verify(fragment.presenter).onTextChange(eq(text));
    }

    @Test
    public void setTextCount() throws Throwable {
        rule.runOnUiThread(() -> fragment.setTextCount(10));
        assertThat(fragment.tvCount).hasText("10/30");
    }

    @Test
    public void successUpdate() throws Throwable {
        rule.runOnUiThread(() -> fragment.successUpdate());
        assertThat(fragment.getActivity()).isFinishing();
    }

    @Test
    public void setUser() throws Throwable {
        User me = TeamInfoLoader.getInstance().getUser(TeamInfoLoader.getInstance().getMyId());
        rule.runOnUiThread(() -> fragment.setContent(me.getName()));

        assertThat(fragment.etName).hasTextString(me.getName());
    }

}