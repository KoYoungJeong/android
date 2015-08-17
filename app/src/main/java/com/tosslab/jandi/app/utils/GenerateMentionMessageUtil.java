package com.tosslab.jandi.app.utils;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.views.spannable.ClickableMensionMessageSpannable;
import com.tosslab.jandi.app.views.spannable.MentionMessageSpannable;

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
    private int textColor = 0xFF00a6e9;
    private int backgroundColor = 0x00ffffff;
    private int meTextColor = 0xFF00a6e9;
    private int meBackgroundColor = 0xFFdaf2ff;
    private int myId;

    public GenerateMentionMessageUtil(TextView textView,
                                      SpannableStringBuilder stringBuilder,
                                      Collection<MentionObject> mentions,
                                      int myId) {
        context = textView.getContext();
        this.textView = textView;
        this.pxSize = context.getResources().getDimensionPixelSize(R.dimen.jandi_mention_message_item_font_size);
        this.stringBuilder = stringBuilder;
        this.mentions = mentions;
        this.myId = myId;
    }

    public SpannableStringBuilder generate() {
        boolean hasMention = false;
        MentionMessageSpannable spannable = null;
        for (MentionObject mention : mentions) {
            String name = stringBuilder.subSequence(mention.getOffset() + 1,
                    mention.getLength() + mention.getOffset()).toString();
            if (mention.getId() == myId) {
                spannable = new ClickableMensionMessageSpannable(context,
                        name, mention.getId(), pxSize, meTextColor, meBackgroundColor);
            } else {
                spannable = new ClickableMensionMessageSpannable(context,
                        name, mention.getId(), pxSize, textColor, backgroundColor);
            }
            stringBuilder.setSpan(spannable, mention.getOffset(),
                    mention.getLength() + mention.getOffset(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            if (!hasMention) {
                hasMention = true;
            }
        }

        if (hasMention) {
//            LinkifyUtil.setOnLinkClick(textView);
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

    public GenerateMentionMessageUtil setMeBackgroundColor(int backgroundColor) {
        this.meBackgroundColor = backgroundColor;
        return this;
    }

    public GenerateMentionMessageUtil setMeTextColor(int textColor) {
        this.meTextColor = textColor;
        return this;
    }


}
