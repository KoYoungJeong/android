package com.tosslab.jandi.app.ui.search.messages.adapter.spannable;

import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;

/**
 * Created by Steve SeongUg Jung on 15. 3. 13..
 */
public class HighlightSpannable extends BackgroundColorSpan {
    private final int textColor;

    public HighlightSpannable(int backgroundColor, int textColor) {
        super(backgroundColor);
        this.textColor = textColor;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setColor(textColor);
    }
}
