package com.tosslab.jandi.app.ui.profile.defaultimage.adapter;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import com.tosslab.jandi.app.utils.image.loader.ImageLoader;

/**
 * Created by tee on 16. 1. 14..
 */
public class CharacterSelectorAdapter extends ProfileSelectorAdapter<RecyclerView.ViewHolder, String> {

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        ItemViewHolder viewHolder = (ItemViewHolder) holder;
        ImageView ivImageBox = viewHolder.ivImageBox;

        String characterUrl = items.get(position);
        ImageLoader.newInstance()
                .actualImageScaleType(ImageView.ScaleType.CENTER_CROP)
                .uri(Uri.parse(characterUrl))
                .into(ivImageBox);

        ivImageBox.setOnClickListener(v -> {
            selectedPosition = position;
            notifyDataSetChanged();
            onRecyclerItemClickListener.onItemClick(ivImageBox, CharacterSelectorAdapter.this, position);
        });
    }
}
