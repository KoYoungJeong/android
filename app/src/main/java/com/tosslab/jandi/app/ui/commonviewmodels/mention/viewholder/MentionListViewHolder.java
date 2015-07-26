package com.tosslab.jandi.app.ui.commonviewmodels.mention.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;

/**
 * Created by tee on 15. 7. 21..
 */
public class MentionListViewHolder extends RecyclerView.ViewHolder {

    private ImageView ivIcon;
    private TextView tvName;
    private View convertView;

    public MentionListViewHolder(View itemView) {
        super(itemView);
        ivIcon = (ImageView) itemView.findViewById(R.id.img_member_item_icon);
        tvName = (TextView) itemView.findViewById(R.id.tv_member_item_name);
        convertView = itemView;
    }

    public ImageView getIvIcon() {
        return ivIcon;
    }

    public TextView getTvName() {
        return tvName;
    }

    public View getConvertView() {
        return convertView;
    }
}
