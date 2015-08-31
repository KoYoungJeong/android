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

    private ImageView ivProfile;
    private TextView tvWriter;
    private TextView tvDate;
    private View convertView;

    public CommonStarMentionViewHolder(View itemView) {
        super(itemView);
        ivProfile = (ImageView) itemView.findViewById(R.id.iv_star_mention_profile);
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

        Ion.with(ivProfile)
                .placeholder(R.drawable.jandi_profile)
                .error(R.drawable.jandi_profile)
                .transform(new IonCircleTransform())
                .load(starMentionVO.getWriterPictureUrl());

        tvWriter.setText(starMentionVO.getWriterName());
        String updateTime = DateTransformator.getTimeString(starMentionVO.getUpdatedAt());
        tvDate.setText(updateTime);

    }

}
