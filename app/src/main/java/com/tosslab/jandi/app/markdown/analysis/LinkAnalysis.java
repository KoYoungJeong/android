package com.tosslab.jandi.app.markdown.analysis;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.markdown.rule.MarkdownRule;
import com.tosslab.jandi.app.views.spannable.JandiURLSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkAnalysis implements RuleAnalysis {

    @Override
    public void analysis(SpannableStringBuilder stringBuilder) {
        Pattern pattern = Pattern.compile(MarkdownRule.Link.getRegex());
        Matcher matcher = pattern.matcher(stringBuilder);
        Context context = JandiApplication.getContext();
        int color = context.getResources().getColor(R.color.jandi_accent_color);
        while (matcher.find()) {
            String replaceText = matcher.group(1);
            String link = matcher.group(2);
            int start = matcher.start();
            int end = matcher.end();

            stringBuilder.replace(start, end, replaceText);
            stringBuilder.setSpan(new JandiURLSpan(context, link, color), start, start + replaceText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            matcher.reset(stringBuilder);
        }
    }
}
