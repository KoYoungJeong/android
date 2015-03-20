package com.tosslab.jandi.app.ui.search.messages.adapter.visitor;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessageSearch;
import com.tosslab.jandi.app.ui.search.messages.adapter.spannable.TypeImageSpannable;

/**
 * Created by Steve SeongUg Jung on 15. 3. 13..
 */
public class TypeTextVisitor implements TextVisitor {

    private Context context;

    public TypeTextVisitor(Context context) {
        this.context = context;
    }

    @Override
    public void visit(SpannableStringBuilder builder, ResMessageSearch.Record record) {
        String type = record.getType();

        ImageSpan imageSpan = null;
        if (TextUtils.equals(type, "file")) {
            imageSpan = new TypeImageSpannable(context, R.drawable.jandi_account_upload);
        } else if (TextUtils.equals(type, "text")) {
        } else if (TextUtils.equals(type, "comment")) {
            imageSpan = new TypeImageSpannable(context, R.drawable.jandi_account_comment);
        }

        if (imageSpan != null) {
            builder.append(" ");
            builder.setSpan(imageSpan, builder.length() - 1, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}
