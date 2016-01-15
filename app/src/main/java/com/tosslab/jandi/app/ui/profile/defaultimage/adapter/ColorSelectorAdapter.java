package com.tosslab.jandi.app.ui.profile.defaultimage.adapter;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by tee on 16. 1. 14..
 */
public class ColorSelectorAdapter<Integer> extends ProfileSelectorAdapter<Integer> {
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List payloads) {
        ItemViewHolder viewHolder = (ItemViewHolder) holder;
        ImageView ivImageBox = viewHolder.ivImageBox;
        int color = (java.lang.Integer) items.get(position);
        ivImageBox.setImageDrawable(new ColorDrawable(color));
        ivImageBox.setOnClickListener(v -> {
                    selectedPosition = position;
                    notifyDataSetChanged();
                    onRecyclerItemClickListener.onItemClick(ivImageBox, ColorSelectorAdapter.this, position);
                }
        );

        super.onBindViewHolder(holder, position, payloads);
    }
}
