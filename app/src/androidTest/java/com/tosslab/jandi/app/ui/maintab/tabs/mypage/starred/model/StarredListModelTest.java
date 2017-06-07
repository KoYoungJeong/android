package com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.model;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.client.messages.MessageApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.network.models.ResStarMentioned;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import rx.observers.TestSubscriber;
import setup.BaseInitUtil;

import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class StarredListModelTest {

    private StarredListModel starredListModel;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        BaseInitUtil.releaseDatabase();
    }

    @Before
    public void setUp() throws Exception {
        starredListModel = new StarredListModel(() -> new MessageApi(InnerApiRetrofitBuilder.getInstance()));
    }

    @Test
    public void getStarredListObservable() throws Exception {
        TestSubscriber<ResStarMentioned> testSubscriber = new TestSubscriber<>();
        starredListModel.getStarredListObservable("", -1, 20)
                .subscribe(testSubscriber);

        if (testSubscriber.getOnErrorEvents().size() > 0) {
            return;
        }

        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();

        ResStarMentioned resStarMentioned = testSubscriber.getOnNextEvents().get(0);

        assertNotNull(resStarMentioned);
        assertTrue(resStarMentioned.getRecords() != null && !resStarMentioned.getRecords().isEmpty());
        assertThat(resStarMentioned.getRecords().size(), is(lessThan(20)));
    }

}