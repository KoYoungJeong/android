package com.tosslab.jandi.app.ui.starmention.adapter.viewholder;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.starmention.vo.StarMentionVO;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.UriFactory;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.transform.TransformConfig;

/**
 * Created by tee on 15. 8. 2..
 */
public class CommonStarMentionViewHolder extends RecyclerView.ViewHolder {

    private SimpleDraweeView ivProfile;
    private TextView tvWriter;
    private TextView tvDate;
    private View convertView;

    public CommonStarMentionViewHolder(View itemView) {
        super(itemView);
        ivProfile = (SimpleDraweeView) itemView.findViewById(R.id.iv_star_mention_profile);
        tvWriter = (TextView) itemView.findViewById(R.id.tv_star_mention_name);
        tvDate = (TextView) itemView.findViewById(R.id.tv_star_mention_date);
        convertView = itemView;
    }

    @Override
    public String toString() {
        return "CommonStarMentionViewHolder{" +
                "ivProfile=" + ivProfile +
                ", tvWriter=" + tvWriter +
                ", tvDate=" + tvDate +
                ", convertView=" + convertView +
                '}';
    }

    public void bindView(StarMentionVO starMentionVO) {


        boolean isBot = EntityManager.getInstance().isBot(starMentionVO.getWriterId());
        boolean isJandiBot = EntityManager.getInstance().isJandiBot(starMentionVO.getWriterId());
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) ivProfile.getLayoutParams();
        if (!isJandiBot) {
            layoutParams.topMargin = ivProfile.getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin);
            layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 44f, ivProfile.getResources().getDisplayMetrics());
        } else {
            layoutParams.height = layoutParams.width * 5 / 4;
            layoutParams.topMargin = ivProfile.getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin) - layoutParams.width / 4;
        }

        ivProfile.setLayoutParams(layoutParams);

        if (!isJandiBot) {
            Uri uri = Uri.parse(starMentionVO.getWriterPictureUrl());
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
            ImageLoader.newBuilder()
                    .placeHolder(R.drawable.bot_80x100, ScalingUtils.ScaleType.CENTER_INSIDE)
                    .actualScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                    .load(UriFactory.getResourceUri(R.drawable.bot_80x100))
                    .into(ivProfile);
        }

        tvWriter.setText(starMentionVO.getWriterName());
        String updateTime = DateTransformator.getTimeString(starMentionVO.getUpdatedAt());
        tvDate.setText(updateTime);

    }

}
