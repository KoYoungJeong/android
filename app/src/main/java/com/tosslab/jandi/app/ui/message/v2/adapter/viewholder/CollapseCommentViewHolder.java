package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.GenerateMentionMessageUtil;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.views.spannable.DateViewSpannable;
import com.tosslab.jandi.app.views.spannable.NameSpannable;


public class CollapseCommentViewHolder implements BodyViewHolder {

    private TextView tvMessage;
    private View vLastRead;
    private View contentView;

    @Override
    public void initView(View rootView) {
        contentView = rootView.findViewById(R.id.vg_message_item);
        tvMessage = (TextView) rootView.findViewById(R.id.tv_pure_comment_content);
        vLastRead = rootView.findViewById(R.id.vg_message_last_read);
    }

    @Override
    public void bindData(ResMessages.Link link, int teamId, int roomId, int entityId) {
        ResMessages.CommentMessage commentMessage = (ResMessages.CommentMessage) link.message;
        String message = commentMessage.content.body;

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(message);

        Context context = tvMessage.getContext();

        boolean hasLink = LinkifyUtil.addLinks(context, builder);
        if (hasLink) {
            Spannable linkSpannable = Spannable.Factory.getInstance().newSpannable(builder);
            builder.setSpan(linkSpannable, 0, message.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            LinkifyUtil.setOnLinkClick(tvMessage);
        }

        builder.append(" ");

        GenerateMentionMessageUtil generateMentionMessageUtil = new GenerateMentionMessageUtil(
                tvMessage, builder, commentMessage.mentions,
                EntityManager.getInstance().getMe().getId())
                .setPxSize(R.dimen.jandi_mention_comment_item_font_size);
        builder = generateMentionMessageUtil.generate(true);


        int startIndex = builder.length();
        builder.append(DateTransformator.getTimeStringForSimple(commentMessage.createTime));
        int endIndex = builder.length();

        DateViewSpannable spannable =
                new DateViewSpannable(tvMessage.getContext(),
                        DateTransformator.getTimeStringForSimple(commentMessage.createTime));
        builder.setSpan(spannable, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        int unreadCount = UnreadCountUtil.getUnreadCount(teamId, roomId,
                link.id, link.fromEntity, EntityManager.getInstance().getMe().getId());

        if (unreadCount > 0) {
            NameSpannable unreadCountSpannable =
                    new NameSpannable(
                            context.getResources().getDimensionPixelSize(R.dimen.jandi_text_size_small)
                            , context.getResources().getColor(R.color.jandi_accent_color));
            int beforeLength = builder.length();
            builder.append(" ");
            builder.append(String.valueOf(unreadCount))
                    .setSpan(unreadCountSpannable, beforeLength, builder.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        tvMessage.setText(builder);
    }

    @Override
    public void setLastReadViewVisible(int currentLinkId, int lastReadLinkId) {
        if (currentLinkId == lastReadLinkId) {
            vLastRead.setVisibility(View.VISIBLE);
        } else {
            vLastRead.setVisibility(View.GONE);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_pure_cmt_without_file_v2;
    }

    @Override
    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        if (contentView != null && itemClickListener != null) {
            contentView.setOnClickListener(itemClickListener);
        }
    }

    @Override
    public void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener) {
        if (contentView != null && itemLongClickListener != null) {
            contentView.setOnLongClickListener(itemLongClickListener);
        }
    }
}
