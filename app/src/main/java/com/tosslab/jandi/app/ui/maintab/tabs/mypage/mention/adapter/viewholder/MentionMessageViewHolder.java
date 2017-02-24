package com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.adapter.viewholder;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.spannable.SpannableLookUp;
import com.tosslab.jandi.app.spannable.analysis.mention.MentionAnalysisInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.dto.MentionMessage;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.image.transform.JandiProfileTransform;
import com.tosslab.jandi.app.utils.image.transform.TransformConfig;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MentionMessageViewHolder extends BaseViewHolder<MentionMessage> {

    @Bind(R.id.iv_mention_message_profile)
    ImageView ivProfile;
    @Bind(R.id.v_mention_message_profile_cover)
    View vProfileCover;
    @Bind(R.id.tv_mention_message_name)
    TextView tvWriter;
    @Bind(R.id.tv_mention_message_date)
    TextView tvDate;
    @Bind(R.id.tv_mention_message_content)
    TextView tvMentionContent;
    @Bind(R.id.tv_mention_message_topic_name)
    TextView tvTopicName;
    @Bind(R.id.iv_mention_message_content_icon)
    ImageView ivContentIcon;
    @Bind(R.id.tv_mention_message_content_name)
    TextView tvContentName;
    @Bind(R.id.tv_divide_bar)
    TextView tvDivideBar;
    @Bind(R.id.v_full_divider)
    View vFullDivider;
    @Bind(R.id.v_half_divider)
    View vHalfDivider;

    public MentionMessageViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public static MentionMessageViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_mention_message, parent, false);
        return new MentionMessageViewHolder(itemView);
    }

    @Override
    public void onBindView(MentionMessage mentionMessage) {
        bindWriter(mentionMessage);
        bindContent(mentionMessage);
    }

    public void setHalfDivider(boolean isHalfDivider) {
        if (isHalfDivider) {
            vHalfDivider.setVisibility(View.VISIBLE);
            vFullDivider.setVisibility(View.GONE);
        } else {
            vFullDivider.setVisibility(View.VISIBLE);
            vHalfDivider.setVisibility(View.GONE);
        }
    }

    private void bindContent(MentionMessage mentionMessage) {
        if ("comment".equals(mentionMessage.getContentType())) {
            tvTopicName.setEllipsize(TextUtils.TruncateAt.MIDDLE);
            tvTopicName.setVisibility(View.GONE);
            if ("poll".equals(mentionMessage.getFeedbackType())) {
                ivContentIcon.setImageResource(R.drawable.icon_message_poll);
                tvContentName.setText(mentionMessage.getFeedbackTitle());
            } else {
                ivContentIcon.setImageResource(R.drawable.icon_message_file);
                tvContentName.setText(mentionMessage.getFeedbackTitle());
            }
            tvContentName.setVisibility(View.VISIBLE);
            ivContentIcon.setVisibility(View.VISIBLE);
            tvDivideBar.setVisibility(View.GONE);
        } else {
            tvTopicName.setVisibility(View.VISIBLE);
            tvTopicName.setText(mentionMessage.getRoomName());
            tvTopicName.setEllipsize(TextUtils.TruncateAt.END);
            ivContentIcon.setVisibility(View.GONE);
            tvDivideBar.setVisibility(View.GONE);
            tvContentName.setVisibility(View.GONE);
        }

        SpannableStringBuilder ssb = new SpannableStringBuilder(mentionMessage.getContentBody());

        long myId = TeamInfoLoader.getInstance().getMyId();
        MentionAnalysisInfo mentionAnalysisInfo =
                MentionAnalysisInfo.newBuilder(myId, mentionMessage.getMentions())
                        .textSizeFromResource(R.dimen.jandi_mention_star_list_item_font_size)
                        .forMeBackgroundColor(0xFFCFF1FF)
                        .forMeTextColor(0xFF00ACE9)
                        .build();

        SpannableLookUp.text(ssb)
                .hyperLink(false)
                .markdown(false)
                .mention(mentionAnalysisInfo, false)
                .lookUp(tvMentionContent.getContext());

        // for single spannable
        ssb.append(" ");
        tvMentionContent.setText(ssb);

        String updateTime = DateTransformator.getTimeString(mentionMessage.getMessageCreatedAt());

        tvDate.setText(updateTime);

    }

    private void bindWriter(MentionMessage mentionMessage) {
        tvWriter.setText(mentionMessage.getWriterName());

        boolean isBot = TeamInfoLoader.getInstance().isBot(mentionMessage.getWriterId());
        boolean isJandiBot = TeamInfoLoader.getInstance().isJandiBot(mentionMessage.getWriterId());

        if (!isJandiBot && !isBot) {
            ImageUtil.loadProfileImage(ivProfile, mentionMessage.getWriterProfileUrl(), R.drawable.profile_img);

            User user = TeamInfoLoader.getInstance().getUser(mentionMessage.getWriterId());

            if (user != null && user.isEnabled()) {
                vProfileCover.setBackgroundColor(Color.TRANSPARENT);
                if ((tvWriter.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0) {
                    tvWriter.setPaintFlags(
                            tvWriter.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                }
                tvWriter.setTextColor(0xff333333);

            } else {
                ShapeDrawable foreground = new ShapeDrawable(new OvalShape());
                foreground.getPaint().setColor(0x66FFFFFF);
                vProfileCover.setBackgroundDrawable(foreground);
                tvWriter.setTextColor(0xff999999);
                tvWriter.setPaintFlags(tvWriter.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            return;
        }

        vProfileCover.setVisibility(View.GONE);

        if (isJandiBot) {

            ivProfile.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            ImageLoader.loadFromResources(ivProfile, R.drawable.logotype_80);

        } else {
            ImageLoader.newInstance()
                    .placeHolder(R.drawable.profile_img, ImageView.ScaleType.FIT_CENTER)
                    .actualImageScaleType(ImageView.ScaleType.CENTER_CROP)
                    .transformation(new JandiProfileTransform(ivProfile.getContext(),
                            TransformConfig.DEFAULT_CIRCLE_BORDER_WIDTH,
                            TransformConfig.DEFAULT_CIRCLE_BORDER_COLOR,
                            Color.TRANSPARENT))
                    .uri(Uri.parse(mentionMessage.getWriterProfileUrl()))
                    .into(ivProfile);
        }
    }

}
