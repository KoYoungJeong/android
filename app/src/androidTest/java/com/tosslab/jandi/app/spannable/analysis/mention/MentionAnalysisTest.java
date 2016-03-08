package com.tosslab.jandi.app.spannable.analysis.mention;

import android.support.test.runner.AndroidJUnit4;
import android.text.SpannableStringBuilder;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.views.spannable.MentionMessageSpannable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collection;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Created by tonyjs on 16. 2. 22..
 */
@RunWith(AndroidJUnit4.class)
public class MentionAnalysisTest {

    static final String TEST_MENTION = "@Tony Hello World";

    private MentionAnalysis analysis;

    @Before
    public void setUp() throws Exception {
        analysis = new MentionAnalysis();

        MentionObject mentionObject = new MentionObject();
        mentionObject.setOffset(0);
        mentionObject.setLength(4);

        Collection<MentionObject> mentions = new ArrayList<>();
        mentions.add(mentionObject);
        MentionAnalysisInfo mentionAnalysisInfo = MentionAnalysisInfo.newBuilder(123, mentions)
                .build();
        analysis.setMentionAnalysisInfo(mentionAnalysisInfo);
    }

    @Test
    public void testMentionAnalysis() throws Exception {
        SpannableStringBuilder ssb = new SpannableStringBuilder(TEST_MENTION);
        analysis.analysis(JandiApplication.getContext(), ssb, false);

        MentionMessageSpannable[] spans = ssb.getSpans(0, ssb.length(), MentionMessageSpannable.class);
        boolean find = spans.length == 1;

        assertThat(ssb.toString(), find, is(true));
    }

}