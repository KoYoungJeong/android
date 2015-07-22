package com.tosslab.jandi.lib.sprinkler.track;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by tonyjs on 15. 7. 22..
 */
@Config(
        application = SprinklerTestApplication.class,
        manifest = "src/main/AndroidManifest.xml",
        emulateSdk = 18
)
@RunWith(RobolectricTestRunner.class)
public class TrackDatabaseHelperTest {

    @Test
    public void testInsert() throws Exception {
        TrackDatabaseHelper databaseHelper = TrackDatabaseHelper.getInstance(Robolectric.application);

        Map<String, String> identifiers = new HashMap<String, String>() {
            {
                put("d", "%12312asdr@asdad57575569!@^%@!@&*^%@(!@&''");
            }
        };

        String event = "helloworld";

        String identifier = new JSONObject(identifiers).toString();
        System.out.println(identifier);

        String platform = "android";
        String properties = null;
        final Date date = new Date();
        long time = date.getTime();

        String dateString = new SimpleDateFormat("yyyyMMdd - HH.mm.sss").format(date);
        System.out.println(dateString);

        boolean insert = databaseHelper.insert(event, identifier, platform, properties, time);
        System.out.println("insert ? " + insert);
        assertTrue(insert);
    }
}