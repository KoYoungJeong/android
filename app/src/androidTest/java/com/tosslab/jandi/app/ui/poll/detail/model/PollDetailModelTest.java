package com.tosslab.jandi.app.ui.poll.detail.model;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.messages.MessageApi;
import com.tosslab.jandi.app.network.client.teams.poll.PollApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqCreatePoll;
import com.tosslab.jandi.app.network.models.ResCreatePoll;
import com.tosslab.jandi.app.network.models.ResPollLink;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.poll.detail.dto.PollDetail;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

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

        model = new PollDetailModel(() -> new PollApi(RetrofitBuilder.getInstance()),
                () -> new MessageApi(RetrofitBuilder.getInstance()));
    }

    @Test
    public void testGetPollDetailObservable() throws Exception {
        // Given
        Poll poll = createPollAndGet();

        // When
        Observable<PollDetail> pollDetailObservable =
                model.getPollDetailObservable(poll.getId(), new PollDetail(poll.getId()));

        TestSubscriber<PollDetail> testSubscriber = new TestSubscriber<>();
        pollDetailObservable.subscribe(testSubscriber);

        // Then
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();

        PollDetail pollDetail = testSubscriber.getOnNextEvents().get(0);
        System.out.println(pollDetail.toString());
        assertEquals(poll.getId(), pollDetail.getPoll().getId());
        testSubscriber.assertCompleted();
    }

    @Test
    public void testGetPollVoteObservable() throws Exception {
        // Given
        Poll poll = createPollAndGet();

        Poll.Item item = poll.getItems().iterator().next();

        List<Integer> voteSeqs = Collections.singletonList(item.getSeq());

        // When
        Observable<ResPollLink> pollVoteObservable = model.getPollVoteObservable(poll.getId(), voteSeqs);

        TestSubscriber<ResPollLink> testSubscriber = new TestSubscriber<>();
        pollVoteObservable.subscribe(testSubscriber);

        // Then
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();

        ResPollLink pollDetail = testSubscriber.getOnNextEvents().get(0);
        System.out.println(pollDetail.toString());
        assertEquals(poll.getId(), pollDetail.getLinkMessage().poll.getId());
        testSubscriber.assertCompleted();
    }

    @Test
    public void testGetPollCommentsObservable() throws Exception {
        // Given

        // When

        // Then

    }

    @Test
    public void testGetSendCommentObservable() throws Exception {

    }

    @Test
    public void testGetSendStickerCommentObservable() throws Exception {

    }

    @Test
    public void testGetCommentStarredObservable() throws Exception {

    }

    @Test
    public void testGetCommentUnStarredObservable() throws Exception {

    }

    @Test
    public void testGetCommentDeleteObservable() throws Exception {

    }

    @Test
    public void testGetStickerCommentDeleteObservable() throws Exception {

    }

    @Test
    public void testGetPollFinishObservable() throws Exception {

    }

    @Test
    public void testGetPollDeleteObservable() throws Exception {

    }

    @Test
    public void testUpsertPoll() throws Exception {

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