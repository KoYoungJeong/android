package com.tosslab.jandi.app.spannable.analysis;

import android.support.test.runner.AndroidJUnit4;
import android.text.SpannableStringBuilder;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.views.spannable.JandiURLSpan;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class HyperLinkAnalysisTest {

    private static final String[] TEST_MARKDOWNS = {
            "[aehe](aaaa)",
            "asdad [aehe](aaaa)",
            "[aehe](aaaa) asdasd sdfjhaslkdfjh",
            "asdada[aehe](aaaa)aasdasd sdfjhaslkdfjh"
    };

    private static final String[] TEST_MARKDOWNS_2 = {
            "[aehe1](aaaa1) [aehe2](aaaa2)"
    };

    private static final String[] TEST_MARKDOWNS_3 = {
            "[aehe1](a\naaa1) [a\nehe2](aaaa2)"
    };
    private RuleAnalysis analysis;

    @Before
    public void setUp() throws Exception {
        analysis = new HyperLinkAnalysis();

    }

    @Test
    public void testHyperLinkAnalysis() throws Exception {
        for (String testMarkdown : TEST_MARKDOWNS) {
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(testMarkdown);
            analysis.analysis(JandiApplication.getContext(), spannableStringBuilder, false);

            int length = spannableStringBuilder.length();
            boolean find = false;
            JandiURLSpan[] spans = spannableStringBuilder.getSpans(0, length, JandiURLSpan.class);
            if (spans.length == 1) {
                find = true;
            }

            assertThat("Text : " + testMarkdown, find, is(true));
        }

        for (String testMarkdown : TEST_MARKDOWNS_2) {
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(testMarkdown);
            analysis.analysis(JandiApplication.getContext(), spannableStringBuilder, false);

            int length = spannableStringBuilder.length();
            boolean find = false;
            JandiURLSpan[] spans = spannableStringBuilder.getSpans(0, length, JandiURLSpan.class);
            if (spans.length == 2) {
                find = true;
            }

            assertThat("Text : " + testMarkdown, find, is(true));
        }

        {
            for (String testMarkdown : TEST_MARKDOWNS_3) {
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(testMarkdown);
                analysis.analysis(JandiApplication.getContext(), spannableStringBuilder, true);

                int length = spannableStringBuilder.length();
                boolean find = false;
                JandiURLSpan[] spans = spannableStringBuilder.getSpans(0, length, JandiURLSpan.class);
                if (spans.length > 0) {
                    find = true;
                }

                assertThat("Text : " + testMarkdown, find, is(false));
            }
        }
    }
}