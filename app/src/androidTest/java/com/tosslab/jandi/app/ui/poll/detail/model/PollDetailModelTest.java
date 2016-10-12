package com.tosslab.jandi.app.ui.poll.detail.model;

import com.tosslab.jandi.app.lists.messages.MessageItem;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.PollRepository;
import com.tosslab.jandi.app.local.orm.repositories.StickerRepository;
import com.tosslab.jandi.app.network.client.messages.MessageApi;
import com.tosslab.jandi.app.network.client.teams.poll.PollApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqCreatePoll;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResCreatePoll;
import com.tosslab.jandi.app.network.models.ResPollCommentCreated;
import com.tosslab.jandi.app.network.models.ResPollComments;
import com.tosslab.jandi.app.network.models.ResPollDetail;
import com.tosslab.jandi.app.network.models.ResPollLink;
import com.tosslab.jandi.app.network.models.ResStarredMessage;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.team.TeamInfoLoader;

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

        Observable<ResPollDetail> pollDetailObservable = model.getPollDetailObservable(poll.getId());

        // When
        TestSubscriber<ResPollDetail> testSubscriber = new TestSubscriber<>();
        pollDetailObservable.subscribe(testSubscriber);

        // Then
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();

        ResPollDetail pollDetail = testSubscriber.getOnNextEvents().get(0);
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

        Observable<ResPollLink> pollVoteObservable = model.getPollVoteObservable(poll.getId(), voteSeqs);

        // When
        TestSubscriber<ResPollLink> testSubscriber = new TestSubscriber<>();
        pollVoteObservable.subscribe(testSubscriber);

        // Then
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();

        ResPollLink pollDetail = testSubscriber.getOnNextEvents().get(0);
        System.out.println(pollDetail.toString());
        assertEquals(poll.getId(), pollDetail.getLinkMessage().poll.getId());
    }

    @Test
    public void testGetPollCommentsObservable() throws Exception {
        // Given
        Poll poll = createPollAndGet();

        long pollId = poll.getId();

        Observable<ResPollComments> pollCommentsObservable = model.getPollCommentsObservable(pollId);

        // When
        TestSubscriber<ResPollComments> testSubscriber = new TestSubscriber<>();
        pollCommentsObservable.subscribe(testSubscriber);

        // Then
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();

        ResPollComments resPollComments = testSubscriber.getOnNextEvents().get(0);
        System.out.println(resPollComments.toString());
        assertTrue(resPollComments.getCommentCount() == 0);
    }

    @Test
    public void testGetSendCommentObservable() throws Exception {
        // Given
        Poll poll = createPollAndGet();

        String comment = "Hello World !";
        Observable<ResPollCommentCreated> sendCommentObservable =
                model.getSendCommentObservable(poll.getId(), comment, null);

        // When
        TestSubscriber<ResPollCommentCreated> testSubscriber = new TestSubscriber<>();
        sendCommentObservable.subscribe(testSubscriber);

        // Then
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();

        ResPollCommentCreated resPollCommentCreated = testSubscriber.getOnNextEvents().get(0);
        System.out.println(resPollCommentCreated.toString());
        assertEquals(poll.getId(), resPollCommentCreated.getPoll().getId());
    }

    @Test
    public void testGetSendStickerCommentObservable() throws Exception {
        // Given
        StickerRepository.getRepository();

        Poll poll = createPollAndGet();

        String comment = "Hello World !";
        Observable<ResPollCommentCreated> sendCommentObservable =
                model.getSendStickerCommentObservable(poll.getId(),
                        StickerRepository.DEFAULT_GROUP_ID_DAY, "1", comment, null);

        // When
        TestSubscriber<ResPollCommentCreated> testSubscriber = new TestSubscriber<>();
        sendCommentObservable.subscribe(testSubscriber);

        // Then
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();

        ResPollCommentCreated resPollCommentCreated = testSubscriber.getOnNextEvents().get(0);
        System.out.println(resPollCommentCreated.toString());
        assertEquals(poll.getId(), resPollCommentCreated.getPoll().getId());
    }

    @Test
    public void testStarAndUnStarAction() throws Exception {
        // Given
        Poll poll = createPollAndGet();

        String comment = "Hello World !";
        model.getSendCommentObservable(poll.getId(), comment, null)
                .subscribe(res -> {
                    long messageId = res.getLinkComment().messageId;
                    System.out.println("messageId = " + messageId);
                    Observable<ResStarredMessage> commentStarredObservable =
                            model.getCommentStarredObservable(messageId);
                    // When
                    TestSubscriber<ResStarredMessage> testSubscriber = new TestSubscriber<>();
                    commentStarredObservable.subscribe(testSubscriber);

                    // Then
                    testSubscriber.assertNoErrors();
                    testSubscriber.assertCompleted();

                    ResStarredMessage resStarredMessage = testSubscriber.getOnNextEvents().get(0);
                    System.out.println(resStarredMessage.toString());
                    assertEquals(messageId, resStarredMessage.getMessageId());

                    Observable<ResCommon> commentUnStarredObservable =
                            model.getCommentUnStarredObservable(messageId);

                    // Unstar

                    // When
                    System.out.println("when start");
                    TestSubscriber<ResCommon> testUnStarSubscriber = new TestSubscriber<>();
                    commentUnStarredObservable.subscribe(testUnStarSubscriber);

                    // Then
                    System.out.println("then start");
                    testSubscriber.assertNoErrors();
                    testSubscriber.assertCompleted();

                    ResCommon resCommon = testUnStarSubscriber.getOnNextEvents().get(0);
                    assertNotNull(resCommon);
                });

    }

    @Test
    public void testGetCommentDeleteObservable() throws Exception {
        // Given
        Poll poll = createPollAndGet();

        String comment = "Hello World !";
        model.getSendCommentObservable(poll.getId(), comment, null)
                .subscribe(res -> {
                    long messageId = res.getLinkComment().messageId;
                    System.out.println("messageId = " + messageId);
                    long feedbackId = res.getLinkComment().feedbackId;
                    System.out.println("feedbackId = " + feedbackId);

                    Observable<ResCommon> commentDeleteObservable =
                            model.getCommentDeleteObservable(messageId, feedbackId);

                    TestSubscriber<ResCommon> testSubscriber = new TestSubscriber<>();
                    commentDeleteObservable.subscribe(testSubscriber);

                    testSubscriber.assertNoErrors();
                    testSubscriber.assertCompleted();

                    ResCommon resCommon = testSubscriber.getOnNextEvents().get(0);
                    assertNotNull(resCommon);
                });
    }

    @Test
    public void testGetStickerCommentDeleteObservable() throws Exception {

        // Given
        Poll poll = createPollAndGet();

        String comment = "Hello World !";
        model.getSendCommentObservable(poll.getId(), comment, null)
                .subscribe(res -> {
                    long messageId = res.getLinkComment().messageId;
                    System.out.println("messageId = " + messageId);

                    long feedbackId = res.getLinkComment().feedbackId;

                    Observable<ResCommon> commentDeleteObservable =
                            model.getStickerCommentDeleteObservable(feedbackId, messageId);

                    // When
                    TestSubscriber<ResCommon> testSubscriber = new TestSubscriber<>();
                    commentDeleteObservable.subscribe(testSubscriber);

                    // Then
                    testSubscriber.assertNoErrors();
                    testSubscriber.assertCompleted();

                    ResCommon resCommon = testSubscriber.getOnNextEvents().get(0);
                    assertNotNull(resCommon);
                });
    }

    @Test
    public void testGetPollFinishObservable() throws Exception {
        // Given
        Poll poll = createPollAndGet();

        Observable<ResPollLink> pollFinishObservable = model.getPollFinishObservable(poll.getId());

        // When
        TestSubscriber<ResPollLink> testSubscriber = new TestSubscriber<>();
        pollFinishObservable.subscribe(testSubscriber);

        // Then
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();

        ResPollLink resPollLink = testSubscriber.getOnNextEvents().get(0);
        System.out.println(resPollLink.toString());
        assertTrue(resPollLink != null && resPollLink.getLinkMessage() != null
                    && resPollLink.getLinkMessage().poll != null
                    && "finished".equals(resPollLink.getLinkMessage().poll.getStatus()));
    }

    @Test
    public void testGetPollDeleteObservable() throws Exception {
        // Given
        Poll poll = createPollAndGet();

        Observable<ResPollLink> pollFinishObservable = model.getPollDeleteObservable(poll.getId());

        // When
        TestSubscriber<ResPollLink> testSubscriber = new TestSubscriber<>();
        pollFinishObservable.subscribe(testSubscriber);

        // Then
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();

        ResPollLink resPollLink = testSubscriber.getOnNextEvents().get(0);
        System.out.println(resPollLink.toString());
        assertTrue(resPollLink != null && resPollLink.getLinkMessage() != null
                && resPollLink.getLinkMessage().poll != null
                && "deleted".equals(resPollLink.getLinkMessage().poll.getStatus()));
    }

    @Test
    public void testUpsertPoll() throws Exception {
        // Given
        Poll poll = createPollAndGet();

        // When
        model.upsertPoll(poll);

        // Then
        Poll upsertedPoll = PollRepository.getInstance().getPollById(poll.getId());

        assertEquals(poll.getId(), upsertedPoll.getId());
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