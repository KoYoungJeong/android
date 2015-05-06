package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.res.Resources;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.search.messages.adapter.spannable.MessageSpannable;
import com.tosslab.jandi.app.utils.DateTransformator;

/**
 * Created by Steve SeongUg Jung on 15. 5. 6..
 */
public class PureMessageViewHolder implements BodyViewHolder {

    private TextView tvMessage;

    @Override
    public void initView(View rootView) {
        tvMessage = (TextView) rootView.findViewById(R.id.tv_message_content);
    }

    @Override
    public void bindData(ResMessages.Link link, int teamId, int roomId) {
        String message = ((ResMessages.TextMessage) link.message).content.body;

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(message).append(" ");

        Resources resources = tvMessage.getContext().getResources();

        int dimension = ((int) resources.getDimension(R.dimen.jandi_messages_date));
        int jandi_messages_date = R.color.jandi_messages_date;

        int startIndex = builder.length();
        builder.append(DateTransformator.getTimeStringForSimple(link.message.updateTime));
        int endIndex = builder.length();

        MessageSpannable spannable = new MessageSpannable(dimension, resources.getColor(jandi_messages_date));
        builder.setSpan(spannable, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvMessage.setText(builder);

    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_puremsg_v2;
    }
}
