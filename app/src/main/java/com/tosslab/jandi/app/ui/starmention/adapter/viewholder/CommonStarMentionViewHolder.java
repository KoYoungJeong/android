package com.tosslab.jandi.app.ui.starmention.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.starmention.vo.StarMentionVO;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.IonCircleTransform;

/**
 * Created by tee on 15. 8. 2..
 */
public class CommonStarMentionViewHolder extends RecyclerView.ViewHolder {

    private ImageView starMentionProfileView;
    private TextView starMentionNameView;
    private TextView starMentionDateView;
    private View convertView;

    public CommonStarMentionViewHolder(View itemView) {
        super(itemView);
        starMentionProfileView = (ImageView) itemView.findViewById(R.id.iv_star_mention_profile);
        starMentionNameView = (TextView) itemView.findViewById(R.id.tv_star_mention_name);
        starMentionDateView = (TextView) itemView.findViewById(R.id.tv_star_mention_date);
        convertView = itemView;
    }

    public ImageView getStarMentionProfileView() {
        return starMentionProfileView;
    }

    public TextView getStarMentionNameView() {
        return starMentionNameView;
    }

    public TextView getStarMentionDateView() {
        return starMentionDateView;
    }

    public View getConvertView() {
        return convertView;
    }

    @Override
    public String toString() {
        return "CommonStarMentionViewHolder{" +
                "starMentionProfileView=" + starMentionProfileView +
                ", starMentionNameView=" + starMentionNameView +
                ", starMentionDateView=" + starMentionDateView +
                ", convertView=" + convertView +
                '}';
    }

    public void bindView(StarMentionVO starMentionVO) {

        Ion.with(this.getStarMentionProfileView())
                .placeholder(R.drawable.jandi_profile)
                .error(R.drawable.jandi_profile)
                .transform(new IonCircleTransform())
                .load(starMentionVO.getWriterPictureUrl());

        this.getStarMentionNameView().setText(starMentionVO.getWriterName());
        String updateTime = DateTransformator.getTimeString(starMentionVO.getUpdatedAt());
        this.getStarMentionDateView().setText(updateTime);

    }

}
