package com.tosslab.jandi.app.ui.starred.adapter.viewholder;

import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.starred.vo.StarMentionVO;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.image.transform.JandiProfileTransform;
import com.tosslab.jandi.app.utils.image.transform.TransformConfig;

public class CommonStarMentionViewHolder extends RecyclerView.ViewHolder {

    private ImageView ivProfile;
    private TextView tvWriter;
    private TextView tvDate;
    private View convertView;

    public CommonStarMentionViewHolder(View itemView) {
        super(itemView);
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


        boolean isBot = TeamInfoLoader.getInstance().isBot(starMentionVO.getWriterId());
        boolean isJandiBot = TeamInfoLoader.getInstance().isJandiBot(starMentionVO.getWriterId());
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
            if (!isBot) {
                ImageUtil.loadProfileImage(ivProfile, starMentionVO.getWriterPictureUrl(), R.drawable.profile_img);
            } else {

                Uri uri = Uri.parse(ImageUtil.getLargeProfileUrl(starMentionVO.getWriterPictureUrl()));
                ImageLoader.newInstance()
                        .placeHolder(R.drawable.profile_img, ImageView.ScaleType.FIT_CENTER)
                        .actualImageScaleType(ImageView.ScaleType.CENTER_CROP)
                        .transformation(new JandiProfileTransform(ivProfile.getContext(),
                                TransformConfig.DEFAULT_CIRCLE_BORDER_WIDTH,
                                TransformConfig.DEFAULT_CIRCLE_BORDER_COLOR,
                                Color.TRANSPARENT))
                        .uri(uri)
                        .into(ivProfile);
            }
        } else {
            ivProfile.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            ImageLoader.loadFromResources(ivProfile, R.drawable.bot_80x100);
        }

        tvWriter.setText(starMentionVO.getWriterName());
        String updateTime = DateTransformator.getTimeString(starMentionVO.getUpdatedAt());
        tvDate.setText(updateTime);

    }

}
