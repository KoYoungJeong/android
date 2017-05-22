package com.tosslab.jandi.app.spannable.analysis;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Patterns;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.views.spannable.JandiURLSpan;

import java.util.regex.Matcher;

/**
 * Created by tonyjs on 16. 2. 19..
 */
public class WebLinkAnalysis implements RuleAnalysis {
    @Override
    public void analysis(Context context,
                         SpannableStringBuilder spannableStringBuilder, boolean plainText) {
        Matcher matcher = Patterns.WEB_URL.matcher(spannableStringBuilder);
        int color = context.getResources().getColor(R.color.jandi_accent_color);
        while (matcher.find()) {

            int start = matcher.start();
            int end = matcher.end();
//            if (matcher.group(Regex.VALID_URL_GROUP_PROTOCOL) == null) {
//                // skip if protocol is not present and 'extractURLWithoutProtocol' is false
//                // or URL is preceded by invalid character.
//                if (Regex.INVALID_URL_WITHOUT_PROTOCOL_MATCH_BEGIN
//                        .matcher(matcher.group(Regex.VALID_URL_GROUP_BEFORE)).matches()) {
//                    continue;
//                }
//            }
//            String url;
//            int start = matcher.start(Regex.VALID_URL_GROUP_URL);
//            int end;
//            Matcher spaceMatcher = Pattern.compile("( |\n)").matcher(spannableStringBuilder);
//            if (spaceMatcher.find(start + 1)) {
//                end = spaceMatcher.start();
//            } else {
//                end = spannableStringBuilder.length();
//            }

            String url = spannableStringBuilder.subSequence(start, end).toString();

//            Matcher tco_matcher = Regex.VALID_TCO_URL.matcher(url);
//            if (tco_matcher.find()) {
//                // In the case of t.co URLs, don't allow additional path characters.
//                url = tco_matcher.group();
//                end = start + url.length();
//            }

            JandiURLSpan span = new JandiURLSpan(context, url, color);
            spannableStringBuilder.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}
