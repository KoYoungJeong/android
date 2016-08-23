package com.tosslab.jandi.app.ui.search.filter.member.adapter.vieholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;

/**
 * Created by tee on 2016. 8. 22..
 */

public class EmptyMemberViewHolder extends BaseViewHolder<String> {

    public static EmptyMemberViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_no_search_result, parent, false);
        return new EmptyMemberViewHolder(itemView);
    }

    private EmptyMemberViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void onBindView(String s) {

    }

}
