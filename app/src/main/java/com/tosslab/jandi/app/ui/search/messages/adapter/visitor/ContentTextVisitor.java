package com.tosslab.jandi.app.ui.search.messages.adapter.visitor;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessageSearch;
import com.tosslab.jandi.app.ui.search.messages.adapter.spannable.HighlightSpannable;
import com.tosslab.jandi.app.ui.search.messages.adapter.spannable.MessageSpannable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Steve SeongUg Jung on 15. 3. 13..
 */
public class ContentTextVisitor implements TextVisitor {

    private final String query;
    private Context context;
    private int textColor;

    public ContentTextVisitor(Context context, int textColor, String query) {
        this.context = context;
        this.textColor = textColor;
        this.query = query;
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

        if (!TextUtils.isEmpty(query)) {
            updateHighlight(builder, start, query, textColor, context.getResources().getColor(R.color.jandi_message_search_item_highlight));
        }

    }

    private void updateHighlight(SpannableStringBuilder builder, int start, String query, int textColor1, int backgroundColor) {

        Pattern compile = Pattern.compile(query, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compile.matcher(builder);

        int patternStart = start;

        while (matcher.find(patternStart)) {
            int localStart = matcher.start();
            int localEnd = matcher.end();


            builder.setSpan(new HighlightSpannable(backgroundColor, textColor1), localStart, localEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            patternStart = localEnd;
        }

    }
}
