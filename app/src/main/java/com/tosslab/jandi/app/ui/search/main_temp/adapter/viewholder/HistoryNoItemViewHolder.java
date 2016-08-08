package com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.object.SearchData;

import butterknife.ButterKnife;

/**
 * Created by tee on 16. 7. 29..
 */
public class HistoryNoItemViewHolder extends BaseViewHolder<SearchData> {

    public HistoryNoItemViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public static HistoryNoItemViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_search_history_no_item, parent, false);
        return new HistoryNoItemViewHolder(itemView);
    }

    @Override
    public void onBindView(SearchData searchData) {

    }

}
