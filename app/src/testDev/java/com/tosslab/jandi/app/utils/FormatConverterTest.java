package com.tosslab.jandi.app.utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.JandiRobolectricGradleTestRunner;
import org.robolectric.shadows.ShadowLog;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by justinygchoi on 14. 12. 11..
 */
@RunWith(JandiRobolectricGradleTestRunner.class)
public class FormatConverterTest {
    @Before
    public void setUp() throws Exception {
        System.setProperty("robolectric.logging", "stdout");
        ShadowLog.stream = System.out;
    }

    @Test
    public void testInvaildEmailStringShouldReturnTrue() throws Exception {
        String invaildEmailString = "justin@tosslab";
        assertTrue(FormatConverter.isInvalidEmailString(invaildEmailString));
    }

    @Test
    public void testVaildEmailStringShouldReturnFalse() throws Exception {
        String vaildEmailString = "justin@tosslab.com";
        assertFalse(FormatConverter.isInvalidEmailString(vaildEmailString));
    }
}
