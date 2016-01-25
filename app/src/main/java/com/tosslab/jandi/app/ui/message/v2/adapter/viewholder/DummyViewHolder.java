package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.domain.SendMessage;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.commonviewmodels.markdown.viewmodel.MarkdownViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerManager;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.utils.GenerateMentionMessageUtil;
import com.tosslab.jandi.app.utils.image.ImageUtil;

/**
 * Created by Steve SeongUg Jung on 15. 2. 4..
 */
public class DummyViewHolder implements BodyViewHolder {

    private SimpleDraweeView ivProfile;
    private TextView tvName;
    private TextView tvMessage;
    private View contentView;
    private SimpleDraweeView ivSticker;
    private ImageView ivStickerStatus;

    @Override
    public void initView(View rootView) {
        contentView = rootView.findViewById(R.id.vg_message_item);
        ivProfile = (SimpleDraweeView) rootView.findViewById(R.id.iv_message_user_profile);
        tvName = (TextView) rootView.findViewById(R.id.tv_message_user_name);
        tvMessage = (TextView) rootView.findViewById(R.id.tv_message_content);
        ivSticker = (SimpleDraweeView) rootView.findViewById(R.id.iv_message_sticker);
        ivStickerStatus = (ImageView) rootView.findViewById(R.id.iv_message_send_status);
    }

    @Override
    public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {

        DummyMessageLink dummyMessageLink = (DummyMessageLink) link;

        FormattedEntity entity = EntityManager.getInstance()
                .getEntityById(dummyMessageLink.message.writerId);

        String profileUrl = entity.getUserLargeProfileUrl();

        ImageUtil.loadProfileImage(ivProfile, profileUrl, R.drawable.profile_img);

        tvName.setText(entity.getName());

        SpannableStringBuilder builder = new SpannableStringBuilder();

        if (link.message instanceof ResMessages.TextMessage) {
            ResMessages.TextMessage textMessage = (ResMessages.TextMessage) link.message;
            builder.append(textMessage.content.body);
            ivSticker.setVisibility(View.GONE);
            ivStickerStatus.setVisibility(View.GONE);
            tvMessage.setVisibility(View.VISIBLE);

            setTextSendingStatus(dummyMessageLink, builder);

            MarkdownViewModel markdownViewModel = new MarkdownViewModel(tvMessage, builder, false);
            markdownViewModel.execute();

            GenerateMentionMessageUtil generateMentionMessageUtil = new GenerateMentionMessageUtil(
                    tvMessage, builder, ((DummyMessageLink) link).getMentions(),
                    EntityManager.getInstance().getMe().getId());
            builder = generateMentionMessageUtil.generate(false);

            tvMessage.setText(builder);
        } else if (link.message instanceof ResMessages.StickerMessage) {
            ResMessages.StickerMessage stickerMessage = (ResMessages.StickerMessage) link.message;
            ivSticker.setVisibility(View.VISIBLE);
            ivStickerStatus.setVisibility(View.VISIBLE);
            tvMessage.setVisibility(View.GONE);

            ResMessages.StickerContent content = stickerMessage.content;

            setStickerSendingStatus(dummyMessageLink);

            StickerManager.getInstance().loadStickerDefaultOption(ivSticker, content.groupId, content.stickerId);
        }

    }

    private void setStickerSendingStatus(DummyMessageLink dummyMessageLink) {
        SendMessage.Status status = SendMessage.Status.valueOf(dummyMessageLink.getStatus());
        switch (status) {

            case COMPLETE:
            case SENDING:
                ivStickerStatus.setImageResource(R.drawable.icon_message_sending);
                break;
            case FAIL:
                ivStickerStatus.setImageResource(R.drawable.icon_message_failure);
                break;
        }
    }

    private void setTextSendingStatus(DummyMessageLink dummyMessageLink, SpannableStringBuilder builder) {
        SendMessage.Status status = SendMessage.Status.valueOf(dummyMessageLink.getStatus());
        int textColor = tvName.getContext().getResources().getColor(R.color.jandi_messages_name);
        switch (status) {
            case FAIL: {
                builder.append("  ");
                int beforLenghth = builder.length();
                Drawable drawable = tvMessage.getContext().getResources()
                        .getDrawable(R.drawable.icon_message_failure);
                drawable.setBounds(0, 0,
                        drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                builder.append(" ")
                        .setSpan(
                                new ImageSpan(drawable,
                                        ImageSpan.ALIGN_BASELINE),
                                beforLenghth, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tvName.setTextColor(textColor);
                tvMessage.setTextColor(textColor);
                break;
            }
            case COMPLETE:
            case SENDING: {
                builder.append("  ");
                int beforLenghth = builder.length();
                Drawable drawable = tvMessage.getContext().getResources()
                        .getDrawable(R.drawable.icon_message_sending);
                drawable.setBounds(0, 0,
                        drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                builder.append(" ")
                        .setSpan(
                                new ImageSpan(drawable,
                                        ImageSpan.ALIGN_BASELINE),
                                beforLenghth, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tvMessage.setTextColor(textColor);
                tvName.setTextColor(textColor);
                tvMessage.setTextColor(textColor);
                break;
            }
        }
    }

    @Override
    public void setLastReadViewVisible(long currentLinkId, long lastReadLinkId) {

    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_dummy_v2;

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
