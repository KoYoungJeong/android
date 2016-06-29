package com.tosslab.jandi.app.ui.search.messages.adapter.strategy;

import android.content.Context;
import android.text.SpannableStringBuilder;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessageSearch;
import com.tosslab.jandi.app.ui.search.messages.adapter.visitor.ContentTextVisitor;
import com.tosslab.jandi.app.ui.search.messages.adapter.visitor.FileInfoTextVisitor;
import com.tosslab.jandi.app.ui.search.messages.adapter.visitor.PollInfoTextVisitor;
import com.tosslab.jandi.app.ui.search.messages.adapter.visitor.TextVisitor;
import com.tosslab.jandi.app.ui.search.messages.adapter.visitor.TypeTextVisitor;
import com.tosslab.jandi.app.ui.search.messages.adapter.visitor.WriterTextVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 3. 13..
 */
public class TextStrategy {

    public static SpannableStringBuilder getCurrentSearchString(Context context, ResMessageSearch.Record record, String query) {

        int textColor = context.getResources().getColor(R.color.jandi_message_search_item_topic_txt_color);

        SpannableStringBuilder stringBuilder = getSearchString(context, record, textColor, query);

        return stringBuilder;
    }

    public static SpannableStringBuilder getSubSearchString(Context context, ResMessageSearch.Record record) {

        int textColor = context.getResources().getColor(R.color.jandi_message_search_item_topic_txt_color_sub);

        SpannableStringBuilder stringBuilder = getSearchString(context, record, textColor, null);

        return stringBuilder;
    }

    private static SpannableStringBuilder getSearchString(Context context, ResMessageSearch.Record record, int textColor, String query) {
        List<TextVisitor> textVisitors = new ArrayList<TextVisitor>();

        textVisitors.add(new WriterTextVisitor(context, textColor));
        textVisitors.add(new TypeTextVisitor(context));
        textVisitors.add(new FileInfoTextVisitor(context, textColor));
        textVisitors.add(new PollInfoTextVisitor(context, textColor));
        textVisitors.add(new ContentTextVisitor(context, textColor, query));

        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();

        int size = textVisitors.size();
        for (int idx = 0; idx < size; idx++) {
            if (idx > 0) {
                stringBuilder.append(" ");
            }
            textVisitors.get(idx).visit(stringBuilder, record);
        }
        return stringBuilder;
    }
}
