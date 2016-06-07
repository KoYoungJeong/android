package com.tosslab.jandi.app.network.client.events;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ResEventHistory;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(AndroidJUnit4.class)
public class EventsApiTest {

    private EventsApi eventsApi;
    private long myId;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @Before
    public void setUp() throws Exception {
        eventsApi = new EventsApi(RetrofitBuilder.getInstance());
        myId = TeamInfoLoader.getInstance().getMyId();
    }

    @Test
    public void testGetEventHistory_with_size() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -3);
        int size = 10;
        ResEventHistory message = eventsApi.getEventHistory(calendar.getTimeInMillis(), myId, "message", size);
        assertThat(message).isNotNull();
        assertThat(message.size).isGreaterThanOrEqualTo(0);
        assertThat(message.records.size()).isGreaterThanOrEqualTo(0);

        assertThat(message.size).isLessThanOrEqualTo(size);
        assertThat(message.records.size()).isLessThanOrEqualTo(size);
    }

    @Test
    public void testGetEventHistory_without_size() throws Exception {

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -3);
        ResEventHistory message = eventsApi.getEventHistory(calendar.getTimeInMillis(), myId, "message");
        assertThat(message).isNotNull();
        assertThat(message.size).isGreaterThanOrEqualTo(0);
        assertThat(message.records.size()).isGreaterThanOrEqualTo(0);

    }
}