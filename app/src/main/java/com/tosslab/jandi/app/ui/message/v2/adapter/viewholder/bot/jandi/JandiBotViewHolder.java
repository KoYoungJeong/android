package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.bot.jandi;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.lists.BotEntity;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.commonviewmodels.markdown.viewmodel.MarkdownViewModel;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.BodyViewHolder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.UnreadCountUtil;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.linkpreview.LinkPreviewViewModel;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.GenerateMentionMessageUtil;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.views.spannable.DateViewSpannable;
import com.tosslab.jandi.app.views.spannable.NameSpannable;

import de.greenrobot.event.EventBus;

public class JandiBotViewHolder implements BodyViewHolder {
    protected Context context;
    private ImageView ivProfile;
    private TextView tvName;
    private TextView tvMessage;
    private View vDisableCover;
    private View vDisableLineThrough;
    private LinkPreviewViewModel linkPreviewViewModel;
    private View vLastRead;
    private View contentView;

    @Override
    public void initView(View rootView) {
        contentView = rootView.findViewById(R.id.vg_message_item);
        ivProfile = (ImageView) rootView.findViewById(R.id.iv_message_user_profile);
        tvName = (TextView) rootView.findViewById(R.id.tv_message_user_name);
        tvMessage = (TextView) rootView.findViewById(R.id.tv_message_content);
        vDisableCover = rootView.findViewById(R.id.v_entity_listitem_warning);
        vDisableLineThrough = rootView.findViewById(R.id.iv_entity_listitem_line_through);
        context = rootView.getContext();

        linkPreviewViewModel = new LinkPreviewViewModel(context);
        linkPreviewViewModel.initView(rootView);
        vLastRead = rootView.findViewById(R.id.vg_message_last_read);
    }

    @Override
    public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {
        long fromEntityId = link.fromEntity;

        EntityManager entityManager = EntityManager.getInstance();
        FormattedEntity entity = entityManager.getEntityById(fromEntityId);
        if (!(entity instanceof BotEntity)) {
            return;
        }

        BotEntity botEntity = (BotEntity) entity;
        ResLeftSideMenu.Bot bot = botEntity.getBot();

        ivProfile.setImageResource(R.drawable.bot_32x40);

        if (bot != null && TextUtils.equals(bot.status, "enabled")) {
            tvName.setTextColor(context.getResources().getColor(R.color.jandi_messages_name));
            vDisableCover.setVisibility(View.GONE);
            vDisableLineThrough.setVisibility(View.GONE);
        } else {
            tvName.setTextColor(
                    tvName.getResources().getColor(R.color.deactivate_text_color));
            vDisableCover.setVisibility(View.VISIBLE);
            vDisableLineThrough.setVisibility(View.VISIBLE);
        }

        tvName.setText(bot.name);
        if (link.message instanceof ResMessages.TextMessage) {
            ResMessages.TextMessage textMessage = (ResMessages.TextMessage) link.message;

            SpannableStringBuilder messageStringBuilder = new SpannableStringBuilder();
            messageStringBuilder.append(!TextUtils.isEmpty(textMessage.content.body) ? textMessage.content.body : "");

            GenerateMentionMessageUtil generateMentionMessageUtil = new GenerateMentionMessageUtil(
                    tvMessage, messageStringBuilder, textMessage.mentions, entityManager.getMe().getId());
            messageStringBuilder = generateMentionMessageUtil.generate(true);

            MarkdownViewModel markdownViewModel = new MarkdownViewModel(tvMessage, messageStringBuilder, false);
            markdownViewModel.execute();


            LinkifyUtil.addLinks(context, messageStringBuilder);
            LinkifyUtil.setOnLinkClick(tvMessage);

            messageStringBuilder.append(" ");

            int startIndex = messageStringBuilder.length();
            messageStringBuilder.append(
                    DateTransformator.getTimeStringForSimple(link.message.createTime));
            int endIndex = messageStringBuilder.length();

            DateViewSpannable spannable =
                    new DateViewSpannable(tvMessage.getContext(),
                            DateTransformator.getTimeStringForSimple(link.message.createTime));
            messageStringBuilder.setSpan(spannable,
                    startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            int unreadCount = UnreadCountUtil.getUnreadCount(teamId, roomId,
                    link.id, link.fromEntity, EntityManager.getInstance().getMe().getId());

            if (unreadCount > 0) {
                NameSpannable unreadCountSpannable =
                        new NameSpannable(
                                context.getResources().getDimensionPixelSize(R.dimen.jandi_text_size_small)
                                , context.getResources().getColor(R.color.jandi_accent_color));
                int beforeLength = messageStringBuilder.length();
                messageStringBuilder.append(" ");
                messageStringBuilder.append(String.valueOf(unreadCount))
                        .setSpan(unreadCountSpannable, beforeLength, messageStringBuilder.length(),
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            tvMessage.setText(messageStringBuilder);

        }
        ivProfile.setOnClickListener(v -> EventBus.getDefault().post(new ShowProfileEvent(bot.id, ShowProfileEvent.From.Image)));
        tvName.setOnClickListener(v -> EventBus.getDefault().post(new ShowProfileEvent(bot.id, ShowProfileEvent.From.Name)));

        linkPreviewViewModel.bindData(link);
    }

    @Override
    public void setLastReadViewVisible(long currentLinkId, long lastReadLinkId) {
        if (currentLinkId == lastReadLinkId) {
            vLastRead.setVisibility(View.VISIBLE);
        } else {
            vLastRead.setVisibility(View.GONE);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_jandi_bot_msg_v2;
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
