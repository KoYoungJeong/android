package com.tosslab.jandi.app.utils;

import android.support.test.runner.AndroidJUnit4;
import android.text.SpannableStringBuilder;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.views.spannable.JandiURLSpan;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

@RunWith(AndroidJUnit4.class)
public class LinkifyUtilTest {

    private static final String[] URLS_WITH_SPACE = {
            " http://www.naver.com ",
            " www.naver.com ",
            " www.네이버.com ",
            " http://www.네이버.com ",
            " http://www.naver.com/네이버 ",
            " http://www.naver.com/a?네이버 ",
            " http://www.naver.com/a?value=네이버 ",
            " http://www.naver.com/a?밸류=naver ",
            " http://www.naver.com/a?밸류=네이버 ",
    };

    private static final String DUMMY_TEXT_CRLF_SPACE = " \naslkdj \nasdijhalskdjh";
    private static final String DUMMY_CRLF_SPACE = " \n";

    private static final String[] URLS_WITH_CR_LF_SPACE = {
            DUMMY_CRLF_SPACE + "http://www.naver.com" + DUMMY_TEXT_CRLF_SPACE,
            DUMMY_CRLF_SPACE + "www.naver.com" + DUMMY_TEXT_CRLF_SPACE,
            DUMMY_CRLF_SPACE + "www.네이버.com" + DUMMY_TEXT_CRLF_SPACE,
            DUMMY_CRLF_SPACE + "http://www.네이버.com" + DUMMY_TEXT_CRLF_SPACE,
            DUMMY_CRLF_SPACE + "http://www.naver.com/네이버" + DUMMY_TEXT_CRLF_SPACE,
            DUMMY_CRLF_SPACE + "http://www.naver.com/a?네이버" + DUMMY_TEXT_CRLF_SPACE,
            DUMMY_CRLF_SPACE + "http://www.naver.com/a?value=네이버" + DUMMY_TEXT_CRLF_SPACE,
            DUMMY_CRLF_SPACE + "http://www.naver.com/a?밸류=naver" + DUMMY_TEXT_CRLF_SPACE,
            DUMMY_CRLF_SPACE + "http://www.naver.com/a?밸류=네이버" + DUMMY_TEXT_CRLF_SPACE,

    };
    private static final String[] URLS = {
            "http://www.naver.com",
            "www.naver.com",
            "www.네이버.com",
            "http://www.네이버.com",
            "http://www.naver.com/네이버",
            "http://www.naver.com/a?네이버",
            "http://www.naver.com/a?value=네이버",
            "http://www.naver.com/a?밸류=naver",
            "http://www.naver.com/a?밸류=네이버",
    };

    @Test
    public void testAddLinks() throws Exception {
        {
            int idx = 0;
            for (String url : URLS_WITH_SPACE) {
                SpannableStringBuilder text = new SpannableStringBuilder(url);
                LinkifyUtil.addLinks(JandiApplication.getContext(), text);

                JandiURLSpan[] spans = text.getSpans(0, 1, JandiURLSpan.class);
                assertThat(spans.length, is(equalTo(0)));


                spans = text.getSpans(url.length() - 1, url.length(), JandiURLSpan.class);
                assertThat(spans.length, is(equalTo(0)));

                spans = text.getSpans(1, 2, JandiURLSpan.class);
                assertThat(spans.length, is(equalTo(1)));

                spans = text.getSpans(url.length() - 2, url.length() - 1, JandiURLSpan.class);
                assertThat(spans.length, is(equalTo(1)));

                assertThat(spans[0].getUrl(), is(equalTo(URLS[idx])));

                idx++;
            }
        }

        {
            int idx = 0;
            for (String url : URLS) {
                SpannableStringBuilder text = new SpannableStringBuilder(url);
                LinkifyUtil.addLinks(JandiApplication.getContext(), text);

                JandiURLSpan[] spans = text.getSpans(0, 1, JandiURLSpan.class);
                assertThat(spans.length, is(equalTo(1)));

                spans = text.getSpans(url.length() - 1, url.length(), JandiURLSpan.class);
                assertThat(spans.length, is(equalTo(1)));
                assertThat(spans[0].getUrl(), is(equalTo(URLS[idx])));

                idx++;
            }
        }

        {
            int startLengith = DUMMY_CRLF_SPACE.length();
            int endLength = DUMMY_TEXT_CRLF_SPACE.length();
            int idx = 0;
            for (String url : URLS_WITH_CR_LF_SPACE) {

                int urlLength = url.length();
                SpannableStringBuilder text = new SpannableStringBuilder(url);
                LinkifyUtil.addLinks(JandiApplication.getContext(), text);

                JandiURLSpan[] spans = text.getSpans(0, startLengith, JandiURLSpan.class);
                assertThat(spans.length, is(equalTo(0)));

                spans = text.getSpans(startLengith, startLengith + 1, JandiURLSpan.class);
                assertThat(spans.length, is(equalTo(1)));

                spans = text.getSpans(urlLength - endLength, urlLength, JandiURLSpan.class);
                assertThat(spans.length, is(equalTo(0)));

                spans = text.getSpans(urlLength - endLength - 1, urlLength - endLength, JandiURLSpan.class);
                assertThat(spans.length, is(equalTo(1)));
                assertThat(spans[0].getUrl(), is(equalTo(URLS[idx])));

                idx++;
            }
        }
    }
}