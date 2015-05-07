package com.tosslab.jandi.app.views.spannable;

import android.content.Context;
import android.text.style.ImageSpan;

/**
 * Created by Steve SeongUg Jung on 15. 3. 13..
 */
public class TypeImageSpannable extends ImageSpan {
    public TypeImageSpannable(Context context, int drawableId) {
        super(context, drawableId, ImageSpan.ALIGN_BASELINE);
    }
}
