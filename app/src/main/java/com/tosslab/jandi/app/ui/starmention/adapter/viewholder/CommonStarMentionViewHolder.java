package com.tosslab.jandi.app.ui.starmention.adapter.viewholder;

import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
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
        if (!EntityManager.getInstance().isJandiBot(starMentionVO.getWriterId())) {

            Uri uri = Uri.parse(starMentionVO.getWriterPictureUrl());
            ImageUtil.loadProfileImage(ivProfile, uri, R.drawable.profile_img);
        } else {
            ImageLoader.newBuilder()
                    .placeHolder(R.drawable.profile_img, ScalingUtils.ScaleType.CENTER_INSIDE)
                    .actualScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                    .backgroundColor(Color.BLACK)
                    .load(UriFactory.getResourceUri(R.drawable.bot_43x54))
                    .into(ivProfile);
        }

        tvWriter.setText(starMentionVO.getWriterName());
        String updateTime = DateTransformator.getTimeString(starMentionVO.getUpdatedAt());
        tvDate.setText(updateTime);

    }

}
