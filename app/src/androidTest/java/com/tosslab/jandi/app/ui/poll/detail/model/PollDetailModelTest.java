package com.tosslab.jandi.app.ui.poll.detail.model;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.teams.poll.PollApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqCreatePoll;
import com.tosslab.jandi.app.network.models.ResCreatePoll;
import com.tosslab.jandi.app.network.models.ResPollDetail;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Calendar;

import rx.Observable;
import rx.observers.TestSubscriber;
import setup.BaseInitUtil;

import static org.junit.Assert.*;

/**
 * Created by tonyjs on 16. 6. 14..
 */
public class PollDetailModelTest {

    private PollDetailModel model;

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

        model = new PollDetailModel(() -> new PollApi(RetrofitBuilder.getInstance()));
    }

    @Test
    public void testGetPollDetailObservable() throws Exception {
        long teamId = AccountRepository.getRepository().getSelectedTeamId();

        long topicId = TeamInfoLoader.getInstance().getDefaultTopicId();

        Calendar calendar = Calendar.getInstance();
        calendar.set(2016, 06, 30, 12, 30, 00);
        ReqCreatePoll reqCreatePoll = ReqCreatePoll.create(topicId, "HiHi", false, true, calendar.getTime(), Arrays.asList("a,b,c".split(",")));
        System.out.println(reqCreatePoll.toString());
        ResCreatePoll poll = model.pollApi.get().createPoll(teamId, reqCreatePoll);
        System.out.println(poll.toString());

        Observable<ResPollDetail> pollDetailObservable = model.getPollDetailObservable(poll.getPoll().getId());

        TestSubscriber<ResPollDetail> testSubscriber = new TestSubscriber<>();
        pollDetailObservable.subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();

        ResPollDetail pollDetail = testSubscriber.getOnNextEvents().get(0);
        System.out.println(pollDetail.toString());
        assertEquals(poll.getPoll().getId(), pollDetail.getPoll().getId());
        testSubscriber.assertCompleted();
    }

}