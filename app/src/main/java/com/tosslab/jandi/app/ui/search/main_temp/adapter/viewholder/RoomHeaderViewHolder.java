package com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder;

import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tee on 16. 7. 21..
 */
public class RoomHeaderViewHolder extends BaseViewHolder {

    @Bind(R.id.cb_unjoin_user)
    AppCompatCheckBox cbUnjoinUser;

    public RoomHeaderViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public static RoomHeaderViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_search_room_header, parent, false);
        return new RoomHeaderViewHolder(itemView);
    }

    @Override
    public void onBindView(Object o) {

    }

}
