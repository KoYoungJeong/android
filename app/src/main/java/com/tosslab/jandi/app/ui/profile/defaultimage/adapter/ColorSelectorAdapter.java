package com.tosslab.jandi.app.ui.profile.defaultimage.adapter;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

/**
 * Created by tee on 16. 1. 14..
 */
public class ColorSelectorAdapter extends ProfileSelectorAdapter<RecyclerView.ViewHolder, Integer> {
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        ItemViewHolder viewHolder = (ItemViewHolder) holder;
        ImageView ivImageBox = viewHolder.ivImageBox;
        int color = items.get(position);
        ivImageBox.setImageDrawable(new ColorDrawable(color));
        ivImageBox.setOnClickListener(v -> {
                    selectedPosition = position;
                    notifyDataSetChanged();
                    onRecyclerItemClickListener.onItemClick(ivImageBox, ColorSelectorAdapter.this, position);
                }
        );
    }
}
