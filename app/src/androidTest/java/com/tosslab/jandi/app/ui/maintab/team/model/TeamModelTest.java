package com.tosslab.jandi.app.ui.maintab.team.model;

import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;

import com.tosslab.jandi.app.ui.maintab.team.vo.Team;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import rx.Observable;
import rx.observers.TestSubscriber;
import setup.BaseInitUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by tonyjs on 16. 3. 28..
 */
@RunWith(AndroidJUnit4.class)
public class TeamModelTest {

    private TeamModel model;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        BaseInitUtil.releaseDatabase();
    }

    @Before
    public void setup() throws Exception {

        model = new TeamModel();
    }

    @Test
    public void testGetTeamObservable() throws Exception {
        Observable<Team> teamObservable = model.getTeamObservable();

        TestSubscriber<Team> teamTestSubscriber = new TestSubscriber<>();

        teamObservable.subscribe(teamTestSubscriber);

        teamTestSubscriber.assertNoErrors();

        Team team = teamTestSubscriber.getOnNextEvents().get(0);

        System.out.println(team.toString());
        System.out.println("size = " + team.getMembers().size());

        assertTrue(team.getOwner() != null
                && TextUtils.equals(team.getOwner().getEmail(), "ekuvekez-9240@yopmail.com"));
        assertEquals(team.getMembers().size(), 3 + 1 /* JandiBot */);

        teamTestSubscriber.assertCompleted();
    }
}