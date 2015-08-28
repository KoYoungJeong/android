package com.tosslab.jandi.lib.sprinkler.io;

import android.content.DialogInterface;
import android.content.Intent;
import android.widget.TextView;

import com.tosslab.jandi.lib.sprinkler.SprinklerTestApplication;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by tonyjs on 15. 7. 23..
 */
@Config(
        application = SprinklerTestApplication.class,
        manifest = "src/main/AndroidManifest.xml",
        sdk = 18
)
@RunWith(RobolectricTestRunner.class)
public class TrackerTest {

    Tracker tracker;

    @Before
    public void setup() throws Exception {
        tracker = new Tracker(RuntimeEnvironment.application);
    }

    @Test
    public void testValidateFutureTrack() throws Exception {
        Serializable testData = new HashMap<>();

        boolean validate = tracker.validateFutureTrack(testData);

        System.out.println("validate ? " + validate);

        assertThat(validate, is(false));
    }

    @Test
    public void testMapToJSONFormat() throws Exception {
        Map<String, TextView> testMap1 = new Hashtable<>();
        testMap1.put("h", new TextView(RuntimeEnvironment.application));
        String test1 = tracker.getJSONFormatFromMap(testMap1);

        System.out.println("test1 = " + test1);

        Map<String, String> testMap2 = null;
        String test2 = tracker.getJSONFormatFromMap(testMap2);

        System.out.println("test2 = " + test2);

        Map<String, Object> testMap3 = new HashMap<>();
        testMap3.put("wow", true);
        String test3 = tracker.getJSONFormatFromMap(testMap3);

        System.out.println("test3 = " + test3);

        Map<String, Object> testMap4 = new HashMap<>();
        testMap4.put("wow", tracker);
        String test4 = tracker.getJSONFormatFromMap(testMap4);

        System.out.println("test4 = " + test4);

        Map<String, Object> testMap5 = new HashMap<>();
        testMap5.put("hoho", true);
        testMap5.put("hoho1", "Hello");
        Map<Object, Object> inputMap = new HashMap<>();
        inputMap.put(new Intent(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        testMap5.put("hoho2", inputMap);
        String test5 = tracker.getJSONFormatFromMap(testMap5);

        System.out.println("test5 = " + test5);

        Map<String, String> testMap6 = new Hashtable<>();
        testMap6.put("Hello", "Wow");
        testMap6.put("Hello2", "Wow");
        testMap6.put("Hello45", "Wow");
        String test6 = tracker.getJSONFormatFromMap(testMap6);

        System.out.println("test6 = " + test6);

        Map<String, String> testMap7 = new HashMap<>();
        testMap7.put("Hello", null);
        testMap7.put("Hel2lo", "JJJ");
        testMap7.put("Hel2lo1", null);
        String test7 = tracker.getJSONFormatFromMap(testMap7);

        System.out.println("test7 = " + test7);

        assertNotNull(test1);
        assertNull(test2);
        assertNotNull(test3);
        assertNotNull(test4);
        assertNotNull(test5);
        assertNotNull(test6);
        assertNotNull(test7);
    }

    @Test
    public void testInsert() throws Exception {
        Map<String, Object> identifiers = new HashMap<String, Object>() {
            {
                put("d", "%12312asdr@asdad57575569!@^%@!@&*^%@(!@&''");
            }
        };

        String event = "helloworld";

        String platform = "android";
        Map<String, Object> properties = null;
        final Date date = new Date();
        long time = date.getTime();

        String dateString = new SimpleDateFormat("yyyyMMdd - HH.mm.sss").format(date);
        System.out.println(dateString);

        boolean insert = tracker.insert(event, identifiers, platform, properties, time);

        System.out.println("insert ? " + insert);

        assertTrue(insert);

        event = null;

        insert = tracker.insert(event, identifiers, platform, properties, time);

        System.out.println("insert ? " + insert);

        assertFalse(insert);
    }
}