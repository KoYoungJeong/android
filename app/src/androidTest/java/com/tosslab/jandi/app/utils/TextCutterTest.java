package com.tosslab.jandi.app.utils;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Created by tonyjs on 16. 1. 8..
 */
@RunWith(AndroidJUnit4.class)
public class TextCutterTest {

    TextCutter.MaxLengthTextWatcher watcher;

    @Before
    public void setUp() throws Exception {
        watcher = new TextCutter.MaxLengthTextWatcher();
    }

    @Test
    public void testCutText() throws Exception {
        String text = "가나safasㄹf다라 @Tony\u2063123";
//        String text = "가나safasㄹf다라pkojun09@gmail.com";

        CharSequence charSequence = watcher.cutText(text);

        String expectText = "가나safasㄹf다라 ";

        assertEquals(charSequence, expectText);
    }
}