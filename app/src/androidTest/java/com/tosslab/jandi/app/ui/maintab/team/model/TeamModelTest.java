package com.tosslab.jandi.app.ui.maintab.team.model;

import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.maintab.team.vo.Team;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;
import setup.BaseInitUtil;

import static org.junit.Assert.*;

/**
 * Created by tonyjs on 16. 3. 28..
 */
@RunWith(AndroidJUnit4.class)
public class TeamModelTest {

    private TeamModel model;

    @Before
    public void setup() throws Exception {
        BaseInitUtil.initData();

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
                && TextUtils.equals(team.getOwner().u_email, "ekuvekez-9240@yopmail.com"));
        assertEquals(team.getMembers().size(), 3 + 1 /* JandiBot */);

        teamTestSubscriber.assertCompleted();
    }

    @Test
    public void testGetSearchedMembers() throws Exception {
        testGetTeamObservable();

        List<FormattedEntity> searchedMembers = model.getSearchedMembers("123");

        assertTrue(searchedMembers != null);
        assertEquals(searchedMembers.size(), 0);
    }
}