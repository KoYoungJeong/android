package com.tosslab.jandi.lib.sprinkler.io;

import android.util.Pair;

import com.google.gson.Gson;
import com.tosslab.jandi.lib.sprinkler.SprinklerTestApplication;
import com.tosslab.jandi.lib.sprinkler.io.model.Track;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Created by tonyjs on 15. 7. 23..
 */
@Config(
        application = SprinklerTestApplication.class,
        manifest = "src/main/AndroidManifest.xml",
        emulateSdk = 18
)
@RunWith(RobolectricTestRunner.class)
public class FlusherTest {

    private Flusher flusher;
    private Tracker tracker;
    @Before
    public void setup() throws Exception {
        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);

        tracker = new Tracker(Robolectric.application);

        for (int i = 0; i < 30; i++) {
            final int index = i;
            Map<String, Object> identifiers = new HashMap<String, Object>() {
                {
                    put("d", index + "gwleqnwlne casldblaksdb!$@$!@$!@%");
                    put("a", index + "helloWo!RID");
                    put("m", index);
                }
            };

            tracker.insert(index + " event", identifiers, "android", identifiers, new Date().getTime());
        }

        flusher = new Flusher(Robolectric.application);
    }

    @Test
    public void testQuery() throws Exception {
        Pair<Integer, List<Track>> query = flusher.query();
        assertNotNull(query);

        int count = query.first;
        System.out.println("Query count = " + count);

        List<Track> list = query.second;
        int listCount = list.size();
        System.out.println("List count = " + listCount);

        assertEquals(count, listCount);

        for (Track track : list) {
//            System.out.println(track.toString());
        }

        Track track = list.get(0);

        byte[] bytes = new Gson().toJson(track).getBytes("UTF-8");

        System.out.println(new String(bytes));

        assertNotNull(list);
    }

    @Test
    public void testQueryForCount() throws Exception {

        for (int i = 0; i < 50030; i++) {
            final int index = i;
            Map<String, Object> identifiers = new HashMap<String, Object>() {
                {
                    put("d", index + "gwleqnwlne casldblaksdb!$@$!@$!@%");
                    put("a", index + "helloWo!RID");
                    put("m", index);
                }
            };

            tracker.insert(index + " event", identifiers, "android", identifiers, new Date().getTime());
        }

        long time = System.currentTimeMillis();
        Pair<Integer, List<Track>> query = flusher.queryForCount();
        long gap = System.currentTimeMillis() - time;
        System.out.println("query end - " + gap);

        assertNotNull(query);

        int count = query.first;
        System.out.println("Query count = " + count);

        List<Track> list = query.second;
        int listCount = list.size();
        System.out.println("List count = " + listCount);

        assertEquals(count, listCount);

        boolean exceed = listCount >= 50000;
        assertTrue(exceed);

        if (exceed) {
            int first = list.get(0).getIndex();
            int last = list.get(500 - 1).getIndex();

            System.out.println("first - " + first + " last - " + last);

            int deletedRows = flusher.deleteRows(first, last);

            System.out.println("deletedRows = " + deletedRows);

            assertTrue(deletedRows == 500);
        }
    }


    @Test
    public void testStringToMap() throws Exception {
        String jsonString = null;
        Map<String, Object> mapFromString1 = flusher.getMapFromString(jsonString);
        System.out.println(mapFromString1);

        jsonString = "{\"a\":121, \"b\":\"5bsdfbfb^^!@$6\"}";
        Map<String, Object> mapFromString2 = flusher.getMapFromString(jsonString);
        System.out.println(mapFromString2);

        jsonString = "HelloWorld";
        Map<String, Object> mapFromString3 = flusher.getMapFromString(jsonString);

        assertFalse(mapFromString1.size() > 0);
        assertTrue(mapFromString2.size() > 0);
        assertFalse(mapFromString3.size() > 0);
    }

    @Test
    public void testNeedToFlush() throws Exception {
        Pair<Integer, List<Track>> query = flusher.query();

        assertTrue(flusher.needToFlush(query));
    }

    @Test
    public void testIsEndPointAlive() throws Exception {
        boolean alive = flusher.isEndPointAlive();

        assertTrue(alive);
    }

    @Test
    public void testFlush() throws Exception {
        Pair<Integer, List<Track>> query = flusher.query();

        int num = query.first;
        String deviceId = "Sprinkler.with(context).getDefaultProperties().getDeviceId()";
        final List<Track> data = query.second;
        Track lastTrack = data.get(data.size() - 1);
        long lastDate = lastTrack.getTime();
        boolean retry = false;
        ResponseBody body = flusher.flush(retry, num, deviceId, lastDate, data);

        System.out.println(body.toString());

        assertNotNull(body);
    }
}