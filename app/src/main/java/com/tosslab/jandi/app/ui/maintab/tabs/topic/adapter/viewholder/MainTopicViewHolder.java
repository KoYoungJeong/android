package com.tosslab.jandi.app.ui.maintab.tabs.topic.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by tee on 2017. 2. 10..
 */

public abstract class MainTopicViewHolder<Item> extends RecyclerView.ViewHolder {


    protected OnItemClickListener onItemClickListener;

    public MainTopicViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void bind(Item item);

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick();
    }

}
