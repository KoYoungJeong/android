package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
* Created by Steve SeongUg Jung on 15. 3. 19..
*/
public class RecyclerBodyViewHodler extends RecyclerView.ViewHolder {
    private final BodyViewHolder viewHolder;

    public RecyclerBodyViewHodler(View itemView, BodyViewHolder viewHolder) {
        super(itemView);
        this.viewHolder = viewHolder;
    }

    public BodyViewHolder getViewHolder() {
        return viewHolder;
    }
}
