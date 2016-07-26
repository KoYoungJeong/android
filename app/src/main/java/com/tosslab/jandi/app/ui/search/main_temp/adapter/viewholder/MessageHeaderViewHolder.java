package com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;

import butterknife.ButterKnife;

/**
 * Created by tee on 16. 7. 21..
 */
public class MessageHeaderViewHolder extends BaseViewHolder {

    public MessageHeaderViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public static MessageHeaderViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_search_message_header, parent, false);
        return new MessageHeaderViewHolder(itemView);
    }

    @Override
    public void onBindView(Object o) {

    }

}