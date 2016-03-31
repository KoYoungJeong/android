package com.tosslab.jandi.app.ui.maintab.teams.model;

import android.support.test.runner.AndroidJUnit4;
import android.util.Pair;

import com.tosslab.jandi.app.local.orm.repositories.BadgeCountRepository;
import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.invitation.InvitationApi;
import com.tosslab.jandi.app.network.client.main.LeftSideApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitAdapterBuilder;
import com.tosslab.jandi.app.ui.team.select.to.Team;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;
import setup.BaseInitUtil;

import static org.junit.Assert.assertTrue;

/**
 * Created by tonyjs on 16. 3. 28..
 */
@RunWith(AndroidJUnit4.class)
public class TeamsModelTest {

    private TeamsModel model;

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

        model = new TeamsModel(() -> new AccountApi(RetrofitAdapterBuilder.newInstance()),
                () -> new LeftSideApi(RetrofitAdapterBuilder.newInstance()),
                () -> new InvitationApi(RetrofitAdapterBuilder.newInstance()));
    }

    @Test
    public void testGetRefreshAccountInfoObservable() throws Exception {
        Observable<Object> refreshAccountInfoObservable = model.getRefreshAccountInfoObservable();

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        refreshAccountInfoObservable.subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();
    }

    @Test
    public void testGetTeamsObservable() throws Exception {
        Observable<List<Team>> teamsObservable = model.getTeamsObservable(new ArrayList<>());

        TestSubscriber<List<Team>> testSubscriber = new TestSubscriber<>();
        teamsObservable.subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();

        List<Team> teams = testSubscriber.getOnNextEvents().get(0);

        assertTrue(teams.size() > 0);
    }

    @Test
    public void testGetPendingTeamsObservable() throws Exception {
        Observable<List<Team>> pendingTeamsObservable = model.getPendingTeamsObservable(new ArrayList<>());

        TestSubscriber<List<Team>> testSubscriber = new TestSubscriber<>();
        pendingTeamsObservable.subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();

        List<Team> teams = testSubscriber.getOnNextEvents().get(0);

        assertTrue(teams.size() <= 0);
    }

    @Test
    public void testGetUpdateBadgeCountObservable() throws Exception {
        Observable<List<Team>> updateBadgeCountObservable =
                model.getTeamsObservable(new ArrayList<>())
                        .concatMap(model::getUpdateBadgeCountObservable);

        TestSubscriber<List<Team>> testSubscriber = new TestSubscriber<>();
        updateBadgeCountObservable.subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();

        assertTrue(BadgeCountRepository.getRepository().getTotalBadgeCount() > 0);
    }

    @Test
    public void testGetCheckSelectedTeamObservable() throws Exception {
        Observable<Pair<Long, List<Team>>> checkSelectedTeamObservable =
                model.getTeamsObservable(new ArrayList<>())
                        .concatMap(model::getCheckSelectedTeamObservable);

        TestSubscriber<Pair<Long, List<Team>>> testSubscriber = new TestSubscriber<>();
        checkSelectedTeamObservable.subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();

        Pair<Long, List<Team>> pair = testSubscriber.getOnNextEvents().get(0);
        Long selectedTeamId = pair.first;
        assertTrue(selectedTeamId > 0);
    }
}