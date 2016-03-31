package com.tosslab.jandi.app.ui.maintab.mypage.model;

import android.support.test.runner.AndroidJUnit4;
import android.util.Pair;

import com.tosslab.jandi.app.network.client.messages.MessageApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitAdapterBuilder;
import com.tosslab.jandi.app.network.models.ResStarMentioned;
import com.tosslab.jandi.app.ui.maintab.mypage.dto.MentionMessage;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;
import setup.BaseInitUtil;

import static org.junit.Assert.assertFalse;

/**
 * Created by tonyjs on 16. 3. 28..
 */
@RunWith(AndroidJUnit4.class)
public class MyPageModelTest {

    private MyPageModel model;

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

        model = new MyPageModel(() -> new MessageApi(RetrofitAdapterBuilder.newInstance()));
    }

    @Test
    public void testGetMentionsObservable() throws Exception {
        Observable<ResStarMentioned> mentionsObservable =
                model.getMentionsObservable(-1, MyPageModel.MENTION_LIST_LIMIT);

        TestSubscriber<ResStarMentioned> testSubscriber = new TestSubscriber<>();
        mentionsObservable.subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();

        ResStarMentioned resStarMentioned = testSubscriber.getOnNextEvents().get(0);
        assertFalse(resStarMentioned.hasMore());

        testSubscriber.assertCompleted();
    }

    @Test
    public void testGetConvertedMentionList() throws Exception {
        Observable<Pair<Boolean, List<MentionMessage>>> pairObservable =
                model.getMentionsObservable(-1, MyPageModel.MENTION_LIST_LIMIT)
                        .concatMap(model::getConvertedMentionObservable);

        TestSubscriber<Pair<Boolean, List<MentionMessage>>> testSubscriber = new TestSubscriber<>();
        pairObservable.subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();

        Pair<Boolean, List<MentionMessage>> pair = testSubscriber.getOnNextEvents().get(0);
        assertFalse(pair.first);

        testSubscriber.assertCompleted();
    }
}