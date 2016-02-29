package com.tosslab.jandi.app.utils;

import android.text.SpannableStringBuilder;
import android.util.Patterns;

import com.tosslab.jandi.app.utils.regex.Regex;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.JandiRobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;

import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(JandiRobolectricGradleTestRunner.class)
public class LinkifyUtilTest {

    private final static Pattern simplePattern = Pattern.compile("^(?:([^:/?#]+):)?(?://((?:(([^:@]*):?([^:@]*))?@)?([^:/?#]*)(?::(\\d*))?))?((((?:[^?#/]*/)*)([^?#]*))(?:\\?([^#]*))?(?:#(.*))?)");
    private final static Pattern simple2Pattern = Pattern.compile("^(?:(?![^:@]+:[^:@/]*@)([^:/?#.]+):)?(?://)?((?:(([^:@]*):?([^:@]*))?@)?([^:/?#]*)(?::(\\d*))?)(((/(?:[^?#](?![^?#/]*\\.[^?#/.]+(?:[?#]|$)))*/?)?([^?#/]*))(?:\\?([^#]*))?(?:#(.*))?)");
    private final static Pattern simple3Pattern = Pattern.compile("/^(https?:\\/\\/)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([\\/\\w \\.-]*)*\\/?$/");
    private final static Pattern simple4Pattern = Pattern.compile("(?i)\\b((?:[a-z][\\w-]+:(?:/{1,3}|[a-z0-9%])|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))\n");

    private static final String simpleRegex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
    private static final String simpleRegex2 = "(?i)\\b((?:[a-z][\\w-]+:(?:/{1,3}|[a-z0-9%])|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))";

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
            "http://www.lime49.com:81/search.php?q1=test1",
            "http://www.lime49.com:81/search/for/search.php?q1=0&&test1&test2=value#top",
            "http://user@www.lime49.com:81/search.php?q1=test1",
            "http://user:pw@www.lime49.com:81/search/for/search.php?q1=0&&test1&test2=value#top",
            "daum.net",
            "www.daum.net"
    };
    /**
     * 말씀해주신 사항과 마침표 대응도 하도록 하겠습니다.(생각해보니 IP는 마침표가4개...)
     * 링크를 지원가능한 전화번호는 다음과 같습니다
     */
    final String[] PHONE_NUMBER_TEST_TEXT = {
            "021234567",
            "0212345678",
            "0311234567",
            "03112345678",

            "02 123 4567",
            "02 1234 5678",
            "031 123 4567",
            "031 1234 5678",
            "http://www.nave.com" +
                    "02.123.4567 http://www.nave.com",
            "02.1234.4567하하호호호",
            "031.123.4567",
            "031.1234.4567",

            "02-123-4567",
            "02-1234-5678",
            "031-123-4567",
            "031-1234-5678",

            "+121234567",
            "+1101234567",
            "+11012345678",
            "+82101234567",
            "+821012345678",
            "+547101234567",
            "+5471012345678",
            "+547811234567데헤헤헤",
            "+5478101234567",
            "+54781012345678",

            "+110 123 4567",
            "+110 1234 5678",
            "+8210 123 4567",
            "+8210 1234 5678",
            "+54710 123 4567",
            "+54710 1234 5678",
            "+54781 123 4567",
            "+547810 123 4567",
            "+547810 1234 5678",

            "+1 10 123 4567",
            "+1 10 1234 5678",
            "+82 10 123 4567",
            "+82 10 1234 5678",
            "+547 10 123 4567",
            "+547 10 1234 5678",
            "+5478 1 123 4567",
            "+5478 10 123 4567",
            "+5478 10 1234 5678",

            "+110.123.4567",
            "+110.1234.5678 @토니",
            "+8210.123.4567",
            "+8210.1234.5678",
            "+54710.123.4567",
            "+54710.1234.5678",
            "+54781.123.4567",
            "+54781.1234.4567",
            "+547810.123.4567",
            "+547810.1234.5678",

            "+1 10.123.4567",
            "+1 10.1234.5678",
            "+82 10.123.4567",
            "+82 10.1234.5678",
            "+547 10.123.4567",
            "+547 10.1234.5678",
            "+5478 1.123.4567",
            "+5478 1.1234.4567",
            "+5478 10.123.4567",
            "+5478 10.1234.5678",

            "+110-123-4567",
            "+110-1234-5678",
            "+8210-123-4567",
            "+8210-1234-5678",
            "+54710-123-4567",
            "+54710-1234-5678",
            "+547810-123-4567",
            "+547810-1234-5678",

            "+1 10-123-4567",
            "+1 10-1234-5678",
            "+82 10-123-4567",
            "+82 10-1234-5678",
            "+547 10-123-4567",
            "+547 10-1234-5678",
            "+5478 10-123-4567",
            "+5478 10-1234-5678"
    };

    final String[] EMAIL_NUMBER_TEST_TEXT = {
            "steve@tosslab.com",
            "aaa@a.aa",
            "a12@a12.aa"
    };

    @Test
    public void testURLMatcher() throws Exception {

        Pattern testPattern = Regex.VALID_URL_PATTERN;

        for (String testUrl : TEST_URLS) {
            Matcher matcher = testPattern.matcher(testUrl);

            if (matcher.find()) {

                if (matcher.group(Regex.VALID_URL_GROUP_PROTOCOL) == null) {
                    // skip if protocol is not present and 'extractURLWithoutProtocol' is false
                    // or URL is preceded by invalid character.
                    if (Regex.INVALID_URL_WITHOUT_PROTOCOL_MATCH_BEGIN
                            .matcher(matcher.group(Regex.VALID_URL_GROUP_BEFORE)).matches()) {
                        continue;
                    }
                }
                String url = matcher.group(Regex.VALID_URL_GROUP_URL);
                int start = matcher.start(Regex.VALID_URL_GROUP_URL);
                int end = matcher.end(Regex.VALID_URL_GROUP_URL);
                Matcher tco_matcher = Regex.VALID_TCO_URL.matcher(url);
                if (tco_matcher.find()) {
                    // In the case of t.co URLs, don't allow additional path characters.
                    url = tco_matcher.group();
                    end = start + url.length();
                }

                assertThat(testUrl, start, is(equalTo(0)));
                assertThat(testUrl, end, is(equalTo(testUrl.length())));
            }


        }

        {
            Matcher matcher;
            String input = "haha www.naver.com";
            matcher = testPattern.matcher(input);

            assertThat(matcher.find(), is(equalTo(true)));

            String url = matcher.group(Regex.VALID_URL_GROUP_URL);
            int start = matcher.start(Regex.VALID_URL_GROUP_URL);
            int end = matcher.end(Regex.VALID_URL_GROUP_URL);
            Matcher tco_matcher = Regex.VALID_TCO_URL.matcher(url);
            if (tco_matcher.find()) {
                // In the case of t.co URLs, don't allow additional path characters.
                url = tco_matcher.group();
                end = start + url.length();
            }

            assertThat(input, start, is(equalTo(5)));
            assertThat(input, end, is(equalTo(input.length())));

        }

        String[] inputs = {"다음.넷", "daum.넷", "www.daum.넷", "www.다음.net"};

        for (String url : inputs) {
            Matcher matcher = testPattern.matcher(url);
            if (matcher.find()) {
                fail(url);
            }
        }
    }

    @Test
    public void testAddPhoneLinks() throws Exception {

        Observable.from(PHONE_NUMBER_TEST_TEXT)
                .map(SpannableStringBuilder::new)
                .map(builder -> LinkifyUtil.addPhoneLinks(RuntimeEnvironment.application, builder))
                .subscribe(Assert::assertTrue);


    }

    @Test
    public void testAddEmailLinks() throws Exception {
        Observable.from(EMAIL_NUMBER_TEST_TEXT)
                .map(SpannableStringBuilder::new)
                .map(builder -> LinkifyUtil.addEmailLinks(RuntimeEnvironment.application, builder))
                .subscribe(Assert::assertTrue);

    }
}