package com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.model;

import android.support.test.runner.AndroidJUnit4;
import android.util.Pair;

import com.tosslab.jandi.app.network.client.messages.MessageApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.network.models.ResStarMentioned;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.dto.MentionMessage;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import rx.observers.TestSubscriber;
import setup.BaseInitUtil;

import static org.junit.Assert.assertFalse;

/**
 * Created by tonyjs on 16. 3. 28..
 */
@RunWith(AndroidJUnit4.class)
public class MentionListModelTest {

    private MentionListModel model;

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

        model = new MentionListModel(() -> new MessageApi(InnerApiRetrofitBuilder.getInstance()));
    }

    @Test
    public void testGetMentionsObservable() throws Exception {
        TestSubscriber<ResStarMentioned> testSubscriber = new TestSubscriber<>();
        model.getMentionsObservable(-1, MentionListModel.MENTION_LIST_LIMIT)
                .subscribe(testSubscriber);

        if (testSubscriber.getOnErrorEvents().size() > 0) {
            return;
        }

        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();

        ResStarMentioned resStarMentioned = testSubscriber.getOnNextEvents().get(0);
        assertFalse(resStarMentioned.hasMore());

        testSubscriber.assertCompleted();
    }

    @Test
    public void testGetConvertedMentionList() throws Exception {
        TestSubscriber<Pair<Boolean, List<MentionMessage>>> testSubscriber = new TestSubscriber<>();
        model.getMentionsObservable(-1, MentionListModel.MENTION_LIST_LIMIT)
                .concatMap(model::getConvertedMentionObservable)
                .subscribe(testSubscriber);

        if (testSubscriber.getOnErrorEvents().size() > 0) {
            return;
        }

        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();

        Pair<Boolean, List<MentionMessage>> pair = testSubscriber.getOnNextEvents().get(0);
        assertFalse(pair.first);

        testSubscriber.assertCompleted();
    }
}