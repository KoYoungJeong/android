package com.tosslab.jandi.app.spannable.rule;

import com.tosslab.jandi.app.spannable.analysis.EmailLinkAnalysis;
import com.tosslab.jandi.app.spannable.analysis.HyperLinkAnalysis;
import com.tosslab.jandi.app.spannable.analysis.TelLinkAnalysis;
import com.tosslab.jandi.app.spannable.analysis.RuleAnalysis;
import com.tosslab.jandi.app.spannable.analysis.MarkdownAnalysis;
import com.tosslab.jandi.app.spannable.analysis.WebLinkAnalysis;
import com.tosslab.jandi.app.spannable.analysis.mention.MentionAnalysis;

public enum SpannableRule {
    HyperLink(HyperLinkAnalysis.class),
    WebLink(WebLinkAnalysis.class),
    TelLink(TelLinkAnalysis.class),
    EmailLink(EmailLinkAnalysis.class),
    Markdown(MarkdownAnalysis.class),
    Mention(MentionAnalysis.class)
    ;

    private final Class<? extends RuleAnalysis> analysisClass;

    SpannableRule(Class<? extends RuleAnalysis> analysisClass) {
        this.analysisClass = analysisClass;
    }

    public Class<? extends RuleAnalysis> getAnalysisClass() {
        return analysisClass;
    }
}
