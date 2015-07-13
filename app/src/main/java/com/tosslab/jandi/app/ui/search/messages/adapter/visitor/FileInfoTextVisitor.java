package com.tosslab.jandi.app.ui.search.messages.adapter.visitor;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResMessageSearch;
import com.tosslab.jandi.app.views.spannable.NameSpannable;

/**
 * Created by Steve SeongUg Jung on 15. 3. 13..
 */
public class FileInfoTextVisitor implements TextVisitor {

    private static final String TYPE_TEXT = "text";
    private static final String TYPE_STICKER = "sticker";
    private Context context;
    private int textColor;

    public FileInfoTextVisitor(Context context, int textColor) {
        this.context = context;
        this.textColor = textColor;
    }

    @Override
    public void visit(SpannableStringBuilder builder, ResMessageSearch.Record record) {
        if (TextUtils.equals(record.getType(), TYPE_TEXT)
                || TextUtils.equals(record.getType(), TYPE_STICKER)) {
            return;
        }

        int textSize = (int) context.getResources().getDimension(R.dimen.jandi_message_search_item_large_txt_size);
        NameSpannable nameSpannable;

        String name = EntityManager.getInstance(context).getEntityById(record.getFileInfo().getWriterId()).getName();
        int start = builder.length();
        nameSpannable = new NameSpannable(textSize, textColor);
        builder.append(name).setSpan(nameSpannable, start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        start = builder.length();
        nameSpannable = new NameSpannable(textSize, textColor);
        builder.append(" | ").setSpan(nameSpannable, start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        start = builder.length();
        nameSpannable = new NameSpannable(textSize, textColor);
        builder.append(record.getFileInfo().getTitle()).setSpan(nameSpannable, start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

    }
}
