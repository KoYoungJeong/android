package com.tosslab.jandi.app.ui.search.messages.adapter.decorator;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.search.messages.adapter.spannable.NameSpannable;

/**
 * Created by Steve SeongUg Jung on 15. 3. 13..
 */
public class BoldTextDecorator extends SearchTextDecorator {

    private String text;
    private Context context;
    private int textColor;

    public BoldTextDecorator(TextDecorator textDecorator) {
        super(textDecorator);
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTextColor(int textColor) {

        this.textColor = textColor;
    }

    @Override
    public void appendText() {

        int textSize = (int) context.getResources().getDimension(R.dimen.jandi_message_search_item_large_txt_size);
        NameSpannable nameSpannable = new NameSpannable(textSize, textColor);

        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        builder.setSpan(nameSpannable, 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}
