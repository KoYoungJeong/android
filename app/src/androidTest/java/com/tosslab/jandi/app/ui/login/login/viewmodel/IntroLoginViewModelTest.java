package com.tosslab.jandi.app.ui.login.login.viewmodel;

import android.app.FragmentManager;
import android.content.Intent;

import com.tosslab.jandi.app.network.models.ResMyTeam;
import com.tosslab.jandi.app.ui.login.IntroMainActivity;
import com.tosslab.jandi.app.ui.login.IntroMainActivity_;
import com.tosslab.jandi.app.ui.login.login.IntroLoginFragment;
import com.tosslab.jandi.app.ui.login.login.IntroLoginFragment_;
import com.tosslab.jandi.app.ui.team.TeamSelectionActivity_;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowInputMethodManager;
import org.robolectric.shadows.ShadowLog;

import java.util.ArrayList;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class IntroLoginViewModelTest {

    private IntroLoginViewModel introLoginViewModel;
    private IntroLoginFragment introLoginFragment;

    @Before
    public void setUp() throws Exception {

        IntroMainActivity introMainActivity = Robolectric.buildActivity(IntroMainActivity_.class).create().start().resume().get();

        introLoginFragment = IntroLoginFragment_.builder().build();
        FragmentManager fragmentManager = introMainActivity.getFragmentManager();
        fragmentManager.beginTransaction().add(introLoginFragment, null).commit();

        introLoginViewModel = introLoginFragment.introLoginViewModel;

        // Real Connect Dev Server
        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);

        System.setProperty("robolectric.logging", "stdout");
        ShadowLog.stream = System.out;

    }


    @Test
    public void testCreateTeamSucceed() throws Exception {

        // When : Success to create team
        introLoginViewModel.createTeamSucceed();

        // Then 1 : Activity Finish
        assertThat(introLoginFragment.getActivity().isFinishing(), is(true));

        // Then 2 : Tutorial Read flag is True
        assertThat(JandiPreference.getFlagForTutorial(introLoginFragment.getActivity()), is(true));
    }

    @Test
    public void testCreateTeamFailed() throws Exception {
        // Cannot test.
    }

    @Test
    public void testGetTeamListSucceed() throws Exception {

        // Given : Mock Team Info
        ResMyTeam resMyTeam = new ResMyTeam();
        resMyTeam.teamList = new ArrayList<ResMyTeam.Team>();
        ResMyTeam.Team team = new ResMyTeam.Team();
        team.name = "testName";
        team.status = "testStatus";
        team.t_domain = "testDomain";
        team.type = "testType";
        team.teamId = 1;
        resMyTeam.teamList.add(team);

        // When : call to success to get team list
        introLoginViewModel.loginSuccess("steve@tosslab.com");

        // then : started TeamSelectionActivity
        ShadowActivity shadowActivity = Robolectric.shadowOf(introLoginFragment.getActivity());
        Intent nextStartedActivity = shadowActivity.getNextStartedActivity();
        assertThat(nextStartedActivity.getComponent().getClassName(), is(equalTo(TeamSelectionActivity_.class.getName())));

    }

    @Test
    public void testMoveToTeamSelectionActivity() throws Exception {

        // Given : Mock Team Info
        ResMyTeam resMyTeam = new ResMyTeam();
        resMyTeam.teamList = new ArrayList<ResMyTeam.Team>();
        ResMyTeam.Team team = new ResMyTeam.Team();
        team.name = "testName";
        team.status = "testStatus";
        team.t_domain = "testDomain";
        team.type = "testType";
        team.teamId = 1;
        resMyTeam.teamList.add(team);


        // When : move team select activity
        introLoginViewModel.moveToTeamSelectionActivity("steve@tosslab.com");

        // then : started TeamSelectionActivity
        ShadowActivity shadowActivity = Robolectric.shadowOf(introLoginFragment.getActivity());
        Intent nextStartedActivity = shadowActivity.getNextStartedActivity();
        assertThat(nextStartedActivity.getComponent().getClassName(), is(equalTo(TeamSelectionActivity_.class.getName())));

    }

    @Test
    public void testGetTeamListFailed() throws Exception {

        // cannot test
    }

    @Test
    public void testSetReadFlagForTutorial() throws Exception {
        // cannot test

        // When : set read flag
        introLoginViewModel.setReadFlagForTutorial();

        // Then : flag is true
        assertThat(JandiPreference.getFlagForTutorial(introLoginFragment.getActivity()), is(true));
    }

    @Test
    public void testShowTeamTeamCreationFragment() throws Exception {
        // test is not meaning
    }

    @Test
    public void testStartLogin() throws Exception {
        // test is not meaning
    }

    @Test
    public void testHideKeypad() throws Exception {

        // when : hide keypad
        introLoginViewModel.hideKeypad();

        // then : check to hide keypad
        ShadowInputMethodManager shadowInputMethodManager = Robolectric.shadowOf(introLoginViewModel.imm);
        assertThat(shadowInputMethodManager.isSoftInputVisible(), is(false));

    }
}