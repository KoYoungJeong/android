package com.tosslab.jandi.app.markdown;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;

import com.tosslab.jandi.app.markdown.analysis.RuleAnalysis;
import com.tosslab.jandi.app.markdown.rule.MarkdownRule;

import rx.Observable;

public class MarkdownLookUp {

    private String text;

    private MarkdownLookUp(String text) {
        if (!TextUtils.isEmpty(text)) {
            this.text = text;
        } else {
            this.text = "";
        }
    }

    public static MarkdownLookUp text(String text) {
        return new MarkdownLookUp(text);
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
