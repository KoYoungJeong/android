package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.content.res.Resources;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.views.spannable.DateViewSpannable;
import com.tosslab.jandi.app.views.spannable.UnreadCountSpannable;

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
        builder.append(!TextUtils.isEmpty(message) ? message : "");

        Context context = tvMessage.getContext();

        boolean hasLink = LinkifyUtil.addLinks(context, builder);
        if (hasLink) {
            Spannable linkSpannable = Spannable.Factory.getInstance().newSpannable(builder);
            builder.setSpan(linkSpannable, 0, message.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            LinkifyUtil.setOnLinkClick(tvMessage);
        }

        builder.append(" ");

        Resources resources = context.getResources();

        int startIndex = builder.length();
        builder.append(DateTransformator.getTimeStringForSimple(link.message.createTime));
        int endIndex = builder.length();

        DateViewSpannable spannable =
                new DateViewSpannable(tvMessage.getContext(),
                        DateTransformator.getTimeStringForSimple(link.message.createTime));
        builder.setSpan(spannable, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        int unreadCount = UnreadCountUtil.getUnreadCount(context, teamId, roomId,
                link.id, link.fromEntity, EntityManager.getInstance(context).getMe().getId());

        if (unreadCount > 0) {
            UnreadCountSpannable unreadCountSpannable =
                    UnreadCountSpannable.createUnreadCountSpannable(
                            context, String.valueOf(unreadCount));
            builder.append("   ")
                    .setSpan(unreadCountSpannable, builder.length() - 2, builder.length() - 1,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        tvMessage.setText(builder);
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_puremsg_v2;
    }
}
