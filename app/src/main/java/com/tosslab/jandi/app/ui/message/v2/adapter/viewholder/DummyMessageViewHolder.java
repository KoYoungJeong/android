package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.domain.SendMessage;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.spannable.SpannableLookUp;
import com.tosslab.jandi.app.spannable.analysis.mention.MentionAnalysisInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerManager;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.builder.BaseViewHolderBuilder;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.utils.image.ImageUtil;

public class DummyMessageViewHolder implements BodyViewHolder {
    private ImageView ivProfile;
    private ViewGroup vgProfileAbsence;
    private TextView tvName;
    private TextView tvMessage;
    private ImageView ivSticker;
    private ImageView ivStatus;

    private boolean hasBottomMargin = false;
    private boolean hasProfile = false;
    private boolean hasOnlyBadge = false;
    private View vBottomMargin;
    private View vgUnreadAndTime;
    private TextView tvUnreadCount;
    private TextView tvTime;


    private DummyMessageViewHolder() {
    }

    @Override
    public void initView(View rootView) {
        ivProfile = (ImageView) rootView.findViewById(R.id.iv_message_user_profile);
        vgProfileAbsence = (ViewGroup) rootView.findViewById(R.id.vg_profile_absence);
        tvName = (TextView) rootView.findViewById(R.id.tv_message_profile_user_name);
        tvMessage = (TextView) rootView.findViewById(R.id.tv_dummy_message_content);
        ivSticker = (ImageView) rootView.findViewById(R.id.iv_dummy_message_sticker);
        ivStatus = (ImageView) rootView.findViewById(R.id.iv_dummy_send_status);
        vBottomMargin = rootView.findViewById(R.id.v_margin);

        vgUnreadAndTime = rootView.findViewById(R.id.vg_badge_time);
        tvUnreadCount = (TextView) rootView.findViewById(R.id.tv_message_badge);
        tvTime = (TextView) rootView.findViewById(R.id.tv_message_time);

        int topMargin = (int) UiUtils.getPixelFromDp(5f);
        if (!hasProfile) {
            topMargin = (int) UiUtils.getPixelFromDp(6f);
        }

        ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) tvMessage.getLayoutParams();
        layoutParams.topMargin = topMargin;
        tvMessage.setLayoutParams(layoutParams);
    }

    private void setProfileVisible() {
        if (hasProfile) {
            ivProfile.setVisibility(View.VISIBLE);
            tvName.setVisibility(View.VISIBLE);
        } else {
            ivProfile.setVisibility(View.GONE);
            tvName.setVisibility(View.GONE);
        }
    }

    private void setMarginVisible(View vBottomMargin) {
        if (hasBottomMargin) {
            vBottomMargin.setVisibility(View.VISIBLE);
        } else {
            vBottomMargin.setVisibility(View.GONE);
        }
    }

    @Override
    public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {
        setMarginVisible(vBottomMargin);
        setProfileVisible();

        DummyMessageLink dummyMessageLink = (DummyMessageLink) link;

        User user = TeamInfoLoader.getInstance()
                .getUser(dummyMessageLink.message.writerId);

        String profileUrl = user.getPhotoUrl();

        if (user.getAbsence() == null || user.getAbsence().getStartAt() == null) {
            vgProfileAbsence.setVisibility(View.GONE);
        } else {
            vgProfileAbsence.setVisibility(View.VISIBLE);
        }

        ImageUtil.loadProfileImage(ivProfile, profileUrl, R.drawable.profile_img);

        tvName.setText(user.getName());

        SpannableStringBuilder builder = new SpannableStringBuilder();

        setSendingStatus(teamId, roomId, dummyMessageLink);

        if (link.message instanceof ResMessages.TextMessage) {
            ResMessages.TextMessage textMessage = (ResMessages.TextMessage) link.message;
            builder.append(textMessage.content.body);
            ivSticker.setVisibility(View.GONE);
            tvMessage.setVisibility(View.VISIBLE);


            long myId = TeamInfoLoader.getInstance().getMyId();
            MentionAnalysisInfo mentionAnalysisInfo =
                    MentionAnalysisInfo.newBuilder(myId, ((DummyMessageLink) link).getMentions())
                            .textSize(tvMessage.getTextSize())
                            .build();

            SpannableLookUp.text(builder)
                    .markdown(false)
                    .mention(mentionAnalysisInfo, false)
                    .lookUp(JandiApplication.getContext());

            tvMessage.setText(builder, TextView.BufferType.SPANNABLE);
        } else if (link.message instanceof ResMessages.StickerMessage) {
            ResMessages.StickerMessage stickerMessage = (ResMessages.StickerMessage) link.message;
            ivSticker.setVisibility(View.VISIBLE);
            tvMessage.setVisibility(View.GONE);

            ResMessages.StickerContent content = stickerMessage.content;

            StickerManager.getInstance().loadStickerDefaultOption(ivSticker, content.groupId, content.stickerId);
        }

    }

    private void setSendingStatus(long teamId, long roomId, DummyMessageLink link) {
        SendMessage.Status status = SendMessage.Status.valueOf(link.getStatus());
        switch (status) {
            case COMPLETE:
                ivStatus.setVisibility(View.GONE);
                vgUnreadAndTime.setVisibility(View.VISIBLE);
                if (hasOnlyBadge) {
                    tvTime.setVisibility(View.GONE);
                } else {
                    tvTime.setVisibility(View.VISIBLE);
                }
                tvTime.setText(DateTransformator.getTimeStringForSimple(link.message.createTime));
                if (link.unreadCnt > 0) {
                    tvUnreadCount.setText(link.unreadCnt + "");
                }
                break;
            case SENDING: {
                ivStatus.setVisibility(View.VISIBLE);
                ivStatus.setImageResource(R.drawable.icon_message_sending);
                vgUnreadAndTime.setVisibility(View.GONE);
                break;
            }
            case FAIL:
                ivStatus.setVisibility(View.VISIBLE);
                ivStatus.setImageResource(R.drawable.icon_message_failure);
                vgUnreadAndTime.setVisibility(View.GONE);
                break;
        }
    }

    private ResMessages.Link getLinkFromTag(View view) {
        if (view == null || view.getTag() == null
                || !view.getTag().getClass().isAssignableFrom(ResMessages.Link.class)) {
            return null;
        }

        return ((ResMessages.Link) view.getTag());
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

    public void setHasOnlyBadge(boolean hasOnlyBadge) {
        this.hasOnlyBadge = hasOnlyBadge;
    }

    public static class Builder extends BaseViewHolderBuilder {

        public DummyMessageViewHolder build() {
            DummyMessageViewHolder dummyViewHolder = new DummyMessageViewHolder();
            dummyViewHolder.setHasProfile(hasProfile);
            dummyViewHolder.setHasBottomMargin(hasBottomMargin);
            dummyViewHolder.setHasOnlyBadge(hasOnlyBadge);
            return dummyViewHolder;
        }
    }

}
