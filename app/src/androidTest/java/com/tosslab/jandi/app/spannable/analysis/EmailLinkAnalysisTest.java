package com.tosslab.jandi.app.spannable.analysis;

import android.text.SpannableStringBuilder;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.views.spannable.JandiEmailSpan;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Created by tonyjs on 16. 2. 22..
 */
public class EmailLinkAnalysisTest {

    static final String[] TEST_EAMILS = {
            "pkojun09@gmail.com", "a@c.kr", "1234@cc.co"
    };

    EmailLinkAnalysis analysis;

    @Before
    public void setUp() throws Exception {
        analysis = new EmailLinkAnalysis();
    }

    @Test
    public void testEmailAnalysis() throws Exception {
        for (String email : TEST_EAMILS) {
            SpannableStringBuilder ssb = new SpannableStringBuilder(email);
            analysis.analysis(JandiApplication.getContext(), ssb, false);

            JandiEmailSpan[] spans = ssb.getSpans(0, ssb.length(), JandiEmailSpan.class);
            boolean find = spans.length == 1;

            assertThat(ssb.toString(), find, is(true));
        }

    }

}