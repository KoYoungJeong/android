package com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.model;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.tosslab.jandi.app.network.client.messages.MessageApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ResStarMentioned;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import rx.Observable;
import rx.observers.TestSubscriber;
import setup.BaseInitUtil;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.*;

/**
 * Created by tonyjs on 2016. 8. 9..
 */
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
        starredListModel = new StarredListModel(() -> new MessageApi(RetrofitBuilder.getInstance()));
    }

    @Test
    public void getStarredListObservable() throws Exception {
        Observable<ResStarMentioned> starredListObservable = starredListModel.getStarredListObservable("", -1, 20);
        TestSubscriber<ResStarMentioned> testSubscriber = new TestSubscriber<>();
        starredListObservable.subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();

        ResStarMentioned resStarMentioned = testSubscriber.getOnNextEvents().get(0);

        Log.i("tony", resStarMentioned.toString());
        assertNotNull(resStarMentioned);
        assertTrue(resStarMentioned.getRecords() != null && !resStarMentioned.getRecords().isEmpty());
        assertThat(resStarMentioned.getRecords().size(), is(lessThan(20)));
    }

    @Test
    public void unregistStarredMessage() throws Exception {

    }

    @Test
    public void getTeamId() throws Exception {

    }

}