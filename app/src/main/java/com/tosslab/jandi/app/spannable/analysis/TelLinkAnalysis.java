package com.tosslab.jandi.app.spannable.analysis;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.regex.TelRegex;
import com.tosslab.jandi.app.views.spannable.JandiTelSpan;

import java.util.regex.Matcher;

/**
 * Created by tonyjs on 16. 2. 19..
 */
public class TelLinkAnalysis implements RuleAnalysis {
    @Override
    public void analysis(Context context,
                         SpannableStringBuilder spannableStringBuilder, boolean plainText) {
        Matcher matcher = TelRegex.VALID_TEL_PATTERN.matcher(spannableStringBuilder);

        int color = context.getResources().getColor(R.color.jandi_accent_color);

        while (matcher.find()) {
            String phoneNumber = matcher.group();
            int start = matcher.start();
            int end = matcher.end();

            JandiTelSpan span = new JandiTelSpan(phoneNumber, color);
            spannableStringBuilder.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}
