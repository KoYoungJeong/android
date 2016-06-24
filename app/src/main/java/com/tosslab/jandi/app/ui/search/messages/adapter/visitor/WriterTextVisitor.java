package com.tosslab.jandi.app.ui.search.messages.adapter.visitor;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessageSearch;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.views.spannable.NameSpannable;

public class WriterTextVisitor implements TextVisitor {

    private Context context;
    private int textColor;

    public WriterTextVisitor(Context context, int textColor) {
        this.context = context;
        this.textColor = textColor;
    }

    @Override
    public void visit(SpannableStringBuilder builder, ResMessageSearch.Record record) {

        int textSize = (int) context.getResources().getDimension(R.dimen.jandi_message_search_item_large_txt_size);
        NameSpannable nameSpannable = new NameSpannable(textSize, textColor);

        String name = TeamInfoLoader.getInstance().getMemberName(record.getMemberId());
        int start = builder.length();
        builder.append(name);

        builder.setSpan(nameSpannable, start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}
