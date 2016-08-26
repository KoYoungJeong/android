package com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.adapter.viewholder;

import android.content.res.Resources;
import android.graphics.Color;
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
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.dto.MentionMessage;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.UiUtils;
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
    @Bind(R.id.v_mention_message_name_cover)
    View vWriterCover;
    @Bind(R.id.tv_mention_message_date)
    TextView tvDate;
    @Bind(R.id.tv_mention_message_content)
    TextView tvMentionContent;
    @Bind(R.id.tv_mention_message_topic_name)
    TextView tvTopicName;
    @Bind(R.id.iv_mention_message_content_icon)
    ImageView ivContentIcon;

    private int topMarginForIvProfile;

    public MentionMessageViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        topMarginForIvProfile = itemView.getContext().
                getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin);
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

    private void bindContent(MentionMessage mentionMessage) {
        if ("comment".equals(mentionMessage.getContentType())) {
            tvTopicName.setEllipsize(TextUtils.TruncateAt.MIDDLE);
            tvTopicName.setText(mentionMessage.getFeedbackTitle());
            if ("poll".equals(mentionMessage.getFeedbackType())) {
                ivContentIcon.setImageResource(R.drawable.icon_message_poll);
            } else {
                ivContentIcon.setImageResource(R.drawable.icon_message_file);
            }
            ivContentIcon.setVisibility(View.VISIBLE);
        } else if ("file".equals(mentionMessage.getContentType())) {
            tvTopicName.setText(mentionMessage.getContentTitle());
            tvTopicName.setEllipsize(TextUtils.TruncateAt.END);
            ivContentIcon.setVisibility(View.GONE);
        } else {
            tvTopicName.setText(mentionMessage.getRoomName());
            tvTopicName.setEllipsize(TextUtils.TruncateAt.END);
            ivContentIcon.setVisibility(View.GONE);
        }

        SpannableStringBuilder ssb = new SpannableStringBuilder(mentionMessage.getContentBody());

        long myId = TeamInfoLoader.getInstance().getMyId();
        MentionAnalysisInfo mentionAnalysisInfo =
                MentionAnalysisInfo.newBuilder(myId, mentionMessage.getMentions())
                        .textSizeFromResource(R.dimen.jandi_mention_star_list_item_font_size)
                        .forMeBackgroundColor(Color.parseColor("#FF01A4E7"))
                        .forMeTextColor(Color.WHITE)
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
        ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) ivProfile.getLayoutParams();

        if (!isJandiBot) {
            layoutParams.topMargin = topMarginForIvProfile;
            layoutParams.height = (int) UiUtils.getPixelFromDp(44f);
        } else {
            layoutParams.height = layoutParams.width * 5 / 4;
            layoutParams.topMargin = topMarginForIvProfile - layoutParams.width / 4;
        }

        ivProfile.setLayoutParams(layoutParams);

        Resources resources = tvWriter.getResources();
        if (!isJandiBot && !isBot) {
            ImageUtil.loadProfileImage(ivProfile, mentionMessage.getWriterProfileUrl(), R.drawable.profile_img);

            if (TeamInfoLoader.getInstance().getUser(mentionMessage.getWriterId()).isEnabled()) {
                vProfileCover.setBackgroundColor(Color.TRANSPARENT);

                tvWriter.setTextColor(resources.getColor(R.color.jandi_star_mention_item_name_content_text));
                vWriterCover.setVisibility(View.GONE);
            } else {
                ShapeDrawable foreground = new ShapeDrawable(new OvalShape());
                foreground.getPaint().setColor(0x66FFFFFF);
                vProfileCover.setBackgroundDrawable(foreground);

                tvWriter.setTextColor(resources.getColor(R.color.deactivate_text_color));
                vWriterCover.setVisibility(View.VISIBLE);
            }
            return;
        }

        vProfileCover.setVisibility(View.GONE);
        vWriterCover.setVisibility(View.GONE);

        if (isJandiBot) {

            ivProfile.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            ImageLoader.loadFromResources(ivProfile, R.drawable.bot_80x100);

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
