package com.tosslab.jandi.app.ui.search.messages.adapter.spannable;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.text.style.TextAppearanceSpan;

/**
 * Created by Steve SeongUg Jung on 15. 3. 12..
 */
public class SubNameSpannable extends TextAppearanceSpan {
    public SubNameSpannable(int textSize) {
        super(null, Typeface.NORMAL, textSize, ColorStateList.valueOf(0xFF9A9A9A), ColorStateList.valueOf(0xFF9A9A9A));
    }
}
