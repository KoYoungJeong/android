package com.tosslab.jandi.app.spannable.analysis;

import android.support.test.runner.AndroidJUnit4;
import android.text.SpannableStringBuilder;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.views.spannable.JandiURLSpan;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Created by tonyjs on 16. 2. 22..
 */
@RunWith(AndroidJUnit4.class)
public class WebLinkAnalysisTest {

    static final String[] TEST_WEBLINKS = {
            "www.naver.com", "http://www.naber.com", "https://abc.com", "http://10.2.3.4"
    };

    WebLinkAnalysis analysis;

    @Before
    public void setUp() throws Exception {
        analysis = new WebLinkAnalysis();
    }

    @Test
    public void testWebLinkAnalysis() throws Exception {
        for (String web : TEST_WEBLINKS) {
            SpannableStringBuilder ssb = new SpannableStringBuilder(web);
            analysis.analysis(JandiApplication.getContext(), ssb, false);

            JandiURLSpan[] spans = ssb.getSpans(0, ssb.length(), JandiURLSpan.class);
            boolean find = spans.length == 1;

            assertThat(ssb.toString(), find, is(true));
        }
    }

}