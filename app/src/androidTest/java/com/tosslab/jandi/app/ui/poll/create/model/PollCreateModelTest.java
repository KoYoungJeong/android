package com.tosslab.jandi.app.ui.poll.create.model;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.client.teams.poll.PollApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqCreatePoll;
import com.tosslab.jandi.app.network.models.ResCreatePoll;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.observers.TestSubscriber;
import setup.BaseInitUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by tonyjs on 16. 7. 8..
 */
@RunWith(AndroidJUnit4.class)
public class PollCreateModelTest {

    private PollCreateModel model;

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
        model = new PollCreateModel(() -> new PollApi(RetrofitBuilder.getInstance()));
    }

    @Test
    public void testHasSubject() throws Exception {
        assertTrue(model.hasSubject("Hello"));
    }

    @Test
    public void testHasEnoughItems() throws Exception {
        assertFalse(model.hasEnoughItems(null));
    }

    @Test
    public void testHasTargetTopic() throws Exception {
        assertFalse(model.hasTargetTopic(-1));
    }

    @Test
    public void testGetCreatePollObservable() throws Exception {
        // Given
        long topicId = TeamInfoLoader.getInstance().getDefaultTopicId();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        ReqCreatePoll reqCreatePoll = ReqCreatePoll.create(topicId, "HiHi", "desc", false, true, calendar.getTime(), Arrays.asList("a,b,c".split(",")));
        System.out.println(reqCreatePoll.toString());

        // When
        Observable<ResCreatePoll> createPollObservable = model.getCreatePollObservable(reqCreatePoll);
        TestSubscriber<ResCreatePoll> testSubscriber = new TestSubscriber<>();
        createPollObservable.subscribe(testSubscriber);

        // Then
        ResCreatePoll resCreatePoll = testSubscriber.getOnNextEvents().get(0);
        assertNotNull(resCreatePoll);
        assertNotNull(resCreatePoll.getLinkMessage());
        assertNotNull(resCreatePoll.getLinkMessage().poll);
        assertTrue(resCreatePoll.getLinkMessage().poll.getSubject().equals("HiHi"));
        testSubscriber.assertCompleted();
    }

    @Test
    public void testIsAvailableDueDate() throws Exception {
        // Given
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        // Then
        assertFalse(model.isAvailableDueDate(calendar));
    }

    @Test
    public void testGetFilteredItems() throws Exception {
        // Given
        List<String> expectList = new ArrayList<String>() {
            {
                add("a");
                add("b");
                add("c");
            }
        };

        Map<Integer, String> itemsMap = new HashMap<Integer, String>() {
            {
                put(0, "a");
                put(1, null);
                put(2, "b");
                put(3, "c");
                put(4, null);
            }
        };

        // When
        List<String> filteredItems = model.getFilteredItems(itemsMap);

        // Then

        assertEquals(filteredItems, expectList);
    }
}