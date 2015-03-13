package com.tosslab.jandi.app.ui.search.messages.adapter.visitor;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessageSearch;
import com.tosslab.jandi.app.ui.search.messages.adapter.spannable.MessageSpannable;

/**
 * Created by Steve SeongUg Jung on 15. 3. 13..
 */
public class ContentTextVisitor implements TextVisitor {

    private Context context;
    private int textColor;

    public ContentTextVisitor(Context context, int textColor) {
        this.context = context;
        this.textColor = textColor;
    }

    @Override
    public void visit(SpannableStringBuilder builder, ResMessageSearch.Record record) {

        if (TextUtils.isEmpty(record.getText())) {
            return;
        }

        int textSize = (int) context.getResources().getDimension(R.dimen.jandi_message_search_item_medium_txt_size);
        MessageSpannable messageSpannable = new MessageSpannable(textSize, textColor);

        int start = builder.length();

        builder.append(record.getText()).setSpan(messageSpannable, start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

    }
}
