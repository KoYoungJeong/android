package com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.object.SearchData;

/**
 * Created by tee on 16. 7. 25..
 */
public class NoRoomItemViewHolder extends BaseViewHolder<SearchData> {

    public NoRoomItemViewHolder(View itemView) {
        super(itemView);
    }

    public static NoRoomItemViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_search_no_room_item, parent, false);
        return new NoRoomItemViewHolder(itemView);
    }

    @Override
    public void onBindView(SearchData data) {
    }
}