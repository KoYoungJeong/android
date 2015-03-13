package com.tosslab.jandi.app.ui.search.messages.adapter.decorator;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.search.messages.adapter.spannable.MessageSpannable;

/**
 * Created by Steve SeongUg Jung on 15. 3. 13..
 */
public class NormalTextDecorator extends SearchTextDecorator {

    private String text;
    private Context context;
    private int textColor;

    public NormalTextDecorator(TextDecorator textDecorator) {
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

        int textSize = (int) context.getResources().getDimension(R.dimen.jandi_message_search_item_medium_txt_size);
        MessageSpannable nameSpannable = new MessageSpannable(textSize, textColor);

        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        builder.setSpan(nameSpannable, 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}
