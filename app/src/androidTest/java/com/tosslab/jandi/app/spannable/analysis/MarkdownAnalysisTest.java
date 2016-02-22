package com.tosslab.jandi.app.spannable.analysis;

import android.support.test.runner.AndroidJUnit4;
import android.text.SpannableStringBuilder;
import android.text.style.CharacterStyle;

import com.tosslab.jandi.app.JandiApplication;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Created by tonyjs on 16. 2. 22..
 */
@RunWith(AndroidJUnit4.class)
public class MarkdownAnalysisTest {

    static final String[] TEST_MARKDOWNS = {
            "*Hello World*", "~~GoodBye World~~", "**Thanks World**", "***Sorry World***"
    };

    static final String[] TEST_MARKDOWNS_WITH_WHITESPACE = {
            "*Hello\n World*", "~~GoodBye\n World~~", "**Thanks\n World**", "***Sorry\n World***"
    };

    private MarkdownAnalysis analysis;
    @Before
    public void setUp() throws Exception {
        analysis = new MarkdownAnalysis();
    }

    @Test
    public void testMarkdownAnalysis() throws Exception {
        for (String text : TEST_MARKDOWNS) {
            SpannableStringBuilder ssb = new SpannableStringBuilder(text);
            analysis.analysis(JandiApplication.getContext(), ssb, false);

            CharacterStyle[] spans = ssb.getSpans(0, ssb.length(), CharacterStyle.class);
            boolean find = spans.length == 1;

            assertThat(ssb.toString(), find, is(true));
        }

        for (String text : TEST_MARKDOWNS_WITH_WHITESPACE) {
            SpannableStringBuilder ssb = new SpannableStringBuilder(text);
            analysis.analysis(JandiApplication.getContext(), ssb, false);

            CharacterStyle[] spans = ssb.getSpans(0, ssb.length(), CharacterStyle.class);
            boolean find = spans.length == 1;

            assertThat(ssb.toString(), find, is(false));
        }

    }

}