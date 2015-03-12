package com.tosslab.jandi.app.ui.search.messages.adapter.spannable;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.text.style.TextAppearanceSpan;

/**
 * Created by Steve SeongUg Jung on 15. 3. 12..
 */
public class MainNameSpannable extends TextAppearanceSpan {
    public MainNameSpannable(int textSize) {
        super(null, Typeface.NORMAL, textSize, ColorStateList.valueOf(0xFF404040), ColorStateList.valueOf(0xFF404040));
    }
}
