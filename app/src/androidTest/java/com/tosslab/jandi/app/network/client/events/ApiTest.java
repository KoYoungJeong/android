package com.tosslab.jandi.app.network.client.events;

import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResEventHistory;
import com.tosslab.jandi.app.services.socket.to.SocketFileUnsharedEvent;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Iterator;

import setup.BaseInitUtil;

/**
 * Created by tee on 15. 11. 19..
 */
public class ApiTest {

    long time;

    @Before
    public void setUp() throws Exception {
        time = new Timestamp(System.currentTimeMillis()).getTime() - 24 * 60 * 60 * 1000;
        BaseInitUtil.initData();
//        int defaultTopicId = EntityManager.getInstance().getDefaultTopicId();
//
    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.deleteDummyTopic();
    }

    @Test
    public void testGetEventHistory() throws Exception {
        LogUtil.e("time", time + "");
        LogUtil.e("user id", EntityManager.getInstance().getMe().getId() + "");
        ResEventHistory eventHistory =
                RequestApiManager.getInstance().getEventHistory(time, EntityManager.getInstance().getMe().getId(), null, 300);
        LogUtil.e("event history size", eventHistory.records.size() + "");
        int cnt = 0;
        Iterator<ResEventHistory.EventHistoryInfo> i = eventHistory.records.iterator();
        while (i.hasNext()) {
            ResEventHistory.EventHistoryInfo eventInfo = i.next();
            if (eventInfo instanceof SocketFileUnsharedEvent) {
                cnt++;
                LogUtil.e(eventInfo.toString());
            }
        }
        LogUtil.e("SocketFileUnsharedEvent size", cnt + "");
    }

}