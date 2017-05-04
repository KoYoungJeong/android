package com.tosslab.jandi.app.spannable;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.tosslab.jandi.app.spannable.analysis.RuleAnalysis;
import com.tosslab.jandi.app.spannable.analysis.mention.MentionAnalysis;
import com.tosslab.jandi.app.spannable.analysis.mention.MentionAnalysisInfo;
import com.tosslab.jandi.app.spannable.rule.SpannableRule;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

public class SpannableLookUp {
    public static final String TAG = SpannableLookUp.class.getSimpleName();

    private SpannableStringBuilder stringBuilder;
    private List<Pair<RuleAnalysis, Boolean>> ruleAnalysisList = new ArrayList<>();

    private SpannableLookUp(String text) {
        if (!TextUtils.isEmpty(text)) {
            stringBuilder = new SpannableStringBuilder(text);
        } else {
            stringBuilder = new SpannableStringBuilder("");
        }
    }

    private SpannableLookUp(SpannableStringBuilder stringBuilder) {
        this.stringBuilder = stringBuilder;
    }

    public static SpannableLookUp text(String text) {
        return new SpannableLookUp(text);
    }

    public static SpannableLookUp text(SpannableStringBuilder stringBuilder) {
        return new SpannableLookUp(stringBuilder);
    }

    public SpannableLookUp hyperLink(boolean isPlainText) {
        addToLookUpList(SpannableRule.HyperLink.getAnalysisClass(), isPlainText);
        return this;
    }

    public SpannableLookUp markdown(boolean isPlainText) {
        addToLookUpList(SpannableRule.Markdown.getAnalysisClass(), isPlainText);
        return this;
    }

    public SpannableLookUp webLink(boolean isPlainText) {
        addToLookUpList(SpannableRule.WebLink.getAnalysisClass(), isPlainText);
        return this;
    }

    public SpannableLookUp telLink(boolean isPlainText) {
        addToLookUpList(SpannableRule.TelLink.getAnalysisClass(), isPlainText);
        return this;
    }

    public SpannableLookUp emailLink(boolean isPlainText) {
        addToLookUpList(SpannableRule.EmailLink.getAnalysisClass(), isPlainText);
        return this;
    }

    public SpannableLookUp mention(MentionAnalysisInfo mentionAnalysisInfo, boolean isPlainText) {
        try {
            if (mentionAnalysisInfo.getMentions().size() > 0) {
                RuleAnalysis ruleAnalysis = SpannableRule.Mention.getAnalysisClass().newInstance();
                ((MentionAnalysis) ruleAnalysis).setMentionAnalysisInfo(mentionAnalysisInfo);
                ruleAnalysisList.add(0, Pair.create(ruleAnalysis, isPlainText));
            }
        } catch (Exception e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));
        }
        return this;
    }

    private void addToLookUpList(Class<? extends RuleAnalysis> clazzRulAnalysis,
                                 boolean isPlainText) {
        try {
            RuleAnalysis ruleAnalysis = clazzRulAnalysis.newInstance();
            ruleAnalysisList.add(Pair.create(ruleAnalysis, isPlainText));
        } catch (Exception e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));
        }
    }

    public SpannableStringBuilder lookUp(Context context) {

        Observable.from(ruleAnalysisList)
                .subscribe(pair -> {
                    RuleAnalysis analysis = pair.first;
                    Boolean isPlainText = pair.second;

                    analysis.analysis(context, stringBuilder, isPlainText);
                }, throwable -> {
                    LogUtil.e(TAG, Log.getStackTraceString(throwable));
                });

        return stringBuilder;
    }

    public SpannableStringBuilder lookAll(Context context,
                                          MentionAnalysisInfo mentionAnalysisInfo, boolean isPlainText) {

        Observable.from(SpannableRule.values())
                .subscribe(markdownRule -> {
                    try {
                        RuleAnalysis ruleAnalysis = markdownRule.getAnalysisClass().newInstance();
                        if (mentionAnalysisInfo != null && ruleAnalysis instanceof MentionAnalysis) {
                            ((MentionAnalysis) ruleAnalysis).setMentionAnalysisInfo(mentionAnalysisInfo);
                        }
                        ruleAnalysis.analysis(context, stringBuilder, isPlainText);
                    } catch (Exception e) {
                        LogUtil.e(TAG, Log.getStackTraceString(e));
                    }
                }, throwable -> {
                    LogUtil.e(TAG, Log.getStackTraceString(throwable));
                });

        return stringBuilder;
    }
}
