package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.bot.jandi;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.lists.BotEntity;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.spannable.SpannableLookUp;
import com.tosslab.jandi.app.spannable.analysis.mention.MentionAnalysisInfo;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.BodyViewHolder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.UnreadCountUtil;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.builder.BaseViewHolderBuilder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.linkpreview.LinkPreviewViewModel;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.views.spannable.DateViewSpannable;

import de.greenrobot.event.EventBus;

public class JandiBotViewHolder implements BodyViewHolder {
    protected Context context;
    private ImageView ivProfile;
    private TextView tvName;
    private TextView tvMessage;
    private View vDisableCover;
    private View vDisableLineThrough;
    private LinkPreviewViewModel linkPreviewViewModel;
    private ViewGroup vLastRead;
    private View contentView;
    private boolean hasOnlyBadge;
    private boolean hasBottomMargin;
    private View vMargin;
    private TextView tvMessageTime;
    private TextView tvMessageBadge;
    private boolean hasBotProfile;
    private ViewGroup vgUserName;

    private JandiBotViewHolder() {
    }

    @Override
    public void initView(View rootView) {
        contentView = rootView.findViewById(R.id.vg_dummy_message_item);
        ivProfile = (ImageView) rootView.findViewById(R.id.iv_message_user_profile);
        vgUserName = (ViewGroup) rootView.findViewById(R.id.vg_message_profile_user_name);
        tvName = (TextView) rootView.findViewById(R.id.tv_message_user_name);
        tvMessage = (TextView) rootView.findViewById(R.id.tv_message_content);
        vDisableCover = rootView.findViewById(R.id.v_entity_listitem_warning);
        vDisableLineThrough = rootView.findViewById(R.id.iv_entity_listitem_line_through);
        vMargin = rootView.findViewById(R.id.v_margin);
        tvMessageTime = (TextView) rootView.findViewById(R.id.tv_message_time);
        tvMessageBadge = (TextView) rootView.findViewById(R.id.tv_message_badge);
        context = rootView.getContext();

        linkPreviewViewModel = new LinkPreviewViewModel(context);
        linkPreviewViewModel.initView(rootView);
        vLastRead = (ViewGroup) rootView.findViewById(R.id.vg_message_last_read);

        if (hasBottomMargin) {
            vMargin.setVisibility(View.VISIBLE);
        } else {
            vMargin.setVisibility(View.GONE);
        }

        if (hasBotProfile) {
            ivProfile.setVisibility(View.VISIBLE);
            vgUserName.setVisibility(View.VISIBLE);
        } else {
            ivProfile.setVisibility(View.INVISIBLE);
            vgUserName.setVisibility(View.GONE);
        }
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

            long myId = entityManager.getMe().getId();
            MentionAnalysisInfo mentionAnalysisInfo =
                    MentionAnalysisInfo.newBuilder(myId, textMessage.mentions)
                            .textSize(tvMessage.getTextSize())
                            .build();

            SpannableLookUp.text(messageStringBuilder)
                    .hyperLink(false)
                    .markdown(false)
                    .emailLink(false)
                    .webLink(false)
                    .telLink(false)
                    .mention(mentionAnalysisInfo, false)
                    .lookUp(context);

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

            if (!hasOnlyBadge) {
                tvMessageTime.setVisibility(View.VISIBLE);
                tvMessageTime.setText(DateTransformator.getTimeStringForSimple(link.message.createTime));
            } else {
                tvMessageTime.setVisibility(View.GONE);
            }

            if (unreadCount > 0) {
                tvMessageBadge.setText(String.valueOf(unreadCount));
            }

        }

        ivProfile.setOnClickListener(v -> EventBus.getDefault().post(new ShowProfileEvent(bot.id, ShowProfileEvent.From.Image)));
        tvName.setOnClickListener(v -> EventBus.getDefault().post(new ShowProfileEvent(bot.id, ShowProfileEvent.From.Name)));

        linkPreviewViewModel.bindData(link);
    }

    @Override
    public void setLastReadViewVisible(long currentLinkId, long lastReadLinkId) {
        if (vLastRead != null) {
            if (currentLinkId == lastReadLinkId) {
                vLastRead.removeAllViews();
                LayoutInflater.from(vLastRead.getContext())
                        .inflate(R.layout.item_message_last_read_v2, vLastRead);
                vLastRead.setVisibility(View.VISIBLE);
            } else {
                vLastRead.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_jandi_bot_msg_v3;
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

    public void setHasOnlyBadge(boolean hasOnlyBadge) {
        this.hasOnlyBadge = hasOnlyBadge;
    }

    public void setHasBottomMargin(boolean hasBottomMargin) {
        this.hasBottomMargin = hasBottomMargin;
    }

    public void setHasBotProfile(boolean hasBotProfile) {
        this.hasBotProfile = hasBotProfile;
    }

    public static class Builder extends BaseViewHolderBuilder {
        public JandiBotViewHolder build() {
            JandiBotViewHolder jandiBotViewHolder = new JandiBotViewHolder();
            jandiBotViewHolder.setHasOnlyBadge(hasOnlyBadge);
            jandiBotViewHolder.setHasBottomMargin(hasBottomMargin);
            jandiBotViewHolder.setHasBotProfile(hasProfile);
            return jandiBotViewHolder;
        }
    }

}
