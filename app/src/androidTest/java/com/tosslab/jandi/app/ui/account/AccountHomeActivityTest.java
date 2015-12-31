package com.tosslab.jandi.app.ui.account;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.ui.team.select.to.Team;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import setup.BaseInitUtil;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isSelected;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class AccountHomeActivityTest {

    @Rule
    public ActivityTestRule<AccountHomeActivity_> rule = new ActivityTestRule<>(AccountHomeActivity_.class, true, false);
    private AccountHomeActivity activity;

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
    public void testOnHelpOptionSelect() throws Throwable {
        rule.runOnUiThread(() -> activity.onHelpOptionSelect());

        onView(withText(R.string.jandi_account_home_help))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        pressBack();
    }

    @Test
    public void testOnNameEditClick() throws Throwable {
        rule.runOnUiThread(() -> activity.onNameEditClick());

        onView(withText(activity.tvAccountName.getText().toString()))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        pressBack();
    }

    @Test
    public void testOnEmailEditClick() throws Throwable {
        rule.runOnUiThread(() -> activity.onEmailEditClick());

        onView((withText(R.string.jandi_choose_email)))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testSetTeamInfo() throws Throwable {
        // Given
        List<Team> teams = getTeams();
        ResAccountInfo.UserTeam selectedTeamInfo = getSelectedTeam();

        // When
        rule.runOnUiThread(() -> activity.setTeamInfo(teams, selectedTeamInfo));

        // Then
        onView(withText(selectedTeamInfo.getName()))
                .check(matches(isDisplayed()))
                .check(matches(isSelected()));

        assertThat(activity.teamLayout.getChildCount(), is(teams.size()));
    }

    @Test
    public void testLoadTeamCreateActivity() throws Throwable {
        rule.runOnUiThread(() -> activity.loadTeamCreateActivity());

        onView(withText(R.string.team_create))
                .check(matches(isDisplayed()));

    }

    @Test
    public void testShowNameEditDialog() throws Throwable {
        // given
        String name = "haha";

        // when
        rule.runOnUiThread(() -> activity.showNameEditDialog(name));

        // then
        onView(withText(name))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        pressBack();
    }

    @Test
    public void testSetAccountName() throws Throwable {

        String name = "haha";
        rule.runOnUiThread(() -> activity.setAccountName(name));
        assertThat(activity.tvAccountName.getText().toString(), is(equalTo(name)));
    }

    @Test
    public void testDismissProgressWheel() throws Throwable {
        rule.runOnUiThread(() -> activity.dismissProgressWheel());
        assertThat(activity.progressWheel.isShowing(), is(false));

    }

    @Test
    public void testShowProgressWheel() throws Throwable {
        rule.runOnUiThread(() -> activity.showProgressWheel());
        assertThat(activity.progressWheel.isShowing(), is(true));
    }

    @Test
    public void testMoveSelectedTeam() throws Throwable {
        rule.runOnUiThread(() -> activity.moveSelectedTeam(false));
        assertThat(activity.isFinishing(), is(true));
        String name = AccountRepository.getRepository().getSelectedTeamInfo().getName();
        onView(withText(name))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testMoveEmailEditClick() throws Throwable {
        rule.runOnUiThread(() -> activity.moveEmailEditClick());
        onView(withText(R.string.jandi_choose_email))
                .check(matches(isDisplayed()));

    }

    @Test
    public void testSetUserEmailText() throws Throwable {
        String name = "haha";
        rule.runOnUiThread(() -> activity.setUserEmailText(name));
        assertThat(activity.tvEmail.getText().toString(), is(equalTo(name)));

    }

    @Test
    public void testShowHelloDialog() throws Throwable {
        rule.runOnUiThread(() -> activity.showHelloDialog());
        onView(withText(R.string.jandi_confirm))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        pressBack();
    }

    @Test
    public void testShowTextAlertDialog() throws Throwable {
        String msg = "hello";
        rule.runOnUiThread(() -> activity.showTextAlertDialog(msg, null));
        onView(withText(R.string.jandi_confirm))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        onView(withText(msg))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        pressBack();
    }

    @Test
    public void testInvalidAccess() throws Throwable {
        rule.runOnUiThread(() -> activity.invalidAccess());
        assertThat(activity.isFinishing(), is(true));
    }

    @Test
    public void testShowCheckNetworkDialog() throws Throwable {
        rule.runOnUiThread(() -> activity.showCheckNetworkDialog());
        onView(withText(R.string.err_network))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        pressBack();
    }

    private List<Team> getTeams() {
        List<Team> teams = new ArrayList<>();

        Observable.from(AccountRepository.getRepository().getAccountTeams())
                .map(Team::createTeam)
                .collect(() -> teams, List::add);

        return teams;
    }

    public ResAccountInfo.UserTeam getSelectedTeam() {
        return AccountRepository.getRepository().getSelectedTeamInfo();
    }
}