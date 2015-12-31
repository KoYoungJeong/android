package com.tosslab.jandi.app.markdown.rule;

import com.tosslab.jandi.app.markdown.analysis.LinkAnalysis;
import com.tosslab.jandi.app.markdown.analysis.RuleAnalysis;

public enum MarkdownRule {
    Link("(\\[(.*?)\\])(\\((.*?)\\))", LinkAnalysis.class);

    private final String regex;
    private final Class<? extends RuleAnalysis> analysisClass;

    MarkdownRule(String regex, Class<? extends RuleAnalysis> analysisClass) {
        this.regex = regex;
        this.analysisClass = analysisClass;
    }

    public String getRegex() {
        return regex;
    }

    public Class<? extends RuleAnalysis> getAnalysisClass() {
        return analysisClass;
    }
}
