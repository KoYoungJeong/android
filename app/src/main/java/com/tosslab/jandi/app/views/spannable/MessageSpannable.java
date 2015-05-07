package com.tosslab.jandi.app.views.spannable;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.text.style.TextAppearanceSpan;

/**
 * Created by Steve SeongUg Jung on 15. 3. 12..
 */
public class MessageSpannable extends TextAppearanceSpan {
    public MessageSpannable(int textSize, int textColor) {
        super(null, Typeface.NORMAL, textSize, ColorStateList.valueOf(textColor), ColorStateList.valueOf(textColor));
    }
}
