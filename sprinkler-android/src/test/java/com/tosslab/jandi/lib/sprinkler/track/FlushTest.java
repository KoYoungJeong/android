package com.tosslab.jandi.lib.sprinkler.track;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by tonyjs on 15. 7. 22..
 */
@Config(
        application = SprinklerTestApplication.class,
        manifest = "src/main/AndroidManifest.xml",
        emulateSdk = 18
)
@RunWith(RobolectricTestRunner.class)
public class FlushTest {
    @Test
    public void testConvert() {
        Track track = new Track(1, null, null, null, null, 0);

        Gson gson = new Gson();

        String trackToSting = gson.toJson(track);

        System.out.println(trackToSting);
    }
}
