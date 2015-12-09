package com.tosslab.jandi.app.markdown;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;

import com.tosslab.jandi.app.markdown.analysis.RuleAnalysis;
import com.tosslab.jandi.app.markdown.rule.MarkdownRule;

import rx.Observable;

public class MarkdownLookup {

    private String text;

    private MarkdownLookup(String text) {
        if (!TextUtils.isEmpty(text)) {
            this.text = text;
        } else {
            this.text = "";
        }
    }

    public static MarkdownLookup text(String text) {
        return new MarkdownLookup(text);
    }

    public SpannableStringBuilder lookUp(Context context) {
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(text);

        Observable.from(MarkdownRule.values())
                .subscribe(markdownRule -> {
                    try {
                        RuleAnalysis analysis = markdownRule.getAnalysisClass().newInstance();
                        analysis.analysis(context, stringBuilder);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, Throwable::printStackTrace);

        return stringBuilder;
    }
}
