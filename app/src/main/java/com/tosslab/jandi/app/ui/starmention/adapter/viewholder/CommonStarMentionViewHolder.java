package com.tosslab.jandi.app.ui.starmention.adapter.viewholder;

import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.starmention.vo.StarMentionVO;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.UriFactory;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;

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


        boolean isUser = !EntityManager.getInstance().isJandiBot(starMentionVO.getWriterId());
        ViewGroup.LayoutParams layoutParams = ivProfile.getLayoutParams();
        if (isUser) {
            layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 44f, ivProfile.getResources().getDisplayMetrics());
        } else {
            layoutParams.height = layoutParams.width * 5 / 4;
        }

        ivProfile.setLayoutParams(layoutParams);

        if (isUser) {
            Uri uri = Uri.parse(starMentionVO.getWriterPictureUrl());
            ImageUtil.loadProfileImage(ivProfile, uri, R.drawable.profile_img);
        } else {
            ImageLoader.newBuilder()
                    .placeHolder(R.drawable.bot_80x100, ScalingUtils.ScaleType.CENTER_INSIDE)
                    .actualScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                    .backgroundColor(Color.BLACK)
                    .load(UriFactory.getResourceUri(R.drawable.bot_80x100))
                    .into(ivProfile);
        }

        tvWriter.setText(starMentionVO.getWriterName());
        String updateTime = DateTransformator.getTimeString(starMentionVO.getUpdatedAt());
        tvDate.setText(updateTime);

    }

}
