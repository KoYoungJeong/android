package com.tosslab.jandi.app.markdown.analysis;

import android.content.Context;
import android.text.SpannableStringBuilder;

public interface RuleAnalysis {
    void analysis(Context context, SpannableStringBuilder spannableStringBuilder, boolean plainText);
}
