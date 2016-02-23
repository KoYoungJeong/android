package com.tosslab.jandi.app.spannable.analysis;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.views.spannable.JandiEmailSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tonyjs on 16. 2. 19..
 */
public class EmailLinkAnalysis implements RuleAnalysis {
    private static final Pattern sPattern;

    static {
        sPattern = Pattern.compile("[_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{2,4})");
    }

    @Override
    public void analysis(Context context,
                         SpannableStringBuilder spannableStringBuilder, boolean plainText) {
        Matcher matcher = sPattern.matcher(spannableStringBuilder);

        int color = context.getResources().getColor(R.color.jandi_accent_color);
        while (matcher.find()) {
            String email = matcher.group();
            int start = matcher.start();
            int end = matcher.end();
            JandiEmailSpan jandiEmailSpan = new JandiEmailSpan(email, color);
            spannableStringBuilder.setSpan(jandiEmailSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}
