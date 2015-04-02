package com.tosslab.jandi.app.utils;

import android.util.Patterns;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class LinkifyUtilTest {

    private final static Pattern simplePattern = Pattern.compile("^(?:([^:/?#]+):)?(?://((?:(([^:@]*):?([^:@]*))?@)?([^:/?#]*)(?::(\\d*))?))?((((?:[^?#/]*/)*)([^?#]*))(?:\\?([^#]*))?(?:#(.*))?)");
    private final static Pattern simple2Pattern = Pattern.compile("^(?:(?![^:@]+:[^:@/]*@)([^:/?#.]+):)?(?://)?((?:(([^:@]*):?([^:@]*))?@)?([^:/?#]*)(?::(\\d*))?)(((/(?:[^?#](?![^?#/]*\\.[^?#/.]+(?:[?#]|$)))*/?)?([^?#/]*))(?:\\?([^#]*))?(?:#(.*))?)");
    private final static Pattern simple3Pattern = Pattern.compile("/^(https?:\\/\\/)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([\\/\\w \\.-]*)*\\/?$/");
    private final static Pattern simple4Pattern = Pattern.compile("(?i)\\b((?:[a-z][\\w-]+:(?:/{1,3}|[a-z0-9%])|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))\n");
    private static final Pattern WEB_URL = Pattern.compile(
            "((?:(http|https|Http|Https|rtsp|Rtsp):\\/\\/(?:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)"
                    + "\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_"
                    + "\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@)?)?"
                    + "(?:" + Patterns.DOMAIN_NAME + ")"
                    + "(?:\\:\\d{1,5})?)" // plus option port number
                    + "(\\/(?:(?:[" + Patterns.GOOD_IRI_CHAR + "\\;\\/\\?\\:\\@\\&\\=\\#\\~\\|"  // plus option query params
                    + "\\-\\.\\+\\!\\*\\'\\(\\)\\,\\_])|(?:\\%[a-fA-F0-9]{2}))*)?"
                    + "(?:\\b|$)"); // and finally, a word boundary or end of

    private static final String[] TEST_URLS = {
            "http://www.11st.co.kr/product/SellerProductDetail.tmall?method=getSellerProductDetail&prdNo=226625559&NaPm=ct=i7zkj32g|ci=2adbadc59954f4756fb831e2ac8df2b7e19e602f|tr=sl|sn=17703|hk=dc84c1d0ea163361654d8c9f7614264d58bcf50b",
            "http://www.lime49.com/",
            "http://lime49.com/",
            "http://lime49.com/blah",
            "http://user@www.lime49.com:81/search.php?q1=test1",
            "http://user:pwd@www.lime49.com:81/search/for/search.php?q1=0&&test1&test2=value#top",
            "daum.net",
            "www.daum.net"
    };

    @Test
    public void testURLMatcher() throws Exception {

        Pattern testPattern = WEB_URL;

        for (String testUrl : TEST_URLS) {
            Matcher matcher = testPattern.matcher(testUrl);
            if (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();

                assertThat(testUrl, start, is(equalTo(0)));
                assertThat(testUrl, end, is(equalTo(testUrl.length())));

            } else {
                fail(testUrl);
            }
        }

        String input = "haha www.naver.com";
        Matcher matcher = testPattern.matcher(input);

        assertThat(matcher.find(), is(equalTo(true)));
        assertThat(matcher.start(), is(equalTo(5)));
        assertThat(matcher.end(), is(equalTo(input.length())));
    }
}