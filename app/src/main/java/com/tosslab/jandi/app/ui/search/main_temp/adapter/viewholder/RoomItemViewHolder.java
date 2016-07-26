package com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tee on 16. 7. 21..
 */
public class RoomItemViewHolder extends BaseViewHolder {


    @Bind(R.id.iv_icon)
    ImageView ivIcon;

    @Bind(R.id.iv_info)
    ImageView ivInfo;

    @Bind(R.id.tv_room_name)
    TextView tvRoomName;

    @Bind(R.id.tv_room_description)
    TextView tvRoomDescription;

    @Bind(R.id.v_full_divider)
    View vFullDivider;

    @Bind(R.id.v_half_divider)
    View vHalfDivider;

    public RoomItemViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public static RoomItemViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_search_room_item, parent, false);
        return new RoomItemViewHolder(itemView);
    }

    @Override
    public void onBindView(Object o) {

    }

}
