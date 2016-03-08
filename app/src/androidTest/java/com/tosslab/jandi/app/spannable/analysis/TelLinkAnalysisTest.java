package com.tosslab.jandi.app.spannable.analysis;

import android.support.test.runner.AndroidJUnit4;
import android.text.SpannableStringBuilder;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.views.spannable.JandiTelSpan;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Created by tonyjs on 16. 2. 22..
 */
@RunWith(AndroidJUnit4.class)
public class TelLinkAnalysisTest {

    static final String[] TEST_TELS = {
        "010-3108-2515", "82-10-3108-2515", "01032081515", "028412515"
    };

    TelLinkAnalysis analysis;

    @Before
    public void setUp() throws Exception {
        analysis = new TelLinkAnalysis();
    }

    @Test
    public void testTelLinkAnalysis() throws Exception {
        for (String tel : TEST_TELS) {
            SpannableStringBuilder ssb = new SpannableStringBuilder(tel);
            analysis.analysis(JandiApplication.getContext(), ssb, false);

            JandiTelSpan[] spans = ssb.getSpans(0, ssb.length(), JandiTelSpan.class);
            boolean find = spans.length == 1;

            assertThat(ssb.toString(), find, is(true));
        }
    }

}