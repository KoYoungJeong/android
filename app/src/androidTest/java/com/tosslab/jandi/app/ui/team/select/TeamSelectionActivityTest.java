package com.tosslab.jandi.app.ui.team.select;

import android.content.Intent;

import com.tosslab.jandi.app.local.database.JandiDatabaseOpenHelper;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity_;
import com.tosslab.jandi.app.ui.team.select.to.Team;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@Ignore
@RunWith(RobolectricGradleTestRunner.class)
public class TeamSelectionActivityTest {

    private TeamSelectionActivity teamSelectionActivity;

    @Before
    public void setUp() throws Exception {

        BaseInitUtil.initData(Robolectric.application);

        teamSelectionActivity = Robolectric.buildActivity(TeamSelectionActivity_.class).create().start().resume().get();
    }

    @After
    public void tearDown() throws Exception {
        JandiDatabaseOpenHelper.getInstance(Robolectric.application).getWritableDatabase().close();
    }


    @Test
    public void testSelectTeam() throws Exception {

        List<ResAccountInfo.UserTeam> userTeams = JandiAccountDatabaseManager.getInstance(Robolectric.application).getUserTeams();
        ResAccountInfo.UserTeam userTeam = userTeams.get(0);

        // When
        Team team = Team.createTeam(userTeam);
        teamSelectionActivity.teamSelectionModel.updateSelectedTeam(team);
        teamSelectionActivity.teamSelectionPresenter.selectTeam();

        // Then
        Intent nextStartedActivity = Robolectric.shadowOf(teamSelectionActivity).getNextStartedActivity();
        ResAccountInfo.UserTeam selectedTeamInfo = JandiAccountDatabaseManager.getInstance(Robolectric.application).getSelectedTeamInfo();

        assertThat(nextStartedActivity, is(notNullValue()));
        assertThat(nextStartedActivity.getComponent().getClassName(), is(equalTo(MainTabActivity_.class.getName())));

        assertThat(selectedTeamInfo, is(notNullValue()));
        assertThat(selectedTeamInfo.getTeamId(), is(equalTo(team.getTeamId())));
    }
}