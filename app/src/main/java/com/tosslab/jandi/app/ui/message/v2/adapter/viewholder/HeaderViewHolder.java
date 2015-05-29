package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.R;

/**
 * Created by Steve SeongUg Jung on 15. 1. 21..
 */
public class HeaderViewHolder extends RecyclerView.ViewHolder {
    public TextView dateTextView;

    public HeaderViewHolder(View itemView) {
        super(itemView);
        dateTextView = (TextView) itemView.findViewById(R.id.txt_message_date_devider);
    }
}
