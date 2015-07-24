package com.tosslab.jandi.lib.sprinkler.io;

import android.util.Pair;

import com.tosslab.jandi.lib.sprinkler.SprinklerTestApplication;
import com.tosslab.jandi.lib.sprinkler.io.model.Track;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

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

    @Before
    public void setup() throws Exception {
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
            System.out.println(track.toString());
        }

        assertNotNull(list);
    }

    @Test
    public void testNeedToFlush() throws Exception {
        Pair<Integer, List<Track>> query = flusher.query();

        assertTrue(flusher.needToFlush(query));
    }

    @Test
    public void testFlush() throws Exception {
        Pair<Integer, List<Track>> query = flusher.query();

        int num = query.first;
        String deviceId = "Sprinkler.with(context).getDefaultProperties().getDeviceId()";
        final List<Track> data = query.second;
        Track lastTrack = data.get(data.size() - 1);
        long lastDate = lastTrack.getTime();
        ResponseBody body = flusher.flush(num, deviceId, lastDate, data);

        assertNotNull(body);
    }
}