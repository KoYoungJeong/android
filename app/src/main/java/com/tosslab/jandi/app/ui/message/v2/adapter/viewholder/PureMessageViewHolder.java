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
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.linkpreview.LinkPreviewViewModel;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.views.spannable.DateViewSpannable;
import com.tosslab.jandi.app.views.spannable.MensionMessageSpannable;
import com.tosslab.jandi.app.views.spannable.NameSpannable;

/**
 * Created by Steve SeongUg Jung on 15. 5. 6..
 */
public class PureMessageViewHolder implements BodyViewHolder {

    private TextView tvMessage;
    private LinkPreviewViewModel linkPreviewViewModel;
    private View lastReadView;

    @Override
    public void initView(View rootView) {
        tvMessage = (TextView) rootView.findViewById(R.id.tv_message_content);
        linkPreviewViewModel = new LinkPreviewViewModel(rootView.getContext());
        linkPreviewViewModel.initView(rootView);
        lastReadView = rootView.findViewById(R.id.vg_message_last_read);

    }

    @Override
    public void bindData(ResMessages.Link link, int teamId, int roomId, int entityId) {
        ResMessages.TextMessage textMessage = (ResMessages.TextMessage) link.message;
        String message = textMessage.content.body;

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

        int unreadCount = UnreadCountUtil.getUnreadCount(teamId, roomId,
                link.id, link.fromEntity, EntityManager.getInstance(context).getMe().getId());

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

        boolean hasMention = false;
        for (MentionObject mention : textMessage.mentions) {
            String name = builder.subSequence(mention.getOffset() + 1, mention.getLength() + mention.getOffset()).toString();
            MensionMessageSpannable spannable1 = new MensionMessageSpannable(tvMessage.getContext
                    (), name, mention.getId(), tvMessage.getResources().getDimensionPixelSize(R.dimen
                    .jandi_mention_message_item_font_size));
            builder.setSpan(spannable1, mention.getOffset(), mention.getLength() + mention.getOffset(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            if (!hasMention) {
                hasMention = true;
            }
        }

        if (hasMention) {
            LinkifyUtil.setOnLinkClick(tvMessage);
        }

        tvMessage.setText(builder);


        linkPreviewViewModel.bindData(link);

    }

    @Override
    public void setLastReadViewVisible(int currentLinkId, int lastReadLinkId) {
        if (currentLinkId == lastReadLinkId) {
            lastReadView.setVisibility(View.VISIBLE);
        } else {
            lastReadView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_puremsg_v2;
    }
}
