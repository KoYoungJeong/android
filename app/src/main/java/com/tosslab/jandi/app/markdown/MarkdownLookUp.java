package com.tosslab.jandi.app.markdown;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;

import com.tosslab.jandi.app.markdown.analysis.RuleAnalysis;
import com.tosslab.jandi.app.markdown.rule.MarkdownRule;

import rx.Observable;

public class MarkdownLookUp {

    private SpannableStringBuilder stringBuilder;
    private boolean plainText;

    private MarkdownLookUp(String text) {
        if (!TextUtils.isEmpty(text)) {
            stringBuilder = new SpannableStringBuilder(text);
        } else {
            stringBuilder = new SpannableStringBuilder("");
        }
    }

    private MarkdownLookUp(SpannableStringBuilder stringBuilder) {
        this.stringBuilder = stringBuilder;
    }

    public MarkdownLookUp plainText(boolean plainText) {
        this.plainText = plainText;
        return this;
    }

    public static MarkdownLookUp text(String text) {
        return new MarkdownLookUp(text);
    }

    public static MarkdownLookUp text(SpannableStringBuilder stringBuilder) {
        return new MarkdownLookUp(stringBuilder);
    }

    public SpannableStringBuilder lookUp(Context context) {

        Observable.from(MarkdownRule.values())
                .subscribe(markdownRule -> {
                    try {
                        RuleAnalysis analysis = markdownRule.getAnalysisClass().newInstance();
                        analysis.analysis(context, stringBuilder, plainText);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, Throwable::printStackTrace);

        return stringBuilder;
    }
}
