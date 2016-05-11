package com.tosslab.jandi.app.ui.maintab.mypage.adapter.viewholder;

import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.spannable.SpannableLookUp;
import com.tosslab.jandi.app.spannable.analysis.mention.MentionAnalysisInfo;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.maintab.mypage.dto.MentionMessage;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.utils.UriFactory;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.transform.TransformConfig;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tonyjs on 16. 3. 17..
 */
public class MentionMessageViewHolder extends BaseViewHolder<MentionMessage> {

    @Bind(R.id.iv_mention_message_profile)
    SimpleDraweeView ivProfile;
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
        tvTopicName.setText(mentionMessage.getRoomName());

        SpannableStringBuilder ssb = new SpannableStringBuilder(mentionMessage.getContentBody());

        long myId = EntityManager.getInstance().getMe().getId();
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
        boolean isBot = EntityManager.getInstance().isBot(mentionMessage.getWriterId());
        boolean isJandiBot = EntityManager.getInstance().isJandiBot(mentionMessage.getWriterId());
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

        if (!isJandiBot) {
            Uri uri = Uri.parse(mentionMessage.getWriterProfileUrl());
            if (!isBot) {
                ImageUtil.loadProfileImage(ivProfile, uri, R.drawable.profile_img);
            } else {
                RoundingParams circleRoundingParams = ImageUtil.getCircleRoundingParams(
                        TransformConfig.DEFAULT_CIRCLE_LINE_COLOR, TransformConfig.DEFAULT_CIRCLE_LINE_WIDTH);

                ImageLoader.newBuilder()
                        .placeHolder(R.drawable.profile_img, ScalingUtils.ScaleType.FIT_CENTER)
                        .actualScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                        .roundingParams(circleRoundingParams)
                        .load(uri)
                        .into(ivProfile);
            }
        } else {

            if (EntityManager.getInstance().getEntityById(mentionMessage.getWriterId()).isEnabled()) {
                vProfileCover.setBackgroundColor(Color.TRANSPARENT);

                tvWriter.setTextColor(tvWriter.getResources().getColor(R.color.jandi_star_mention_item_name_content_text));
                vWriterCover.setVisibility(View.GONE);
            } else {
                ShapeDrawable foreground = new ShapeDrawable(new OvalShape());
                foreground.getPaint().setColor(0x66FFFFFF);
                vProfileCover.setBackgroundDrawable(foreground);

                tvWriter.setTextColor(tvWriter.getResources().getColor(R.color.deactivate_text_color));
                vWriterCover.setVisibility(View.VISIBLE);
            }
            ImageLoader.newBuilder()
                    .placeHolder(R.drawable.bot_80x100, ScalingUtils.ScaleType.CENTER_INSIDE)
                    .actualScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                    .load(UriFactory.getResourceUri(R.drawable.bot_80x100))
                    .into(ivProfile);

        }

        tvWriter.setText(mentionMessage.getWriterName());
    }
}
