package com.tosslab.jandi.app.ui.poll.participants.model;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.teams.poll.PollApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqCreatePoll;
import com.tosslab.jandi.app.network.models.ResCreatePoll;
import com.tosslab.jandi.app.network.models.ResPollParticipants;
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

import static org.junit.Assert.assertTrue;

/**
 * Created by tonyjs on 16. 7. 15..
 */
@RunWith(AndroidJUnit4.class)
public class PollParticipantsModelTest {

    private PollParticipantsModel model;

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

        model = new PollParticipantsModel(() -> new PollApi(RetrofitBuilder.getInstance()));
    }

    @Test
    public void testGetAllParticipantsObservable() throws Exception {
        // Given
        Poll poll = createPollAndGet();

        long teamId = AccountRepository.getRepository().getSelectedTeamId();
        model.pollApi.get().finishPoll(teamId, poll.getId());

        Observable<ResPollParticipants> allParticipantsObservable =
                model.getAllParticipantsObservable(poll.getId());

        // When
        TestSubscriber<ResPollParticipants> testSubscriber = new TestSubscriber<>();
        allParticipantsObservable.subscribe(testSubscriber);

        // Then
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();

        ResPollParticipants pollParticipants = testSubscriber.getOnNextEvents().get(0);
        assertTrue(pollParticipants != null &&
                (pollParticipants.getMemberIds() == null || pollParticipants.getMemberIds().isEmpty()));
    }

    @Test
    public void testGetParticipantsObservable() throws Exception {
        // Given
        Poll poll = createPollAndGet();

        long teamId = AccountRepository.getRepository().getSelectedTeamId();
        model.pollApi.get().finishPoll(teamId, poll.getId());

        Observable<ResPollParticipants> allParticipantsObservable =
                model.getParticipantsObservable(poll.getId(), 0);

        // When
        TestSubscriber<ResPollParticipants> testSubscriber = new TestSubscriber<>();
        allParticipantsObservable.subscribe(testSubscriber);

        // Then
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();

        ResPollParticipants pollParticipants = testSubscriber.getOnNextEvents().get(0);
        assertTrue(pollParticipants != null &&
                (pollParticipants.getMemberIds() == null || pollParticipants.getMemberIds().isEmpty()));
    }

    private Poll createPollAndGet() throws RetrofitException {
        long teamId = AccountRepository.getRepository().getSelectedTeamId();
        long topicId = TeamInfoLoader.getInstance().getDefaultTopicId();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        ReqCreatePoll reqCreatePoll = ReqCreatePoll.create(topicId, "HiHi", "desc", false, true, calendar.getTime(), Arrays.asList("a,b,c".split(",")));
        System.out.println(reqCreatePoll.toString());
        ResCreatePoll resCreatePoll = model.pollApi.get().createPoll(teamId, reqCreatePoll);
        System.out.println(resCreatePoll.toString());
        return resCreatePoll.getLinkMessage().poll;
    }
}