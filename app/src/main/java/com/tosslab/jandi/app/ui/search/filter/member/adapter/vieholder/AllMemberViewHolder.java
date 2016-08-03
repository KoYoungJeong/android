package com.tosslab.jandi.app.ui.search.filter.member.adapter.vieholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;

/**
 * Created by tonyjs on 16. 7. 22..
 */
public class AllMemberViewHolder extends BaseViewHolder<String> {

    public static AllMemberViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_member_all, parent, false);
        return new AllMemberViewHolder(itemView);
    }

    private AllMemberViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void onBindView(String s) {

    }
}
