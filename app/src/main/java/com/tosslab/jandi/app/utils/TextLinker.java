package com.tosslab.jandi.app.utils;

import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.widget.TextView;

/**
 * Created by tonyjs on 16. 2. 17..
 */
public class TextLinker {
    private TextView textView;
    private SpannableStringBuilder spannableStringBuilder;

    private TextLinker(TextView textView, String message) {
        this.textView = textView;
        spannableStringBuilder =
                new SpannableStringBuilder(TextUtils.isEmpty(message) ? "" : message);
    }

    public static TextLinker from(TextView textView, String message) {
        return new TextLinker(textView, message);
    }

    public TextLinker markdown() {

        return this;
    }


}
