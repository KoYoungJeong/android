package com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.model;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.teams.poll.PollApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqCreatePoll;
import com.tosslab.jandi.app.network.models.ResCreatePoll;
import com.tosslab.jandi.app.network.models.ResPollList;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Calendar;

import rx.Observable;
import rx.observers.TestSubscriber;
import setup.BaseInitUtil;

import static org.junit.Assert.*;

/**
 * Created by tonyjs on 16. 7. 14..
 */
@RunWith(AndroidJUnit4.class)
public class PollListModelTest {

    private PollListModel model;

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

        model = new PollListModel(() -> new PollApi(RetrofitBuilder.getInstance()));
    }

    @Test
    public void testGetPollListObservable() throws Exception {
        // Given
        createPollAndGet();

        Observable<ResPollList> pollListObservable =
                model.getPollListObservable(PollListModel.DEFAULT_REQUEST_ITEM_COUNT);

        // When
        TestSubscriber<ResPollList> testSubscriber = new TestSubscriber<>();
        pollListObservable.subscribe(testSubscriber);

        // Then
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();

        ResPollList resPollList = testSubscriber.getOnNextEvents().get(0);
        assertTrue(resPollList != null && resPollList.getOnGoing() != null && !resPollList.getOnGoing().isEmpty());
    }

    private Poll createPollAndGet() throws RetrofitException {
        long teamId = AccountRepository.getRepository().getSelectedTeamId();
        long topicId = TeamInfoLoader.getInstance().getDefaultTopicId();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        ReqCreatePoll reqCreatePoll = ReqCreatePoll.create(topicId, "HiHi", false, true, calendar.getTime(), Arrays.asList("a,b,c".split(",")));
        System.out.println(reqCreatePoll.toString());
        ResCreatePoll resCreatePoll = model.pollApi.get().createPoll(teamId, reqCreatePoll);
        System.out.println(resCreatePoll.toString());
        return resCreatePoll.getLinkMessage().poll;
    }
}