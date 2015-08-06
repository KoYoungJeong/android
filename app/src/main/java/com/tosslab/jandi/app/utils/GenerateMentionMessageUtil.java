package com.tosslab.jandi.app.utils;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.views.spannable.MensionMessageSpannable;

import java.util.Collection;

/**
 * Created by tee on 15. 8. 6..
 */
public class GenerateMentionMessageUtil {

    private Context context = null;
    private SpannableStringBuilder stringBuilder = null;
    private Collection<MentionObject> mentions = null;
    private TextView textView = null;
    private float pxSize = -1;
    private int textColor = -1;
    private int backgroundColor = -1;

    public GenerateMentionMessageUtil(TextView textView, SpannableStringBuilder stringBuilder,
                                      Collection<MentionObject> mentions) {

        this.pxSize = context.getResources().getDimensionPixelSize(R.dimen.jandi_mention_message_item_font_size);
        this.textColor = 0xFF00a6e9;
        this.context = context;
        this.stringBuilder = stringBuilder;
        this.mentions = mentions;

    }

    public SpannableStringBuilder generate() {
        boolean hasMention = false;
        for (MentionObject mention : mentions) {
            String name = stringBuilder.subSequence(mention.getOffset() + 1, mention.getLength() + mention.getOffset()).toString();
            MensionMessageSpannable spannable1 = new MensionMessageSpannable(context,
                    name, mention.getId(), pxSize, textColor, backgroundColor);
            stringBuilder.setSpan(spannable1, mention.getOffset(), mention.getLength() + mention.getOffset(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (!hasMention) {
                hasMention = true;
            }
        }

        if (hasMention) {
            LinkifyUtil.setOnLinkClick(textView);
        }

        return stringBuilder;
    }

    public GenerateMentionMessageUtil setPxSize(int pxSize) {
        this.pxSize = context.getResources().getDimensionPixelSize(pxSize);
        return this;
    }

    public GenerateMentionMessageUtil setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public GenerateMentionMessageUtil setTextColor(int textColor) {
        this.textColor = textColor;
        return this;
    }

}
