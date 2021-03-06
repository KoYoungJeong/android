package com.tosslab.jandi.app.spannable.analysis;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.views.spannable.JandiURLSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HyperLinkAnalysis implements RuleAnalysis {

    private static final Pattern sPattern;

    static {
        sPattern = Pattern.compile("(\\[((?:\\[[^\\]]*\\]|[^\\[\\]])*)\\]\\([ \\t]*()<?((?:\\([^)]*\\)|[^()\\s])*?)>?[ \\t]*((['\"])(.*?)\\6[ \\t]*)?\\))");
    }

    @Override
    public void analysis(Context context, SpannableStringBuilder stringBuilder, boolean plainText) {
        Matcher matcher = sPattern.matcher(stringBuilder);
        int color = context.getResources().getColor(R.color.jandi_accent_color);
        while (matcher.find()) {
            // 노출되는 텍스트 영역
            String replaceText = matcher.group(2);
            // 링크
            String link = matcher.group(4);
            // -->[blahblah](http://blahblah)
            int start = matcher.start();
            // [blahblah](http://blahblah)<--
            int end = matcher.end();

            // [blahblah](http://blahblah) => blahblah
            stringBuilder.replace(start, end, replaceText);

            if (!plainText) {
                // blahblah 에 하이퍼링크 처리
                stringBuilder.setSpan(new JandiURLSpan(context, link, color), start, start + replaceText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            // [blahblah1](http://blahblah1) [blahblah2](http://blahblah2) => blahbla1 [blahblah2](http://blahblah2)
            matcher.reset(stringBuilder);
        }
    }
}
