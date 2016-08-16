package com.tosslab.jandi.app.ui.search.main.adapter.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.search.main.object.SearchData;

/**
 * Created by tee on 16. 7. 25..
 */
public class NoMessageItemViewHolder extends BaseViewHolder<SearchData> {

    public NoMessageItemViewHolder(View itemView) {
        super(itemView);
    }

    public static NoMessageItemViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_search_no_message_item, parent, false);
        return new NoMessageItemViewHolder(itemView);
    }

    @Override
    public void onBindView(SearchData data) {
    }

}
