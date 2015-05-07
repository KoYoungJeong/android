package com.tosslab.jandi.app.views.spannable;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.text.style.TextAppearanceSpan;

/**
 * Created by Steve SeongUg Jung on 15. 3. 12..
 */
public class NameSpannable extends TextAppearanceSpan {
    public NameSpannable(int textSize, int textColor) {
        super(null, Typeface.BOLD, textSize, ColorStateList.valueOf(textColor), ColorStateList.valueOf(textColor));
    }
}
