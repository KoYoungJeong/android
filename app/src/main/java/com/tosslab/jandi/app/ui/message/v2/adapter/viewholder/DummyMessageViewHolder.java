package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.domain.SendMessage;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.spannable.SpannableLookUp;
import com.tosslab.jandi.app.spannable.analysis.mention.MentionAnalysisInfo;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerManager;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.builder.BaseViewHolderBuilder;
import com.tosslab.jandi.app.utils.image.ImageUtil;

public class DummyMessageViewHolder implements BodyViewHolder {
    private SimpleDraweeView ivProfile;
    private ViewGroup vgName;
    private TextView tvName;
    private TextView tvMessage;
    private SimpleDraweeView ivSticker;
    private ImageView ivStatus;

    private boolean hasBottomMargin = false;
    private boolean hasProfile = false;

    private DummyMessageViewHolder() {
    }

    public void initView(View rootView) {
        ivProfile = (SimpleDraweeView) rootView.findViewById(R.id.iv_message_user_profile);
        vgName = (ViewGroup) rootView.findViewById(R.id.vg_message_profile_user_name);
        tvName = (TextView) rootView.findViewById(R.id.tv_message_profile_user_name);
        tvMessage = (TextView) rootView.findViewById(R.id.tv_dummy_message_content);
        ivSticker = (SimpleDraweeView) rootView.findViewById(R.id.iv_dummy_message_sticker);
        ivStatus = (ImageView) rootView.findViewById(R.id.iv_dummy_send_status);
        View vBottomMargin = rootView.findViewById(R.id.v_margin);

        if (hasBottomMargin) {
            vBottomMargin.setVisibility(View.VISIBLE);
        } else {
            vBottomMargin.setVisibility(View.GONE);
        }

        if (hasProfile) {
            ivProfile.setVisibility(View.VISIBLE);
            vgName.setVisibility(View.VISIBLE);
        } else {
            ivProfile.setVisibility(View.INVISIBLE);
            vgName.setVisibility(View.GONE);
        }
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
            ivStatus.setVisibility(View.VISIBLE);
            ivStatus.setVisibility(View.GONE);
            tvMessage.setVisibility(View.VISIBLE);

            setTextSendingStatus(dummyMessageLink);

            long myId = EntityManager.getInstance().getMe().getId();
            MentionAnalysisInfo mentionAnalysisInfo =
                    MentionAnalysisInfo.newBuilder(myId, ((DummyMessageLink) link).getMentions())
                            .textSize(tvMessage.getTextSize())
                            .build();

            SpannableLookUp.text(builder)
                    .markdown(false)
                    .mention(mentionAnalysisInfo, false)
                    .lookUp(JandiApplication.getContext());

            tvMessage.setText(builder);
        } else if (link.message instanceof ResMessages.StickerMessage) {
            ResMessages.StickerMessage stickerMessage = (ResMessages.StickerMessage) link.message;
            ivSticker.setVisibility(View.VISIBLE);
            ivStatus.setVisibility(View.GONE);
            ivStatus.setVisibility(View.VISIBLE);
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
                ivStatus.setImageResource(R.drawable.icon_message_sending);
                break;
            case FAIL:
                ivStatus.setImageResource(R.drawable.icon_message_failure);
                break;
        }
    }

    private void setTextSendingStatus(DummyMessageLink dummyMessageLink) {
        SendMessage.Status status = SendMessage.Status.valueOf(dummyMessageLink.getStatus());
        switch (status) {
            case COMPLETE:
            case SENDING: {
                ivStatus.setImageResource(R.drawable.icon_message_sending);
                break;
            }
            case FAIL:
                ivStatus.setImageResource(R.drawable.icon_message_failure);
                break;
        }
    }

    @Override
    public void setLastReadViewVisible(long currentLinkId, long lastReadLinkId) {

    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_dummy_v3;
    }

    public void setHasBottomMargin(boolean hasBottomMargin) {
        this.hasBottomMargin = hasBottomMargin;
    }

    public void setHasProfile(boolean hasProfile) {
        this.hasProfile = hasProfile;
    }

    @Override
    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        if (ivSticker.getVisibility() == View.VISIBLE) {
            ivSticker.setOnClickListener(itemClickListener);
        } else {
            tvMessage.setOnClickListener(itemClickListener);
        }
    }

    @Override
    public void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener) {
        if (ivSticker.getVisibility() == View.VISIBLE) {
            ivSticker.setOnLongClickListener(itemLongClickListener);
        } else {
            tvMessage.setOnLongClickListener(itemLongClickListener);
        }
    }

    public static class Builder extends BaseViewHolderBuilder {
//        private boolean hasProfile = false;
//        private boolean hasBottomMargin = false;
//
//        public Builder setHasBottomMargin(boolean hasBottomMargin) {
//            this.hasBottomMargin = hasBottomMargin;
//            return this;
//        }
//
//        public Builder setHasProfile(boolean hasProfile) {
//            this.hasProfile = hasProfile;
//            return this;
//        }

        public DummyMessageViewHolder build() {
            DummyMessageViewHolder dummyViewHolder = new DummyMessageViewHolder();
            dummyViewHolder.setHasProfile(hasProfile);
            dummyViewHolder.setHasBottomMargin(hasBottomMargin);
            return dummyViewHolder;
        }
    }

}
